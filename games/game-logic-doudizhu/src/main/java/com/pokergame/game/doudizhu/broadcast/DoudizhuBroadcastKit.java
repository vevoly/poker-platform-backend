package com.pokergame.game.doudizhu.broadcast;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.pokergame.common.card.CardDTO;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.cmd.WSCmd;
import com.pokergame.common.converter.Convertible;
import com.pokergame.common.enums.TurnPhase;
import com.pokergame.common.game.GameType;
import com.pokergame.common.model.broadcast.*;
import com.pokergame.common.model.player.PlayerInfoDTO;
import com.pokergame.core.broadcast.BroadcastKit;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 斗地主广播工具，封装斗地主特有的广播消息。
 * 复用 BaseBroadcastKit 发送广播，广播数据都继承 BaseBroadcastData 并自动设置 gameType。
 */
@Slf4j
public final class DoudizhuBroadcastKit {

    private DoudizhuBroadcastKit() {}

    private static final int GAME_TYPE = GameType.DOUDIZHU.getCode();

    /**
     * 广播玩家准备状态
     */
    public static void broadcastReady(DoudizhuPlayer player, boolean ready, DoudizhuRoom room) {
        // 填充基础数据
        ReadyBroadcastData data = BroadcastKit.fill(new ReadyBroadcastData(), GAME_TYPE, player.getUserId());;
        data.setReady(ready);
        data.setNickname(player.getNickname());
        BroadcastKit.broadcastToRoom(room, WSCmd.CMD, WSCmd.READY_BROADCAST, data);
    }

    /**
     * 广播玩家进入房间
     */
    public static void broadcastEnterRoom(DoudizhuPlayer player, DoudizhuRoom room) {
        EnterRoomBroadcastData data = BroadcastKit.fill(new EnterRoomBroadcastData(), GAME_TYPE, player.getUserId());
        data.setNickname(player.getNickname());
        data.setPlayerCount(room.getPlayerCount());
        data.setMaxPlayers(room.getMaxPlayers());
        BroadcastKit.broadcastToRoom(room, WSCmd.CMD, WSCmd.ENTER_ROOM_BROADCAST, data);
    }

    /**
     * 广播玩家离开房间
     */
    public static void broadcastLeaveRoom(long userId, DoudizhuRoom room) {
        QuitRoomBroadcastData data = BroadcastKit.fill(new QuitRoomBroadcastData(), GAME_TYPE, userId);
        data.setPlayerCount(room.getPlayerCount());
        BroadcastKit.broadcastToRoom(room, WSCmd.CMD, WSCmd.QUIT_ROOM_BROADCAST, data);
    }

    /**
     * 广播发牌
     */
    public static void broadcastDealCards(long userId, List<CardDTO> handCards, DoudizhuRoom room) {
        DealCardsBroadcastData data = BroadcastKit.fill(new DealCardsBroadcastData(), GAME_TYPE, userId);
        data.setHandCards(handCards);
        BroadcastKit.broadcastToUser(room, userId, WSCmd.CMD, WSCmd.DEAL_CARDS_BROADCAST, data);
    }

    /**
     * 广播游戏开始
     */
    public static void broadcastGameStart(DoudizhuRoom room) {
        GameStartBroadcastData data = BroadcastKit.fill(new GameStartBroadcastData(), GAME_TYPE, room.getOwnerId());
        data.setRoomId(room.getRoomId());
        // 注意：players 列表中的每个 DoudizhuPlayer 可能需要转换为不含敏感信息的 DTO
        List<PlayerInfoDTO> players = Convertible.toDTOList(room.getAllDoudizhuPlayers());
        data.setPlayers(players);
        BroadcastKit.broadcastToRoom(room, WSCmd.CMD, WSCmd.GAME_START_BROADCAST, data);
    }

    /**
     * 广播出牌
     */
    public static void broadcastPlayCard(long playerId, List<CardDTO> cards, CardPattern cardPattern, DoudizhuRoom room) {
        PlayCardBroadcastData data = BroadcastKit.fill(new PlayCardBroadcastData(), GAME_TYPE, playerId);
        data.setCards(cards);
        data.setRemain(room.getDoudizhuPlayer(playerId).getCardCount());
        data.setPattern(cardPattern.getCode());
        BroadcastKit.broadcastToRoom(room, WSCmd.CMD, WSCmd.PLAY_CARD_BROADCAST, data);
    }

