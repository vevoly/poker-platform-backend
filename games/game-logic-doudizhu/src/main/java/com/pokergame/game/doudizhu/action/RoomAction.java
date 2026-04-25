package com.pokergame.game.doudizhu.action;

import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.iohao.game.widget.light.room.flow.RoomCreateContext;
import com.pokergame.common.card.Card;
import com.pokergame.common.cmd.RoomCmd;
import com.pokergame.common.context.MyFlowContext;
import com.pokergame.common.deal.MultiPlayerDealContext;
import com.pokergame.common.deal.dealer.impl.DoudizhuDealer;
import com.pokergame.common.event.GameStartEvent;
import com.pokergame.common.game.GameType;
import com.pokergame.common.model.game.StartGameReq;
import com.pokergame.common.model.room.*;
import com.pokergame.common.exception.GameCode;
import com.pokergame.common.util.EventPublisher;
import com.pokergame.core.room.RoomOperations;
import com.pokergame.game.doudizhu.bidding.BiddingManager;
import com.pokergame.game.doudizhu.broadcast.DoudizhuBroadcastKit;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import com.pokergame.game.doudizhu.state.DoudizhuGameStateManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 斗地主房间操作 Action
 * 使用 RoomOperations 工具类复用公共房间操作逻辑
 */
@Slf4j
@ActionController(RoomCmd.CMD)
public class RoomAction {

    private final DoudizhuRoomService roomService = DoudizhuRoomService.me();

