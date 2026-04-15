package com.pokergame.game.doudizhu.action;

import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.iohao.game.widget.light.room.flow.RoomCreateContext;
import com.pokergame.common.cmd.DoudizhuCmd;
import com.pokergame.common.model.player.PlayerInfo;
import com.pokergame.common.model.room.*;
import com.pokergame.common.exception.GameCode;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.enums.InternalOperation;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 房间操作 Action
 *
 * 处理房间相关的客户端请求：
 * - 创建房间
 * - 加入房间
 * - 离开房间
 *
 * @author poker-platform
 */
@Slf4j
@ActionController(DoudizhuCmd.CMD)
public class RoomAction {

    private final DoudizhuRoomService roomService = DoudizhuRoomService.me();

    /**
     * 创建房间
     * 不需要广播，因为只有创建者自己
     * @param req 创建房间请求
     * @param ctx FlowContext
     * @return 房间信息
     */
    @ActionMethod(DoudizhuCmd.CREATE_ROOM)
    public CreateRoomResp createRoom(CreateRoomReq req, FlowContext ctx) {
        long userId = ctx.getUserId();

        // 检查玩家是否已在房间中
        DoudizhuRoom existingRoom = roomService.getUserRoom(userId);
        GameCode.PLAYER_ALREADY_IN_ROOM.assertTrueThrows(existingRoom != null);

        // 创建房间上下文
        var createContext = RoomCreateContext.of(userId)
                .setSpaceSize(req.getMaxPlayers());

        // 创建房间
        DoudizhuRoom room = roomService.createRoom(createContext);
        room.setOwnerId(userId);
        room.setMaxPlayers(req.getMaxPlayers());

        // 创建玩家
        DoudizhuPlayer player = new DoudizhuPlayer(userId, req.getPlayerName());

        // 加入房间
        room.addDoudizhuPlayer(player);
        roomService.addRoom(room);
        roomService.addPlayer(room, player);

        log.info("玩家 {} 创建房间: roomId={}, maxPlayers={}", userId, room.getRoomId(), req.getMaxPlayers());

        // 返回响应
        return new CreateRoomResp()
                .setRoomId(room.getRoomId())
                .setOwnerId(room.getOwnerId())
                .setMaxPlayers(room.getMaxPlayers())
                .setPlayerCount(room.getPlayerCount())
                .setGameStatus(room.getGameStatus().name())
                .setRoomName(req.getRoomName());
    }

    /**
     * 加入房间
     * 需要触发 OperationHandler 来广播
     * @param req 加入房间请求
     * @param ctx FlowContext
     * @return 房间信息
     */
    @ActionMethod(DoudizhuCmd.JOIN_ROOM)
    public JoinRoomResp joinRoom(JoinRoomReq req, FlowContext ctx) {
        long userId = ctx.getUserId();
        log.info("加入房间请求: userId={}, roomId={}", userId, req.getRoomId());
        // 检查玩家是否已在房间中
        DoudizhuRoom existingRoom = roomService.getUserRoom(userId);
        GameCode.PLAYER_ALREADY_IN_ROOM.assertTrueThrows(existingRoom != null);

        // 获取目标房间
        DoudizhuRoom room = roomService.getDoudizhuRoom(req.getRoomId());
        GameCode.ROOM_NOT_FOUND.assertTrueThrows(room == null);

        // 检查房间是否已满
        GameCode.ROOM_FULL.assertTrueThrows(room.isFull());

        // 检查游戏是否已开始
        DoudizhuGameStatus status = room.getGameStatus();
        GameCode.ROOM_ALREADY_STARTED.assertTrueThrows(
                status != DoudizhuGameStatus.WAITING && status != DoudizhuGameStatus.READY
        );

        // 创建玩家
        DoudizhuPlayer player = new DoudizhuPlayer(userId, req.getPlayerName());

        // 加入房间
        room.addDoudizhuPlayer(player);
        roomService.addPlayer(room, player);

        log.info("玩家 {} 加入房间: roomId={}", userId, room.getRoomId());

        // 触发 OperationHandler 来广播玩家进入
        room.operation(InternalOperation.ENTER_ROOM);

        // 构建玩家列表
        List<PlayerInfo> players = room.getAllDoudizhuPlayers().stream()
                .map(p -> new PlayerInfo()
                        .setUserId(p.getUserId())
                        .setNickname(p.getNickname())
                        .setLandlord(p.isLandlord())
                        .setCardCount(p.getCardCount()))
                .collect(Collectors.toList());

        // 返回响应
        return new JoinRoomResp()
                .setRoomId(room.getRoomId())
                .setOwnerId(room.getOwnerId())
                .setMaxPlayers(room.getMaxPlayers())
                .setPlayerCount(room.getPlayerCount())
                .setPlayers(players)
                .setGameStatus(room.getGameStatus().name());
    }

    /**
     * 离开房间
     * 需要触发 OperationHandler 来广播
     * @param req 离开房间请求
     * @param ctx FlowContext
     */
    @ActionMethod(DoudizhuCmd.LEAVE_ROOM)
    public void leaveRoom(LeaveRoomReq req, FlowContext ctx) {
        long userId = ctx.getUserId();

        DoudizhuRoom room = roomService.getUserRoom(userId);
        GameCode.PLAYER_NOT_IN_ROOM.assertTrueThrows(room == null);

        log.info("玩家 {} 离开房间: roomId={}", userId, room.getRoomId());

        // 触发 OperationHandler 处理离开逻辑（移除玩家、广播等）
        room.operation(InternalOperation.QUIT_ROOM);

        return;
    }
}