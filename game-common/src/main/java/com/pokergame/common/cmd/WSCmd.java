package com.pokergame.common.cmd;

import com.pokergame.common.cmd.main.MainCmd;

public interface WSCmd {

    /** WS模块 - 主cmd */
    int CMD = MainCmd.WS_CMD;

    /** WS验证 */
    int LOGIN = 1;

}