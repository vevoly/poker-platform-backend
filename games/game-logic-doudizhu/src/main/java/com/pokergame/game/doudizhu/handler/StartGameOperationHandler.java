package com.pokergame.game.doudizhu.handler;

import com.iohao.game.widget.light.room.operation.OperationHandler;
import com.iohao.game.widget.light.room.operation.PlayerOperationContext;
import com.pokergame.common.card.Card;
import com.pokergame.common.deal.MultiPlayerDealContext;
import com.pokergame.common.deal.dealer.Dealer;
import com.pokergame.common.deal.dealer.impl.DoudizhuDealer;
import com.pokergame.common.game.GameType;
import com.pokergame.core.exception.GameCode;
import com.pokergame.game.doudizhu.broadcast.DoudizhuBroadcastKit;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 开始游戏操作处理器
 *
 * 职责：
 * 1. 校验开始游戏条件
 * 2. 调用 DoudizhuDealer 发牌
 * 3. 初始化游戏状态
 * 4. 广播游戏开始
 *
 * @author poker-platform
 */
@Slf4j
public final class StartGameOperationHandler implements OperationHandler {

    @Override
    public boolean processVerify(PlayerOperationContext context) {
        DoudizhuRoom room = context.getRoom();
        long userId = context.getUserId();

        // 1. 必须是房主
        GameCode.NOT_ROOM_OWNER.assertTrueThrows(room.getOwnerId() != userId);

        // 2. 人数必须达到要求（至少2人）
        int playerCount = room.getPlayerMap().size();
        GameCode.ROOM_NOT_ENOUGH_PLAYERS.assertTrueThrows(playerCount < 2);

        // 3. 所有玩家必须已准备
        GameCode.PLAYER_NOT_READY.assertTrueThrows(!room.isAllReady());

        // 4. 游戏状态必须是等待或准备中
        DoudizhuGameStatus status = room.getGameStatus();
        boolean canStart = status == DoudizhuGameStatus.WAITING ||
                status == DoudizhuGameStatus.READY;
        GameCode.ILLEGAL_OPERATION.assertTrueThrows(!canStart);

        return true;
    }

    @Override
    public void process(PlayerOperationContext context) {
        DoudizhuRoom room = context.getRoom();
        List<DoudizhuPlayer> players = room.getAllDoudizhuPlayers();
        List<Long> playerIds = players.stream()
                .map(DoudizhuPlayer::getUserId)
                .collect(Collectors.toList());

        log.info("房间 {} 开始发牌，玩家: {}", room.getRoomId(), playerIds);

        // ==================== 1. 构建发牌上下文 ====================
        MultiPlayerDealContext dealContext = buildDealContext(room, playerIds);

        // ==================== 2. 调用 DoudizhuDealer 发牌 ====================
        DoudizhuDealer dealer = new DoudizhuDealer(playerIds.size());
        List<List<Card>> hands = dealer.deal(dealContext);

        // ==================== 3. 分配手牌 ====================
        for (int i = 0; i < players.size(); i++) {
            DoudizhuPlayer player = players.get(i);
            List<Card> hand = hands.get(i);
            player.setHandCards(hand);
            log.debug("玩家 {} 获得 {} 张手牌", player.getUserId(), hand.size());
        }

        // ==================== 4. 确定出牌顺序 ====================
        // 默认按玩家加入顺序，第一个玩家为先手
        List<Long> playOrder = new ArrayList<>(playerIds);
        long firstPlayerId = playerIds.get(0);
        room.setPlayOrder(playOrder, firstPlayerId);

        // ==================== 5. 更新游戏状态 ====================
        room.changeGameStatus(DoudizhuGameStatus.BIDDING);

        log.info("房间 {} 游戏开始，进入叫地主阶段", room.getRoomId());

        // ==================== 6. 广播游戏开始 ====================
        DoudizhuBroadcastKit.broadcastGameStart(room);
    }

    /**
     * 构建发牌上下文
     */
    private MultiPlayerDealContext buildDealContext(DoudizhuRoom room, List<Long> playerIds) {
        // 构建每个玩家的数据
        List<Integer> vipLevels = new ArrayList<>();
        List<Integer> lossCounts = new ArrayList<>();
        List<Integer> winCounts = new ArrayList<>();
        List<Boolean> rookieFlags = new ArrayList<>();
        List<Boolean> aiFlags = new ArrayList<>();

        for (Long playerId : playerIds) {
            DoudizhuPlayer player = room.getDoudizhuPlayer(playerId);
            // TODO: 从 service-user 获取真实数据
            vipLevels.add(0);
            lossCounts.add(0);
            winCounts.add(0);
            rookieFlags.add(false);
            aiFlags.add(player.isRobot());
        }

        // 地主索引暂时为0，实际由叫地主流程决定
        // 注意：DoudizhuDealer 中地主和农民的基础手牌都是17张
        // 底牌会在 extractLandlordCards 中额外抽取
        return MultiPlayerDealContext.builder()
                .gameType(GameType.DOUDIZHU)
                .playerCount(playerIds.size())
                .playerIds(playerIds)
                .landlordIndex(0)  // 临时，实际由叫地主决定
                .vipLevels(vipLevels)
                .consecutiveLosses(lossCounts)
                .consecutiveWins(winCounts)
                .rookieFlags(rookieFlags)
                .aiFlags(aiFlags)
                .build();
    }
}
