package com.pokergame.common.cmd;

/**
 * 路由模块定义 - 统一管理所有模块的主路由值
 *
 * 遵循 ioGame 官方约定：
 * - 所有模块的主路由定义在一个接口中
 * - 命名统一为 CmdModule
 * - 主路由值范围：1-100 为系统模块，100-1000 为游戏模块
 *
 * @author poker-platform
 */
public interface ModuleCmd {

    // ========== 系统模块 (1-100) ==========

    /** WebSocket 模块 */
    int WS_CMD = 1;

    /** 认证模块 */
    int AUTH_CMD = 2;

    /** 用户服模块 */
    int USER_CMD = 3;

    /** 货币模块 */
    int CURRENCY_CMD = 4;

    /** 匹配服模块 */
    int MATCH_CMD = 5;

    /** 排行榜模块 */
    int RANK_CMD = 6;

    /** 聊天模块 */
    int CHAT_CMD = 7;

    /** 大厅模块 */
    int HALL_CMD = 8;

    /** 管理模块 */
    int ADMIN_CMD = 9;



    // ========== 游戏模块 (101-200) ==========

    /** 斗地主模块 */
    int DOUDIZHU_CMD = 101;

    /** 德州扑克模块 */
    int TEXAS_CMD = 102;

    /** 牛牛模块 */
    int BULL_CMD = 103;
}
