package com.pokergame.common.event;

/**
 * 游戏事件类型枚举
 *
 * 设计原则：
 * - 按功能模块划分
 * - 预留扩展空间
 *
 * @author poker-platform
 */
public enum GameEventType {

    // ========== 通用事件（所有游戏共用） ==========
    GAME_START(1000, "游戏开始"),
    GAME_END(1001, "游戏结束"),
    PLAYER_JOIN(1002, "玩家加入"),
    PLAYER_LEAVE(1003, "玩家离开"),
    PLAYER_READY(1004, "玩家准备"),

    // ========== 斗地主事件 ==========
    DOUDIZHU_PLAY_CARD(2000, "出牌"),
    DOUDIZHU_GRAB_LANDLORD(2001, "抢地主"),
    DOUDIZHU_BOMB(2002, "炸弹"),
    DOUDIZHU_ROCKET(2003, "王炸"),
    DOUDIZHU_SPRING(2004, "春天"),
    DOUDIZHU_COUNTER_SPRING(2005, "反春"),

    // ========== 德州扑克事件 ==========
    TEXAS_BET(3000, "下注"),
    TEXAS_FOLD(3001, "弃牌"),
    TEXAS_ALL_IN(3002, "All-in"),
    TEXAS_SHOWDOWN(3003, "摊牌"),

    // ========== 牛牛事件 ==========
    BULL_BET(4000, "下注"),
    BULL_SHOWDOWN(4001, "开牌"),
    BULL_FIVE_SMALL(4002, "五小牛"),
    BULL_FOUR_BOMB(4003, "四炸");

    private final int code;
    private final String name;

    GameEventType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() { return code; }
    public String getName() { return name; }
}
