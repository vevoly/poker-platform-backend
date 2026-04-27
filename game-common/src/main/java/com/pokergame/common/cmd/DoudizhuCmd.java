package com.pokergame.common.cmd;

import com.pokergame.common.cmd.main.MainCmd;

/**
 * 斗地主路由定义
 *
 * 遵循 ioGame 官方约定：
 * - 以 xxxCmd 结尾
 * - 主路由统一命名为 cmd
 * - 子路由按功能模块划分区间
 *
 * 路由分配规则：
 * - 1-9: 房间操作
 * - 10-19: 游戏操作
 * - 100-199: 广播路由
 *
 * @author poker-platform
 */
public interface DoudizhuCmd {

    /** 主路由 */
    int CMD = MainCmd.DOUDIZHU_CMD;

    // ========== 游戏操作 (10-19) ==========

    /** 抢地主 */
    int GRAB_LANDLORD = 1;

    /** 不抢地主 */
    int NOT_GRAB = 2;

    /** 出牌 */
    int PLAY_CARD = 3;

    /** 过牌 */
    int PASS = 4;


}
