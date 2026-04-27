package com.pokergame.common.cmd;

import com.pokergame.common.cmd.main.MainCmd;

public interface WSCmd {

    /** WS模块 - 主cmd */
    int CMD = MainCmd.WS_CMD;

    /** WS验证 */
    int LOGIN = 1;


    // ========== 广播路由 (100-199) ==========

    /** 准备状态广播 */
    int READY_BROADCAST = 100;

    /** 玩家进入房间广播 */
    int ENTER_ROOM_BROADCAST = 101;

    /** 玩家离开房间广播 */
    int QUIT_ROOM_BROADCAST = 102;

    /** 游戏开始广播 */
    int GAME_START_BROADCAST = 103;

    /** 托管状态变更广播 */
    int TRUSTEESHIP_CHANGE_BROADCAST = 104;      // 托管状态变更广播（服务端 -> 客户端）

    /** 发牌广播 */
    int DEAL_CARDS_BROADCAST = 110;

    /** 叫地主广播 */
    int GRAB_LANDLORD_BROADCAST = 111;

    /** 不叫地主广播 */
    int NOT_GRAB_LANDLORD_BROADCAST = 112;

    /** 下注广播 */
    int BET_BROADCAST = 113;

    /** 回合广播 */
    int TURN_BROADCAST = 114;

    /** 出牌广播 */
    int PLAY_CARD_BROADCAST = 115;

    /** 过牌广播 */
    int PASS_BROADCAST = 116;

    /** 游戏结束广播 */
    int GAME_END_BROADCAST = 117;

}