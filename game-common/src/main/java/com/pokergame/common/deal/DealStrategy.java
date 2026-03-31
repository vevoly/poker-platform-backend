package com.pokergame.common.deal;

import com.pokergame.common.game.GameType;

import java.util.List;

/**
 * 发牌策略接口
 * 用于控制发牌概率，实现新手保护、活动加成等功能
 *
 * @author poker-platform
 */
public interface DealStrategy {

    /** 策略名称 */
    String getName();

    /** 是否启用 */
    boolean isEnabled();

    /** 目标游戏类型 */
    GameType getGameType();

    /**
     * 获取目标牌型 - 所有需要的数据通过参数传入
     * @param context 发牌上下文（包含玩家所有相关数据）
     */
    HandRank getTargetRank(DealContext context);


    /**
     * 获取需要特殊处理的玩家索引
     * @param playerCount 总玩家数
     * @return 需要特殊处理的玩家索引列表
     */
    List<Integer> getSpecialPlayerIndices(int playerCount);

    /**
     * 是否对指定玩家生效
     */
    default boolean shouldApply(int playerIndex) {
        return getSpecialPlayerIndices(10).contains(playerIndex);
    }

    /**
     * 获取权重因子（用于概率计算）
     */
    default double getWeightFactor() {
        return 1.0;
    }
}
