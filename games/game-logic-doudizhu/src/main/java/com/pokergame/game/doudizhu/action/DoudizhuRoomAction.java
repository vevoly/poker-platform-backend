package com.pokergame.game.doudizhu.action;


import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.iohao.game.widget.light.room.flow.RoomCreateContext;
import com.pokergame.common.card.Card;
import com.pokergame.common.cmd.RoomCmd;
import com.pokergame.common.context.MyFlowContext;
import com.pokergame.common.converter.Convertible;
import com.pokergame.common.deal.MultiPlayerDealContext;
import com.pokergame.common.deal.dealer.impl.DoudizhuDealer;
import com.pokergame.common.event.GameStartEvent;
import com.pokergame.common.exception.GameCode;
import com.pokergame.common.game.GameType;
import com.pokergame.common.model.game.StartGameReq;
import com.pokergame.common.model.room.*;
import com.pokergame.common.util.EventPublisher;
import com.pokergame.core.base.BasePlayer;
import com.pokergame.core.base.BaseRoomAction;
import com.pokergame.game.doudizhu.bidding.BiddingManager;
import com.pokergame.game.doudizhu.broadcast.DoudizhuBroadcastKit;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import com.pokergame.game.doudizhu.state.DoudizhuGameStateManager;
import com.pokergame.game.doudizhu.turn.DoudizhuTurnManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 斗地主房间操作 Action
 * <p>
 * 继承 {@link BaseRoomAction}，实现斗地主特有的房间操作和游戏逻辑。
 * 所有需要暴露给客户端的命令都通过 {@code @ActionMethod} 标记，并直接调用父类实现（创建房间除外）。
 * 广播逻辑通过重写钩子方法 {@code afterJoin}, {@code afterLeave}, {@code afterReady} 完成。
 */
@Slf4j
@ActionController(RoomCmd.CMD)
public class DoudizhuRoomAction extends BaseRoomAction<DoudizhuRoom> {

    private final DoudizhuRoomService roomService = DoudizhuRoomService.me();

    // ==================== 实现 BaseRoomAction 的抽象方法 ====================

    @Override
    protected DoudizhuRoomService getRoomService() {
        return roomService;
    }

    @Override
    protected String getNickname(FlowContext ctx) {
        // 假设实际使用的是 MyFlowContext，它提供了 getNickname() 方法
        if (ctx instanceof MyFlowContext) {
            return ((MyFlowContext) ctx).getNickname();
        }
        return "玩家" + ctx.getUserId();
    }

    @Override
    protected BasePlayer createPlayer(long userId, String nickname) {
        return new DoudizhuPlayer(userId, nickname);
    }

    /**
     * 斗地主游戏开始的具体逻辑（发牌、初始化叫地主管理器、发布事件、广播等）
     */
    @Override
    protected void doStartGame(DoudizhuRoom room, FlowContext ctx) {
        DoudizhuGameStateManager gameState = room.getStateManager();
        List<DoudizhuPlayer> players = room.getAllDoudizhuPlayers();
        List<Long> playerIds = players.stream()
                .map(DoudizhuPlayer::getUserId)
                .collect(Collectors.toList());

        log.info("房间 {} 开始游戏，玩家: {}", room.getRoomId(), playerIds);

        // 1. 构建发牌上下文并发牌
        MultiPlayerDealContext dealContext = buildDealContext(room, playerIds);
        DoudizhuDealer dealer = new DoudizhuDealer(playerIds.size());
        List<List<Card>> hands = dealer.deal(dealContext);
        List<Card> landlordCards = dealer.getLandlordCards();

        for (int i = 0; i < players.size(); i++) {
            DoudizhuPlayer player = players.get(i);
            player.setHandCards(hands.get(i));
            log.debug("玩家 {} 获得 {} 张手牌", player.getUserId(), hands.get(i).size());
            // 定向发送手牌（私有消息）
            DoudizhuBroadcastKit.broadcastDealCards(player.getUserId(), Convertible.toDTOList(hands.get(i)), room);
        }

        gameState.setLandlordExtraCards(landlordCards);
        log.info("底牌: {}", landlordCards);

        // 2. 初始化叫地主管理器
        BiddingManager biddingManager = new BiddingManager(room);
        room.setBiddingManager(biddingManager);

        // 3. 更新房间状态为叫地主阶段
        room.updateGameStatus(DoudizhuGameStatus.BIDDING);

        // 4. 发布游戏开始事件（供机器人服务订阅）
        EventPublisher.fire(ctx, () -> new GameStartEvent(
                GameType.DOUDIZHU,
                String.valueOf(room.getRoomId()),
                playerIds,
                0
        ));

        // 5. 广播游戏开始给房间内所有玩家
        DoudizhuBroadcastKit.broadcastGameStart(room);

        // 6. 启动叫地主流程
        biddingManager.start();

        log.info("房间 {} 游戏开始，进入叫地主阶段", room.getRoomId());
    }

