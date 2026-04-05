package com.pokergame.common.cmd;

public interface UserCmd {

    /** 用户模块 - 主cmd */
    int CMD = GameModuleCmd.USER_CMD;

    /** 用户登录 */
    int LOGIN = 1;

    /** 用户注册 */
    int REGISTER = 2;

    /** 获取用户信息 */
    int GET_USER_INFO = 3;

    /** 扣除金币 */
    int DEDUCT_GOLD = 4;

    /** 增加金币 */
    int ADD_GOLD = 5;

    /** 检查金币 */
    int CHECK_GOLD = 6;
}