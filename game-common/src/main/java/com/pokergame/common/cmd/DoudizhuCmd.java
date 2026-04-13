package com.pokergame.common.cmd;

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
    int cmd = ModuleCmd.DOUDIZHU_CMD;

    // ========== 房间操作 (1-9) ==========

    /** 创建房间 */
    int CREATE_ROOM = 1;

    /** 加入房间 */
    int JOIN_ROOM = 2;

    /** 离开房间 */
    int LEAVE_ROOM = 3;

    /** 获取房间列表 */
    int ROOM_LIST = 4;

    /** 获取房间详情 */
    int ROOM_DETAIL = 5;

    // ========== 游戏操作 (10-19) ==========

    /** 玩家准备 */
    int READY = 10;

    /** 开始游戏 */
    int START_GAME = 11;

    /** 抢地主 */
    int GRAB_LANDLORD = 12;

    /** 不抢地主 */
    int NOT_GRAB = 13;

    /** 出牌 */
    int PLAY_CARD = 14;

    /** 过牌 */
    int PASS = 15;

    // ========== 广播路由 (100-199) ==========

    /** 准备状态广播 */
    int READY_BROADCAST = 100;

    /** 玩家进入房间广播 */
    int ENTER_ROOM_BROADCAST = 101;

    /** 玩家离开房间广播 */
    int QUIT_ROOM_BROADCAST = 102;

    /** 游戏开始广播 */
    int GAME_START_BROADCAST = 103;

    /** 叫地主回合广播 */
    int BIDDING_TURN_BROADCAST = 104;

    /** 发牌广播 */
    int DEAL_CARDS_BROADCAST = 105;

    /** 叫地主广播 */
    int GRAB_LANDLORD_BROADCAST = 106;

    /** 不叫地主广播 */
    int NOT_GRAB_LANDLORD_BROADCAST = 107;

    /** 出牌广播 */
    int PLAY_CARD_BROADCAST = 108;

    /** 过牌广播 */
    int PASS_BROADCAST = 109;

    /** 游戏结束广播 */
    int GAME_END_BROADCAST = 110;

}
