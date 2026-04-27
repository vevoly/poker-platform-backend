package com.pokergame.test.region;

import com.iohao.game.external.client.AbstractInputCommandRegion;
import com.iohao.game.external.client.command.CallbackDelegate;
import com.pokergame.common.cmd.RoomCmd;
import com.pokergame.common.model.broadcast.*;
import com.pokergame.common.model.room.PlayerStateDTO;
import com.pokergame.common.model.room.RoomStateDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseInputCommandRegion extends AbstractInputCommandRegion {

    // 子类需要维护 currentRoomId（可以通过静态变量或 getter/setter）
    protected abstract String getCurrentRoomId();
    protected abstract void setCurrentRoomId(String roomId);

    /**
     * 注册公共广播监听（由子类在 initInputCommand 中调用）
     */
    protected void listenPublicBroadcast() {
        // 游戏开始广播
        ofListen((CallbackDelegate) result -> {
            GameStartBroadcastData data = result.getValue(GameStartBroadcastData.class);
            log.info("🎮 收到游戏开始广播: roomId={}, players={}", data.getRoomId(), data.getPlayers());
        }, RoomCmd.GAME_START_BROADCAST, "游戏开始广播");

        // 玩家进入房间广播
        ofListen((CallbackDelegate) result -> {
            EnterRoomBroadcastData data = result.getValue(EnterRoomBroadcastData.class);
            log.info("🚪 收到玩家进入广播: userId={}, nickname={}", data.getUserId(), data.getNickname());
        }, RoomCmd.ENTER_ROOM_BROADCAST, "玩家进入广播");

        // 玩家离开房间广播
        ofListen((CallbackDelegate) result -> {
            QuitRoomBroadcastData data = result.getValue(QuitRoomBroadcastData.class);
            log.info("🚶 收到玩家离开广播: userId={}", data.getUserId());
        }, RoomCmd.QUIT_ROOM_BROADCAST, "玩家离开广播");

        // 准备状态广播
        ofListen((CallbackDelegate) result -> {
            ReadyBroadcastData data = result.getValue(ReadyBroadcastData.class);
            log.info("✅ 收到准备状态广播: userId={}, ready={}, nickname={}",
                    data.getUserId(), data.isReady(), data.getNickname());
        }, RoomCmd.READY_BROADCAST, "准备状态广播");

        // 托管变更广播
        ofListen((CallbackDelegate) result -> {
            TrusteeshipChangeBroadcastData data = result.getValue(TrusteeshipChangeBroadcastData.class);
            log.info("🕹️ 收到托管变更广播: userId={}, isTrustee={}",
                    data.getUserId(), data.isTrusteeship());
        }, RoomCmd.TRUSTEESHIP_CHANGE_BROADCAST, "托管变更广播");
    }

    /**
     * 查询并打印当前房间状态
     */
    protected void printRoomState() {
        String roomId = getCurrentRoomId();
        if (roomId == null) {
            log.info("未进入任何房间，无法查询状态");
            return;
        }
        // 发送 ROOM_STATE 请求
        ofCommand(RoomCmd.ROOM_STATE)
                .setRequestData(() -> {
                    // 无需请求体，或者传入空对象
                    return new Object();
                })
                .callback(result -> {
                    RoomStateDTO state = result.getValue(RoomStateDTO.class);
                    log.info("===== 房间状态 (roomId={}) =====", state.getRoomId());
                    log.info("房主: {}, 状态: {}, 最大人数: {}", state.getOwnerId(), state.getGameStatus(), state.getMaxPlayers());
                    for (PlayerStateDTO p : state.getPlayers()) {
                        log.info("  玩家: userId={}, 昵称={}, 准备={}, 地主={}, 托管={}",
                                p.getUserId(), p.getNickname(), p.isReady(), p.isLandlord(), p.isTrusteeship());
                    }
                    log.info("===============================");
                });
    }
}
