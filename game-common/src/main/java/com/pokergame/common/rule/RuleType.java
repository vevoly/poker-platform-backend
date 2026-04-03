package com.pokergame.common.rule;

/**
 * 规则类型枚举 - 无状态
 *
 * @author poker-platform
 */
public enum RuleType {

    /** 首出限制 */
    FIRST_PLAY_LIMIT,

    /** 炸弹倍率 */
    BOMB_MULTIPLIER,

    /** 春天规则 */
    SPRING_RULE,

    /** 反春规则 */
    COUNTER_SPRING_RULE,

    /** 明牌规则 */
    SHOW_CARD_RULE,

    /** 托管规则 */
    AUTO_PLAY_RULE;
}
