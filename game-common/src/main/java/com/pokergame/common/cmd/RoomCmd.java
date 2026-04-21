package com.pokergame.common.cmd;

import com.pokergame.common.cmd.main.MainCmd;

public interface RoomCmd {

    /** 主路由 */
    int CMD = MainCmd.ROOM_CMD;

    /** 创建房间 */
    int CREATE_ROOM = 1;

    /** 加入房间 */
    int JOIN_ROOM = 2;

    /** 离开房间 */
    int LEAVE_ROOM = 3;

    /** 获取房间列表 */
    int ROOM_LIST = 4;

    /** 获取房间详情 */
    int ROOM_DETAIL = 5;

    // ========== 游戏流程操作 (10-19) ==========

    /** 准备 */
    int READY = 10;

    /** 开始游戏 */
    int START_GAME = 11;

    // ========== 广播路由 (100-199) ==========

    /** 准备状态广播 */
    int READY_BROADCAST = 100;

    /** 玩家进入房间广播 */
    int ENTER_ROOM_BROADCAST = 101;

    /** 玩家离开房间广播 */
    int QUIT_ROOM_BROADCAST = 102;

    /** 游戏开始广播 */
    int GAME_START_BROADCAST = 103;
}
