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

    /** 查询房间状态 */
    int ROOM_STATE = 6;

    // ========== 游戏流程操作 (10-15) ==========

    /** 准备 */
    int READY = 10;

    /** 开始游戏 */
    int START_GAME = 11;

    /** 托管请求 */
    int TRUSTEESHIP = 12;                        // 托管请求（客户端 -> 服务端）

}
