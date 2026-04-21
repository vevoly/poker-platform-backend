package com.pokergame.core.room;

import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.pokergame.common.exception.GameCode;
import com.pokergame.common.model.player.PlayerInfo;
import com.pokergame.common.model.room.JoinRoomResp;
import com.pokergame.core.base.BasePlayer;
import com.pokergame.core.base.BaseRoom;
import com.pokergame.core.base.BaseRoomService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 房间操作公共工具类
 * 提供加入房间、离开房间、准备、开始游戏等所有游戏通用的操作。
 * 注意：创建房间因各游戏差异较大（可能需要不同的初始化逻辑），
 * 不在此工具类中实现，由各游戏自己的 Action 完成。
 *
 * @author poker-platform
 */
@Slf4j
public final class RoomOperations {

    private RoomOperations() {}

    /**
     * 加入房间
     *
     * @param ctx            FlowContext（用于获取当前用户ID）
     * @param roomId         要加入的房间ID
     * @param playerNickname 玩家昵称（如果为空，则使用 "玩家" + userId）
     * @param roomService    房间服务
     * @param afterJoin      加入房间后的回调（通常用于广播“玩家进入”消息），可为 null
     * @return 加入房间响应
     */
    public static JoinRoomResp joinRoom(FlowContext ctx,
                                        long roomId,
                                        String playerNickname,
                                        BaseRoomService roomService,
                                        Consumer<BaseRoom> afterJoin) {
        long userId = ctx.getUserId();

        // 1. 校验玩家是否已在其他房间
        BaseRoom existingRoom = roomService.getUserRoom(userId);
        GameCode.PLAYER_ALREADY_IN_ROOM.assertTrueThrows(existingRoom != null);

        // 2. 获取目标房间
        BaseRoom room = roomService.getRoom(roomId);
        GameCode.ROOM_NOT_FOUND.assertTrueThrows(room == null);

        // 3. 校验房间状态（未满且游戏未开始）
        GameCode.ROOM_FULL.assertTrueThrows(room.isFull());
        String status = room.getGameStatus();
        GameCode.ROOM_ALREADY_STARTED.assertTrueThrows(
                !"WAITING".equals(status) && !"READY".equals(status));

        // 4. 创建玩家对象
        if (playerNickname == null || playerNickname.isEmpty()) {
            playerNickname = "玩家" + userId;
        }
        BasePlayer player = new BasePlayer(userId, playerNickname);

        // 5. 加入房间
        room.addPlayer(player);
        roomService.addPlayer(room, player);

        log.info("玩家 {} 加入房间 roomId={}, 当前人数={}", userId, roomId, room.getPlayerCount());

        // 6. 执行回调（如广播）
        if (afterJoin != null) {
            afterJoin.accept(room);
        }

        // 7. 构建响应
        return new JoinRoomResp()
                .setRoomId(room.getRoomId())
                .setOwnerId(room.getOwnerId())
                .setMaxPlayers(room.getMaxPlayers())
                .setPlayerCount(room.getPlayerCount())
                .setGameStatus(room.getGameStatus())
                .setPlayers(buildPlayerInfoList(room));
    }

    /**
     * 离开房间
     *
     * @param ctx          FlowContext
     * @param roomService  房间服务
     * @param afterLeave   离开房间后的回调（通常用于广播），可为 null
     */
    public static void leaveRoom(FlowContext ctx,
                                 BaseRoomService roomService,
                                 Consumer<BaseRoom> afterLeave) {
        long userId = ctx.getUserId();
        BaseRoom room = roomService.getUserRoom(userId);
        GameCode.PLAYER_NOT_IN_ROOM.assertTrueThrows(room == null);

        // 移除玩家
        roomService.removePlayer(room, userId);
        log.info("玩家 {} 离开房间 roomId={}", userId, room.getRoomId());

        // 若房间为空，则销毁房间
        if (room.isEmpty()) {
            roomService.removeRoom(room);
            log.info("房间 roomId={} 已空，已销毁", room.getRoomId());
        }

        if (afterLeave != null) {
            afterLeave.accept(room);
        }
    }

    /**
     * 准备/取消准备
     *
     * @param ctx          FlowContext
     * @param isReady      是否准备（true=准备，false=取消准备）
     * @param roomService  房间服务
     * @param afterReady   准备后的回调（通常用于广播和更新玩家准备状态），不可为 null
     */
    public static void ready(FlowContext ctx,
                             boolean isReady,
                             BaseRoomService roomService,
                             Consumer<BaseRoom> afterReady) {
        long userId = ctx.getUserId();
        BaseRoom room = roomService.getUserRoom(userId);
        GameCode.PLAYER_NOT_IN_ROOM.assertTrueThrows(room == null);

        // 更新玩家准备状态（由回调完成，因为具体玩家类型可能不同）
        afterReady.accept(room);
    }

    /**
     * 开始游戏（仅房主可调用）
     *
     * @param ctx            FlowContext
     * @param roomService    房间服务
     * @param gameStarter    游戏开始时的回调（负责改变房间状态、初始化游戏管理器、发牌等）
     */
    public static void startGame(FlowContext ctx,
                                 BaseRoomService roomService,
                                 Consumer<BaseRoom> gameStarter) {
        long userId = ctx.getUserId();
        BaseRoom room = roomService.getUserRoom(userId);
        GameCode.PLAYER_NOT_IN_ROOM.assertTrueThrows(room == null);

        // 校验房主权限
        GameCode.NOT_ROOM_OWNER.assertTrueThrows(room.getOwnerId() != userId);

        // 校验所有玩家已准备
        GameCode.NOT_ALL_READY.assertTrueThrows(!room.isAllReady());

        // 执行游戏开始回调（初始化游戏管理器、发牌、发布事件等）
        gameStarter.accept(room);
    }

    // ========== 私有辅助方法 ==========

    private static List<PlayerInfo> buildPlayerInfoList(BaseRoom room) {
        return room.getPlayerMap().values().stream()
                .map(p -> {
                    BasePlayer bp = (BasePlayer) p;
                    return new PlayerInfo()
                            .setUserId(bp.getUserId())
                            .setNickname(bp.getNickname())
                            .setReady(bp.isReady())
                            .setCardCount(bp.getCardCount());
                })
                .collect(Collectors.toList());
    }
}
