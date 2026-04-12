package com.pokergame.common.cmd;

public interface UserCmd {

    /** 用户模块 - 主cmd */
    int CMD = GameModuleCmd.USER_CMD;

    /** 用户注册 */
    int REGISTER = 1;

    /** 用户登录 */
    int LOGIN = 2;

    /** 获取用户信息 */
    int GET_USER_INFO = 3;

    /** 用户登出 */
    int LOGOUT = 4;

}