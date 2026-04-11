package com.pokergame.common.enums;

import lombok.Getter;

/**
 * 货币变更类型枚举
 *
 * @author poker-platform
 */
@Getter
public enum ChangeCurrencyType {

    /** 游戏胜利 */
    GAME_WIN("GAME_WIN", "游戏胜利"),

    /** 游戏失败 */
    GAME_LOSE("GAME_LOSE", "游戏失败"),

    /** 游戏平局 */
    GAME_DRAW("GAME_DRAW", "游戏平局"),

    /** 充值 */
    RECHARGE("RECHARGE", "充值"),

    /** 赠送 */
    GIFT("GIFT", "赠送"),

    /** 购买道具 */
    BUY_ITEM("BUY_ITEM", "购买道具"),

    /** 兑换 */
    EXCHANGE("EXCHANGE", "兑换"),

    /** 系统发放 */
    SYSTEM("SYSTEM", "系统发放"),

    /** 每日奖励 */
    DAILY_REWARD("DAILY_REWARD", "每日奖励"),

    /** 任务奖励 */
    QUEST_REWARD("QUEST_REWARD", "任务奖励"),

    /** 退款 */
    REFUND("REFUND", "退款"),

    /** 管理后台操作 */
    ADMIN("ADMIN", "管理后台操作");

    private final String code;
    private final String desc;

    ChangeCurrencyType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static ChangeCurrencyType fromCode(String code) {
        for (ChangeCurrencyType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
