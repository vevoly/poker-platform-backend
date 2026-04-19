package com.pokergame.common.cmd;

public interface UserCmd {

    /** 用户模块 - 主cmd */
    int CMD = ModuleCmd.USER_CMD;

    // ========== 用户操作 (1-9) ==========
    /** 用户注册 */
    int REGISTER = 1;

    /** 获取用户信息 */
    int GET_USER_INFO = 2;

    /** 更新用户信息 */
    int UPDATE_USER_INFO = 3;

    /** 修改密码 */
    int CHANGE_PASSWORD = 4;

    // ========== 凭证验证 (10-15) ==========
    /** 验证用户凭证（供 Auth 服务调用） */
    int VERIFY_CREDENTIAL = 10;

    /** 处理登录成功（供 Auth 服务调用） */
    int PROCESS_LOGIN_SUCCESS = 11;

    // ========== 机器人操作 (16-29) ==========
    /** 获取机器人账号 */
    int GET_ROBOT_ACCOUNTS = 16;

}