    /**
     * 构建房间状态 DTO（包含玩家列表、手牌数量、托管状态等）
     */
    @Override
    protected RoomStateDTO buildRoomState(DoudizhuRoom room, long userId) {
        RoomStateDTO dto = new RoomStateDTO();
        dto.setRoomId(room.getRoomId());
        dto.setOwnerId(room.getOwnerId());
        dto.setMaxPlayers(room.getMaxPlayers());
        dto.setGameStatus(room.getGameStatus());

        // 玩家列表（包含座位号）
        Map<Integer, Long> seatMap = room.getPlayerSeatMap();
        List<PlayerStateDTO> players = room.getAllDoudizhuPlayers().stream()
                .map(p -> {
                    int seat = seatMap.entrySet().stream()
                            .filter(e -> e.getValue().equals(p.getUserId()))
                            .map(Map.Entry::getKey).findFirst().orElse(-1);
                    return new PlayerStateDTO()
                            .setUserId(p.getUserId())
                            .setNickname(p.getNickname())
                            .setReady(p.isReady())
                            .setLandlord(p.isLandlord())
                            .setTrusteeship(room.getTrusteeshipManager().isTrustee(p.getUserId()))
                            .setCardCount(p.getCardCount())
                            .setSeat(seat);
                })
                .collect(Collectors.toList());
        dto.setPlayers(players);

        DoudizhuGameStateManager stateManager = room.getStateManager();
        if (stateManager != null) {
            // 当前回合玩家
            DoudizhuPlayer current = stateManager.getCurrentPlayer(room);
            dto.setCurrentTurnUserId(current != null ? current.getUserId() : 0);

            // 叫地主轮数
            DoudizhuGameStatus status = (DoudizhuGameStatus) room.getGameStatusEnum();
            if (status == DoudizhuGameStatus.BIDDING && room.getBiddingManager() != null) {
                dto.setBiddingRound(room.getBiddingManager().getCurrentBiddingRound());
            } else {
                dto.setBiddingRound(0);
            }

            dto.setLandlordId(stateManager.getLandlordId());
            dto.setLastPlayCards(Convertible.toDTOList(stateManager.getLastPlayCards()));
            dto.setLastPlayPattern(stateManager.getLastPattern() != null ? stateManager.getLastPattern().getPattern().getCode() : 0);
            dto.setMultiplier(stateManager.getMultiplier());

            // 底牌：仅地主本人可见
            if (userId == stateManager.getLandlordId() && stateManager.getLandlordExtraCards() != null) {
                dto.setLandlordExtraCards(Convertible.toDTOList(stateManager.getLandlordExtraCards()));
            } else {
                dto.setLandlordExtraCards(Collections.emptyList());
            }

            // 剩余超时时间（需要 TurnManager 支持）
            DoudizhuTurnManager turnManager = room.getTurnManager();
            if (turnManager != null) {
                dto.setTimeoutSeconds(turnManager.getRemainingTimeoutSeconds());
            } else {
                dto.setTimeoutSeconds(0);
            }
        } else {
            // 游戏未开始时的默认值
            dto.setCurrentTurnUserId(0);
            dto.setTimeoutSeconds(0);
            dto.setBiddingRound(0);
            dto.setLandlordId(0);
            dto.setLastPlayCards(Collections.emptyList());
            dto.setLastPlayPattern(0);
            dto.setMultiplier(1);
            dto.setLandlordExtraCards(Collections.emptyList());
        }

        return dto;
    }

    // ==================== 重写钩子方法以实现广播 ====================

    @Override
    protected void afterJoin(DoudizhuRoom room, FlowContext ctx) {
        DoudizhuPlayer player = room.getDoudizhuPlayer(ctx.getUserId());
        if (player != null) {
            DoudizhuBroadcastKit.broadcastEnterRoom(player, room);
        }
    }

    @Override
    protected void afterLeave(DoudizhuRoom room, FlowContext ctx) {
        long userId = ctx.getUserId();
        DoudizhuBroadcastKit.broadcastLeaveRoom(userId, room);
    }