    /**
     * 广播过牌
     */
    public static void broadcastPass(long userId, DoudizhuRoom room) {
        PassBroadcastData data = BroadcastKit.fill(new PassBroadcastData(), GAME_TYPE, userId);
        BroadcastKit.broadcastToRoom(room, WSCmd.CMD, WSCmd.PASS_BROADCAST, data);
    }

    /**
     * 广播抢地主
     */
    public static void broadcastGrabLandlord(long userId, int multiple, DoudizhuRoom room) {
        GrabLandlordBroadcastData data = BroadcastKit.fill(new GrabLandlordBroadcastData(), GAME_TYPE, userId);
        data.setMultiple(multiple);
        BroadcastKit.broadcastToRoom(room, WSCmd.CMD, WSCmd.GRAB_LANDLORD_BROADCAST, data);
    }

    /**
     * 广播不抢地主
     */
    public static void broadcastNotGrab(long userId, DoudizhuRoom room) {
        NotGrabBroadcastData data = BroadcastKit.fill(new NotGrabBroadcastData(), GAME_TYPE, userId);
        BroadcastKit.broadcastToRoom(room, WSCmd.CMD, WSCmd.NOT_GRAB_LANDLORD_BROADCAST, data);
    }

    /**
     * 广播叫地主回合
     */
    public static void broadcastBiddingTurn(long playerId, int round, DoudizhuRoom room) {
        TurnBroadcastData data = BroadcastKit.fill(new TurnBroadcastData(), GAME_TYPE, playerId);
        data.setPhase(TurnPhase.BIDDING);
        data.setBiddingRound(round);
        data.setTimeoutSeconds(15);
        BroadcastKit.broadcastToRoom(room, WSCmd.CMD, WSCmd.TURN_BROADCAST, data);
    }

    /**
     * 广播出牌回合（round=0 表示出牌阶段）
     */
    public static void broadcastTurn(long playerId, DoudizhuRoom room) {
        TurnBroadcastData data = BroadcastKit.fill(new TurnBroadcastData(), GAME_TYPE, playerId);
        data.setPhase(TurnPhase.PLAYING);
        data.setBiddingRound(0);          // 约定 0 表示出牌回合
        data.setTimeoutSeconds(30);       // 出牌超时 30 秒
        BroadcastKit.broadcastToRoom(room, WSCmd.CMD, WSCmd.TURN_BROADCAST, data);
    }

    /**
     * 广播托管状态改变
     */
    public static void broadcastTrusteeshipChange(long userId, boolean isTrustee, DoudizhuRoom room) {
        TrusteeshipChangeBroadcastData data = BroadcastKit.fill(new TrusteeshipChangeBroadcastData(), GAME_TYPE, userId);
        data.setTrusteeship(isTrustee);
        BroadcastKit.broadcastToRoom(room, WSCmd.CMD, WSCmd.TRUSTEESHIP_CHANGE_BROADCAST, data);
    }

    /**
     * 广播游戏结束
     */
    public static void broadcastGameEnd(long userId, DoudizhuRoom room) {
        GameEndBroadcastData data = BroadcastKit.fill(new GameEndBroadcastData(), GAME_TYPE, userId);;
        BroadcastKit.broadcastToRoom(room, WSCmd.CMD, WSCmd.GAME_END_BROADCAST, data);
    }

    // ==================== 广播数据类（都继承 BaseBroadcastData） ====================

    @Data
    @ProtobufClass
    public static class GrabLandlordBroadcastData extends BaseBroadcastData {
        private long userId;
        private int multiple;
    }

    @Data
    @ProtobufClass
    public static class NotGrabBroadcastData extends BaseBroadcastData {
        private long userId;
    }

    @Data
    @ProtobufClass
    public static class BiddingTurnData extends BaseBroadcastData {
        private long playerId;
        private int round;
        private int timeoutSeconds;
    }
}