    @ActionMethod(RoomCmd.CREATE_ROOM)
    public CreateRoomResp createRoom(CreateRoomReq req, MyFlowContext ctx) {
        long userId = ctx.getUserId();

        // 检查玩家是否已在其他房间
        DoudizhuRoom existingRoom = roomService.getUserRoom(userId);
        GameCode.PLAYER_ALREADY_IN_ROOM.assertTrueThrows(existingRoom != null);

        // 创建房间上下文
        RoomCreateContext createContext = RoomCreateContext.of(userId)
                .setSpaceSize(req.getMaxPlayers());

        // 创建房间
        DoudizhuRoom room = roomService.createRoom(createContext);
        room.setOwnerId(userId);
        room.setMaxPlayers(req.getMaxPlayers());
        room.setGameStatus(DoudizhuGameStatus.WAITING.name());

        // 初始化斗地主游戏状态（必须在添加玩家前调用）
        room.initGameState();

        // 创建房主玩家
        DoudizhuPlayer player = new DoudizhuPlayer(userId, ctx.getNickname());

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

    @ActionMethod(RoomCmd.JOIN_ROOM)
    public JoinRoomResp joinRoom(JoinRoomReq req, MyFlowContext ctx) {
        String nickname = ctx.getNickname();
        if (nickname == null || nickname.isEmpty()) {
            nickname = "玩家" + ctx.getUserId();
        }
        String finalNickname = nickname;
        return RoomOperations.joinRoom(
                ctx,
                req.getRoomId(),
                roomService,
                userId -> new DoudizhuPlayer(userId, finalNickname),
                room -> {
                    DoudizhuRoom doudizhuRoom = (DoudizhuRoom) room;
                    DoudizhuPlayer player = doudizhuRoom.getDoudizhuPlayer(ctx.getUserId());
                    if (player != null) {
                        DoudizhuBroadcastKit.broadcastEnterRoom(player, doudizhuRoom);
                    }
                }
        );
    }

    @ActionMethod(RoomCmd.LEAVE_ROOM)
    public void leaveRoom(LeaveRoomReq req, FlowContext ctx) {
        RoomOperations.leaveRoom(ctx, roomService, room -> {
            DoudizhuRoom doudizhuRoom = (DoudizhuRoom) room;
            long userId = ctx.getUserId();
            DoudizhuBroadcastKit.broadcastLeaveRoom(userId, doudizhuRoom);
        });
    }

    @ActionMethod(RoomCmd.READY)
    public void ready(ReadyRoomReq req, MyFlowContext ctx) {
        RoomOperations.ready(ctx, req.isReady(), roomService, room -> {
            DoudizhuRoom doudizhuRoom = (DoudizhuRoom) room;
            long userId = ctx.getUserId();
            DoudizhuPlayer player = doudizhuRoom.getDoudizhuPlayer(userId);
            if (player != null) {
                player.setReady(req.isReady());
                DoudizhuBroadcastKit.broadcastReady(player, req.isReady(), doudizhuRoom);
            }
        });
    }

    @ActionMethod(RoomCmd.START_GAME)
    public void startGame(StartGameReq req, MyFlowContext ctx) {
        RoomOperations.startGame(ctx, roomService, room -> {
            DoudizhuRoom doudizhuRoom = (DoudizhuRoom) room;
            DoudizhuGameStateManager gameState = doudizhuRoom.getStateManager();
            List<DoudizhuPlayer> players = doudizhuRoom.getAllDoudizhuPlayers();
            List<Long> playerIds = players.stream()
                    .map(DoudizhuPlayer::getUserId)
                    .collect(Collectors.toList());

            log.info("房间 {} 开始游戏，玩家: {}", doudizhuRoom.getRoomId(), playerIds);

            // ==================== 1. 发牌 ====================
            // 构建发牌上下文（包含玩家行为数据，用于智能发牌）
            MultiPlayerDealContext dealContext = buildDealContext(doudizhuRoom, playerIds);
            DoudizhuDealer dealer = new DoudizhuDealer(playerIds.size());
            List<List<Card>> hands = dealer.deal(dealContext);
            List<Card> landlordCards = dealer.getLandlordCards();

            // 分配手牌
            for (int i = 0; i < players.size(); i++) {
                DoudizhuPlayer player = players.get(i);
                player.setHandCards(hands.get(i));
                log.debug("玩家 {} 获得 {} 张手牌", player.getUserId(), hands.get(i).size());
            }

            // 设置底牌
            gameState.setLandlordExtraCards(landlordCards);
            log.info("底牌: {}", landlordCards);

            // ==================== 2. 初始化叫地主管理器 ====================
            // 注意：BiddingManager 构造函数需要 DoudizhuRoom 和 DoudizhuGameState
            BiddingManager biddingManager = new BiddingManager(doudizhuRoom);
            doudizhuRoom.setBiddingManager(biddingManager);

            // ==================== 3. 更新房间状态为叫地主阶段 ====================
            doudizhuRoom.updateGameStatus(DoudizhuGameStatus.BIDDING);

            // ==================== 4. 发布游戏开始事件（供机器人服务订阅） ====================
            EventPublisher.fire(ctx, () -> new GameStartEvent(
                    GameType.DOUDIZHU,
                    String.valueOf(room.getRoomId()),
                    ((DoudizhuRoom) room).getAllDoudizhuPlayers().stream().map(DoudizhuPlayer::getUserId).collect(Collectors.toList()),
                    0
            ));

            // ==================== 5. 广播游戏开始给房间内所有玩家 ====================
            DoudizhuBroadcastKit.broadcastGameStart(doudizhuRoom);

            // ==================== 6. 启动叫地主流程 ====================
            biddingManager.start();

            log.info("房间 {} 游戏开始，进入叫地主阶段", doudizhuRoom.getRoomId());
        });
    }

    /**
     * 构建发牌上下文（包含玩家行为数据，用于智能发牌）
     */
    private MultiPlayerDealContext buildDealContext(DoudizhuRoom room, List<Long> playerIds) {
        List<Integer> vipLevels = new ArrayList<>();
        List<Integer> lossCounts = new ArrayList<>();
        List<Integer> winCounts = new ArrayList<>();
        List<Boolean> rookieFlags = new ArrayList<>();
        List<Boolean> aiFlags = new ArrayList<>();

        for (Long userId : playerIds) {
            DoudizhuPlayer player = room.getDoudizhuPlayer(userId);
            // TODO: 从 service-user 或统计数据中获取真实行为数据
            // 示例：暂时使用默认值
            vipLevels.add(0);
            lossCounts.add(0);
            winCounts.add(0);
            rookieFlags.add(false);
            aiFlags.add(player.isRobot());
        }

        // 地主索引暂时为0（发牌时还未确定地主），DoudizhuDealer 会根据玩家行为数据智能分配牌
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