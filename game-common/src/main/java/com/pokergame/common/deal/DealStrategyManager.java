package com.pokergame.common.deal;

import com.pokergame.common.deal.strategy.*;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 发牌策略组合管理器
 *
 * 负责：策略优先级排序、策略组合、最终牌型决策
 *
 * @author poker-platform
 */
@Slf4j
public class DealStrategyManager {

    private final GameType gameType;
    private final List<DealStrategy> strategies;

    public DealStrategyManager(GameType gameType) {
        this.gameType = gameType;
        this.strategies = new ArrayList<>();
        initStrategies();
    }

    private void initStrategies() {
        // 按优先级添加策略（高优先级在前）
        strategies.add(new NormalDistributionStrategy(gameType));  // 基础概率控制
        strategies.add(new CompensationStrategy(gameType));        // 连败补偿
        strategies.add(new WinningBalanceStrategy(gameType));      // 连胜平衡
        strategies.add(new VipDealStrategy(gameType));             // VIP特权
        strategies.add(new ReturnBonusStrategy(gameType));         // 回归奖励
        strategies.add(new EventDealStrategy(gameType, 0.2));      // 活动加成
        strategies.add(new GuaranteeDealStrategy(gameType, null)); // 保底策略
        strategies.add(new ItemBoostStrategy(gameType));           // 道具加成
        strategies.add(new RookieDealStrategy(gameType));          // 新手保护
        strategies.add(new AIDealStrategy(gameType, 5));           // AI难度
    }

    /**
     * 获取最终的目标牌型
     */
    public HandRank getTargetRank(long playerId, int consecutiveLosses, int consecutiveWins, int vipLevel) {
        HandRank targetRank = null;

        for (DealStrategy strategy : strategies) {
            if (!strategy.isEnabled()) {
                continue;
            }

            HandRank rank = null;

            if (strategy instanceof CompensationStrategy) {
                rank = ((CompensationStrategy) strategy).getTargetRankByLossCount(consecutiveLosses);
            } else if (strategy instanceof WinningBalanceStrategy) {
                // 连胜平衡策略不需要单独调用，在发牌后调整
                continue;
            } else if (strategy instanceof VipDealStrategy) {
                rank = ((VipDealStrategy) strategy).getTargetRank(playerId, vipLevel);
            } else if (strategy instanceof ReturnBonusStrategy) {
                rank = ((ReturnBonusStrategy) strategy).getTargetRank(playerId);
            } else if (strategy instanceof ItemBoostStrategy) {
                rank = ((ItemBoostStrategy) strategy).getTargetRank(playerId);
            } else if (strategy instanceof NormalDistributionStrategy) {
                rank = strategy.getTargetRank(0);
            } else {
                rank = strategy.getTargetRank(0);
            }

            if (rank != null) {
                targetRank = rank;
                log.debug("策略[{}]提供目标牌型: {}", strategy.getName(), rank.getName());
                break;  // 优先级最高的策略生效
            }
        }

        return targetRank;
    }

    /**
     * 调整牌型（用于连胜平衡）
     */
    public HandRank adjustRank(HandRank originalRank, int consecutiveWins) {
        for (DealStrategy strategy : strategies) {
            if (strategy instanceof WinningBalanceStrategy && strategy.isEnabled()) {
                return ((WinningBalanceStrategy) strategy).getAdjustedRank(originalRank, consecutiveWins);
            }
        }
        return originalRank;
    }
}
