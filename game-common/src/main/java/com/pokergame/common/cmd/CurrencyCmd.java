package com.pokergame.common.cmd;

import com.pokergame.common.cmd.main.MainCmd;

/**
 * 货币模块命令
 */
public interface CurrencyCmd {

    /** Token模块 - 主cmd */
    int CMD = MainCmd.CURRENCY_CMD;

    /** 获取货币 */
    int GET_CURRENCY = 1;

    /** 增加 */
    int INCREASE = 2;

    /** 减少 */
    int DECREASE = 3;

    /** 获取帐变日志 */
    int GET_LOG = 4;
}
