package com.pokergame.common.item;

/**
 * 道具效果类型枚举
 * 定义所有可能的效果类型，供各服务统一使用
 *
 * @author poker-platform
 */
public enum EffectType {

    /** 提高概率（加成类） */
    BOOST_PROBABILITY,

    /** 保底牌型（保底类） */
    GUARANTEE_RANK,

    /** 额外金币 */
    EXTRA_GOLD,

    /** 额外经验 */
    EXTRA_EXP,

    /** 减少消耗 */
    REDUCE_COST,

    /** 双倍奖励 */
    DOUBLE_REWARD;
}
