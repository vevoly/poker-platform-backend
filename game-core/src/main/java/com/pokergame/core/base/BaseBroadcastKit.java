package com.pokergame.core.base;

import com.iohao.game.action.skeleton.core.CmdInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用广播工具，提供所有游戏共用的广播方法。
 * 各游戏可在此基础上封装自己的广播工具。
 */
@Slf4j
public final class BaseBroadcastKit {

    private BaseBroadcastKit() {}

    /**
     * 向房间内所有玩家广播消息
     *
     * @param room    房间对象（必须继承 BaseRoom，能获取 aggregationContext）
     * @param cmd     主命令号（通常使用 RoomCmd.CMD 或各游戏自己的 CMD）
     * @param subCmd  子命令号（如 RoomCmd.READY_BROADCAST）
     * @param data    广播数据（建议继承 BaseBroadcastData 并包含 gameType）
     */
    public static void broadcastToRoom(BaseRoom room, int cmd, int subCmd, Object data) {
        if (room == null || room.getAggregationContext() == null) {
            log.warn("房间或广播上下文为空，无法发送广播");
            return;
        }
        CmdInfo cmdInfo = CmdInfo.of(cmd, subCmd);
        room.getAggregationContext().broadcast(cmdInfo, data);
        log.debug("广播消息: cmd={}, subCmd={}, roomId={}, data={}", cmd, subCmd, room.getRoomId(), data);
    }

    /**
     * 向房间内指定玩家广播消息
     *
     * @param room    房间对象
     * @param userId  目标玩家ID
     * @param cmd     主命令号
     * @param subCmd  子命令号
     * @param data    广播数据
     */
    public static void broadcastToUser(BaseRoom room, long userId, int cmd, int subCmd, Object data) {
        if (room == null || room.getAggregationContext() == null) {
            log.warn("房间或广播上下文为空，无法发送广播");
            return;
        }
        CmdInfo cmdInfo = CmdInfo.of(cmd, subCmd);
        room.getAggregationContext().broadcast(cmdInfo, data, userId);
        log.debug("广播消息给用户: userId={}, cmd={}, subCmd={}, data={}", userId, cmd, subCmd, data);
    }

    /**
     * 向房间内所有玩家广播消息（带排除用户）
     *
     * @param room         房间对象
     * @param excludeUserId 排除的玩家ID
     * @param cmd          主命令号
     * @param subCmd       子命令号
     * @param data         广播数据
     */
    public static void broadcastToRoomExcept(BaseRoom room, long excludeUserId, int cmd, int subCmd, Object data) {
        if (room == null || room.getAggregationContext() == null) {
            log.warn("房间或广播上下文为空，无法发送广播");
            return;
        }
        CmdInfo cmdInfo = CmdInfo.of(cmd, subCmd);
        room.getAggregationContext().broadcast(cmdInfo, data, excludeUserId);
        log.debug("广播消息（排除用户 {}）: cmd={}, subCmd={}, roomId={}", excludeUserId, cmd, subCmd, room.getRoomId());
    }
}
