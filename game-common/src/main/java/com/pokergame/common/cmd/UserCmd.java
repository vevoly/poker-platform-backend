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

    // ========== 凭证验证 (10-19) ==========
    /** 验证用户凭证（供 Auth 服务调用） */
    int VERIFY_CREDENTIAL = 10;

    /** 处理登录成功（供 Auth 服务调用） */
    int PROCESS_LOGIN_SUCCESS = 11;

}