    @Override
    protected void afterReady(DoudizhuRoom room, FlowContext ctx, boolean isReady) {
        DoudizhuPlayer player = room.getDoudizhuPlayer(ctx.getUserId());
        if (player != null) {
            player.setReady(isReady);
            DoudizhuBroadcastKit.broadcastReady(player, isReady, room);
        }
    }

    // ==================== 暴露给客户端的 Action 方法（带 @ActionMethod） ====================

    /**
     * 创建房间（斗地主特有逻辑，不能复用父类）
     */
    @Override
    @ActionMethod(RoomCmd.CREATE_ROOM)
    public CreateRoomResp createRoom(CreateRoomReq req, FlowContext ctx) {
        long userId = ctx.getUserId();

        // 检查玩家是否已在其他房间
        DoudizhuRoom existingRoom = roomService.getUserRoom(userId);
        GameCode.PLAYER_ALREADY_IN_ROOM.assertTrueThrows(existingRoom != null);

        // 创建房间上下文
        RoomCreateContext createContext = RoomCreateContext.of(userId)
                .setSpaceSize(req.getMaxPlayers());

        DoudizhuRoom room = roomService.createRoom(createContext);
        room.setOwnerId(userId);
        room.setMaxPlayers(req.getMaxPlayers());
        room.setGameStatus(DoudizhuGameStatus.WAITING.name());

        // 初始化斗地主游戏状态（必须在添加玩家前调用）
        room.initGameState();

        // 创建房主玩家
        DoudizhuPlayer player = new DoudizhuPlayer(userId, getNickname(ctx));

        // 加入房间
        room.addPlayer(player);
        roomService.addPlayer(room, player);

        log.info("玩家 {} 创建房间: roomId={}, maxPlayers={}", userId, room.getRoomId(), req.getMaxPlayers());

        return new CreateRoomResp()
                .setRoomId(room.getRoomId())
                .setOwnerId(room.getOwnerId())
                .setMaxPlayers(room.getMaxPlayers())
                .setPlayerCount(room.getPlayerCount())
                .setGameStatus(room.getGameStatus())
                .setRoomName(req.getRoomName());
    }

    @Override
    @ActionMethod(RoomCmd.JOIN_ROOM)
    public JoinRoomResp joinRoom(JoinRoomReq req, FlowContext ctx) {
        return super.joinRoom(req, ctx);
    }

    @Override
    @ActionMethod(RoomCmd.LEAVE_ROOM)
    public void leaveRoom(LeaveRoomReq req, FlowContext ctx) {
        super.leaveRoom(req, ctx);
    }

    @Override
    @ActionMethod(RoomCmd.READY)
    public void ready(ReadyRoomReq req, FlowContext ctx) {
        super.ready(req, ctx);
    }

    @Override
    @ActionMethod(RoomCmd.START_GAME)
    public void startGame(StartGameReq req, FlowContext ctx) {
        super.startGame(req, ctx);
    }

    @Override
    @ActionMethod(RoomCmd.TRUSTEESHIP)
    public void trusteeship(TrusteeshipReq req, FlowContext ctx) {
        super.trusteeship(req, ctx);
    }

    @Override
    @ActionMethod(RoomCmd.ROOM_STATE)
    public RoomStateDTO roomState(FlowContext ctx) {
        return super.roomState(ctx);
    }

    // ==================== 私有辅助方法 ====================

    private MultiPlayerDealContext buildDealContext(DoudizhuRoom room, List<Long> playerIds) {
        List<Integer> vipLevels = new ArrayList<>();
        List<Integer> lossCounts = new ArrayList<>();
        List<Integer> winCounts = new ArrayList<>();
        List<Boolean> rookieFlags = new ArrayList<>();
        List<Boolean> aiFlags = new ArrayList<>();

        for (Long userId : playerIds) {
            DoudizhuPlayer player = room.getDoudizhuPlayer(userId);
            // TODO: 从 service-user 或统计数据中获取真实行为数据
            vipLevels.add(0);
            lossCounts.add(0);
            winCounts.add(0);
            rookieFlags.add(false);
            aiFlags.add(player.isRobot());
        }

        return MultiPlayerDealContext.builder()
                .gameType(GameType.DOUDIZHU)
                .playerCount(playerIds.size())
                .playerIds(playerIds)
                .landlordIndex(0)
                .vipLevels(vipLevels)
                .consecutiveLosses(lossCounts)
                .consecutiveWins(winCounts)
                .rookieFlags(rookieFlags)
                .aiFlags(aiFlags)
                .build();
    }
}