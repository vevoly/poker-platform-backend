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

    // ========== 广播路由 (100-199) ==========

    /** 叫地主回合广播 */
    int BIDDING_TURN_BROADCAST = 100;

    /** 发牌广播 */
    int DEAL_CARDS_BROADCAST = 101;

    /** 叫地主广播 */
    int GRAB_LANDLORD_BROADCAST = 102;

    /** 不叫地主广播 */
    int NOT_GRAB_LANDLORD_BROADCAST = 103;

    /** 出牌广播 */
    int PLAY_CARD_BROADCAST = 104;

    /** 过牌广播 */
    int PASS_BROADCAST = 105;

    /** 游戏结束广播 */
    int GAME_END_BROADCAST = 106;

}
