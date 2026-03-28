package com.pokergame.common.cmd;

public interface GameModuleCmd {
    /** 用户模块 - 主cmd: 1 */
    int USER_MODULE_CMD = 1;

    /** 大厅模块 - 主cmd: 2 */
    int HALL_MODULE_CMD = 2;

    /** 游戏模块 - 主cmd: 3 */
    int GAME_MODULE_CMD = 3;

    /** 聊天模块 - 主cmd: 4 */
    int CHAT_MODULE_CMD = 4;

    /** 联盟模块 - 主cmd: 5 */
    int ALLIANCE_MODULE_CMD = 5;
}
