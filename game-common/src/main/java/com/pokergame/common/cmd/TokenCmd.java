package com.pokergame.common.cmd;

/**
 * Token 模块命令
 */
public interface TokenCmd {

    /** Token模块 - 主cmd */
    int CMD = GameModuleCmd.TOKEN_CMD;

    /** Token 刷新 */
    int REFRESH = 1;

    /** Token 验证 */
    int VERIFY = 2;

    /** Token 踢人 */
    int KICK = 3;
}
