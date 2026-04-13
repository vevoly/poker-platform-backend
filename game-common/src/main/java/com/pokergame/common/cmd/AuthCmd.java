package com.pokergame.common.cmd;

public interface AuthCmd {

    int CMD = ModuleCmd.AUTH_CMD;

    // ========== 登录认证 (1-9) ==========
    /** 用户名密码登录 */
    int PASSWORD_LOGIN = 1;

    /** 短信验证码登录 */
    int SMS_LOGIN = 2;

    /** 三方登录（微信/QQ/Google） */
    int OAUTH_LOGIN = 3;

    // ========== Token 管理 (10-19) ==========
    /** 生成 Token */
    int CREATE_TOKEN = 10;

    /** 验证 Token */
    int VERIFY_TOKEN = 11;

    /** 刷新 Token */
    int REFRESH_TOKEN = 12;

    /** 登出（注销 Token） */
    int LOGOUT = 13;

    /** 踢用户下线 */
    int KICK_USER = 14;

}
