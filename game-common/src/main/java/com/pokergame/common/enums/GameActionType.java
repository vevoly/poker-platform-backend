package com.pokergame.common.enums;

/**
 * 游戏动作类型枚举
 * 用于 GameActionEvent 中标识玩家执行的具体动作
 */
public enum GameActionType {
    // 通用动作
    PLAY("PLAY", "出牌"),
    PASS("PASS", "过牌"),
    FOLD("FOLD", "弃牌"),
    CHECK("CHECK", "让牌"),
    BET("BET", "下注"),
    RAISE("RAISE", "加注"),
    CALL("CALL", "跟注"),
    ALL_IN("ALL_IN", "全下"),

    // 斗地主专用
    GRAB_LANDLORD("GRAB_LANDLORD", "抢地主"),
    BID("BID", "叫分"),

    // 德州扑克专用
    SHOWDOWN("SHOWDOWN", "摊牌");

    private final String code;
    private final String desc;

    GameActionType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static GameActionType fromCode(String code) {
        for (GameActionType action : values()) {
            if (action.code.equals(code)) return action;
        }
        return null;
    }
}
