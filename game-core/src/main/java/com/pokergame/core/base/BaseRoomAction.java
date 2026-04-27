package com.pokergame.core.base;

import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.pokergame.common.exception.GameCode;
import com.pokergame.common.model.game.StartGameReq;
import com.pokergame.common.model.room.*;
import com.pokergame.core.room.RoomOperations;
import lombok.extern.slf4j.Slf4j;

/**
 * 房间操作基类（所有游戏共用）
 * 提供房间操作的完整实现，子类只需：
 * <ul>
 *     <li>实现抽象方法 {@link #getRoomService()}, {@link #getNickname(FlowContext)}, {@link #createPlayer(long, String)}</li>
 *     <li>实现 {@link #doStartGame(BaseRoom, FlowContext)} 和 {@link #buildRoomState(BaseRoom)}</li>
 *     <li>可选重写钩子方法 {@link #afterJoin(BaseRoom, FlowContext)} 等来控制广播</li>
 *     <li>在子类中暴露带 {@code @ActionMethod} 注解的公开方法，内部调用父类对应方法</li>
 * </ul>
 *
 * @param <R> 房间类型（继承 BaseRoom）
 */
@Slf4j
public abstract class BaseRoomAction<R extends BaseRoom> {

    // ========== 抽象方法（子类必须实现） ==========

    /**
     * 获取房间服务
     * @return
     */
    protected abstract BaseRoomService getRoomService();

    /**
     * 获取用户昵称
     * @param ctx
     * @return
     */
    protected abstract String getNickname(FlowContext ctx);

    /**
     * 创建玩家
     * @param userId
     * @param nickname
     * @return
     */
    protected abstract BasePlayer createPlayer(long userId, String nickname);

    /**
     * 开始游戏
     * @param room
     * @param ctx
     */
    protected abstract void doStartGame(R room, FlowContext ctx);

    /**
     * 构建房间状态
     * @param room
     * @return
     */
    protected abstract RoomStateDTO buildRoomState(R room, long userId);

    // ========== 钩子方法（子类可选重写，用于广播等） ==========

    /**
     * 玩家加入房间后的回调
     * @param room
     * @param ctx
     */
    protected void afterJoin(R room, FlowContext ctx) { }

    /**
     * 玩家离开房间后的回调
     * @param room
     * @param ctx
     */
    protected void afterLeave(R room, FlowContext ctx) { }

    /**
     * 玩家准备后的回调
     * @param room
     * @param ctx
     * @param isReady
     */
    protected void afterReady(R room, FlowContext ctx, boolean isReady) { }

    // ========== 房间操作的默认实现（供子类调用） ==========

    /**
     * 获取用户所在的房间
     * @param userId
     * @return
     */
    protected R getUserRoom(long userId) {
        return (R) getRoomService().getUserRoom(userId);
    }

    /**
     * 创建房间（子类需自己实现，因为各游戏差异大）
     */
    public abstract CreateRoomResp createRoom(CreateRoomReq req, FlowContext ctx);

    /**
     * 加入房间（默认实现，内部调用 afterJoin 钩子）
     */
    public JoinRoomResp joinRoom(JoinRoomReq req, FlowContext ctx) {
        String nickname = getNickname(ctx);
        if (nickname == null || nickname.isEmpty()) {
            nickname = "玩家" + ctx.getUserId();
        }
        String finalNickname = nickname;
        return RoomOperations.joinRoom(
                ctx,
                req.getRoomId(),
                getRoomService(),
                userId -> createPlayer(userId, finalNickname),
                room -> afterJoin((R) room, ctx)
        );
    }

    /**
     * 离开房间（默认实现，内部调用 afterLeave 钩子）
     */
    public void leaveRoom(LeaveRoomReq req, FlowContext ctx) {
        RoomOperations.leaveRoom(ctx, getRoomService(), room -> afterLeave((R) room, ctx));
    }

    /**
     * 准备（默认实现，内部调用 afterReady 钩子）
     */
    public void ready(ReadyRoomReq req, FlowContext ctx) {
        RoomOperations.ready(ctx, req.isReady(), getRoomService(), room -> afterReady((R) room, ctx, req.isReady()));
    }

    /**
     * 开始游戏（默认实现，内部调用 doStartGame）
     */
    public void startGame(StartGameReq req, FlowContext ctx) {
        RoomOperations.startGame(ctx, getRoomService(), room -> {
            @SuppressWarnings("unchecked")
            R r = (R) room;
            doStartGame(r, ctx);
        });
    }

    /**
     * 托管
     */
    public void trusteeship(TrusteeshipReq req, FlowContext ctx) {
        long userId = ctx.getUserId();
        R room = getUserRoom(userId);
        GameCode.PLAYER_NOT_IN_ROOM.assertTrueThrows(room == null);
        room.getTrusteeshipManager().setTrustee(userId, req.isTrusteeship());
        if (req.isTrusteeship() && room.isCurrentPlayer(userId)) {
            room.getTrusteeshipManager().autoAct(userId);
        }
    }

    /**
     * 房间状态查询
     */
    public RoomStateDTO roomState(FlowContext ctx) {
        long userId = ctx.getUserId();
        R room = getUserRoom(userId);
        GameCode.PLAYER_NOT_IN_ROOM.assertTrueThrows(room == null);
        return buildRoomState(room, userId);
    }
}