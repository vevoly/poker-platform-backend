package com.pokergame.common.cmd;

import com.pokergame.common.cmd.main.MainCmd;

public interface RobotCmd {

    /** WS模块 - 主cmd */
    int CMD = MainCmd.ROBOT_CMD;

    /** 获取机器人账号 */
    int GET_ROBOT_ACCOUNTS = 1;

}