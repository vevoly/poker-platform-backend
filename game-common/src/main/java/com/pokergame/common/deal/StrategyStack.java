package com.pokergame.common.deal;

import com.pokergame.common.game.GameType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 策略叠加器 - 支持多个策略叠加生效
 *
 * 大厂实践：腾讯、网易、字节均采用此模式
 *
 * 叠加规则：
 * 1. 保底策略优先：独立判断，一旦触发直接返回
 * 2. 权重叠加：多个策略的权重相加
 * 3. 概率叠加：P = 1 - (1-P1)*(1-P2)*...
 *
 * @author poker-platform
 */
@Slf4j
public class StrategyStack {

    private final GameType gameType;
    private final List<DealStrategy> strategies;

    public StrategyStack(GameType gameType) {
        this.gameType = gameType;
        this.strategies = new ArrayList<>();
    }

    /**
     * 添加策略
     */
    public StrategyStack addStrategy(DealStrategy strategy) {
        if (strategy != null && strategy.isEnabled()) {
            strategies.add(strategy);
            log.debug("添加策略: {}", strategy.getName());
        }
        return this;
    }

    /**
     * 批量添加策略
     */
    public StrategyStack addStrategies(List<DealStrategy> strategies) {
        if (strategies != null) {
            strategies.forEach(this::addStrategy);
        }
        return this;
    }

    /**
     * 获取目标牌型（叠加模式）
     */
    public HandRank getTargetRank(DealContext context) {
        if (context == null) {
            return null;
        }

        // 1. 保底策略优先（独立判断，一旦触发直接返回）
        HandRank guaranteeRank = executeGuaranteeStrategies(context);
        if (guaranteeRank != null) {
            log.debug("保底策略触发: rank={}", guaranteeRank.getName());
            return guaranteeRank;
        }

        // 2. 收集所有策略的叠加结果
        StackResult result = collectStackResult(context);

        // 3. 如果没有加成，返回null
        if (!result.hasAnyEffect()) {
            return null;
        }

        // 4. 根据叠加后的结果选择牌型
        HandRank selectedRank = selectRankByStackResult(result);

        if (selectedRank != null) {
            log.debug("策略叠加结果: totalBoost={}, rankWeights={}, selected={}",
                    result.getTotalBoost(), result.getRankWeights(), selectedRank.getName());
        }

        return selectedRank;
    }

    /**
     * 执行保底策略（独立判断，不叠加）
     */
    private HandRank executeGuaranteeStrategies(DealContext context) {
        for (DealStrategy strategy : strategies) {
            if (isGuaranteeStrategy(strategy)) {
                HandRank rank = strategy.getTargetRank(context);
                if (rank != null) {
                    return rank;
                }
            }
        }
        return null;
    }

    /**
     * 收集所有策略的叠加结果
     */
    private StackResult collectStackResult(DealContext context) {
        StackResult result = new StackResult();

        for (DealStrategy strategy : strategies) {
            // 跳过保底策略（已单独处理）
            if (isGuaranteeStrategy(strategy)) {
                continue;
            }

            HandRank rank = strategy.getTargetRank(context);
            double weight = strategy.getWeightFactor();

            if (rank != null) {
                // 策略返回了具体牌型，记录权重
                result.addRankWeight(rank, weight);
                log.debug("策略[{}]贡献牌型: {} (权重={})",
                        strategy.getName(), rank.getName(), weight);
            } else if (weight > 0) {
                // 策略返回null但有加成系数
                result.addBoost(weight);
                log.debug("策略[{}]贡献加成: weight={}", strategy.getName(), weight);
            }
        }

        return result;
    }

    /**
     * 根据叠加结果选择牌型
     */
    private HandRank selectRankByStackResult(StackResult result) {
        // 如果有具体牌型建议，按权重选择
        if (result.hasRankWeights()) {
            return result.selectRankByWeight();
        }

        // 只有概率加成，根据总加成决定是否触发
        if (result.getTotalBoost() > 0) {
            double probability = calculateTotalProbability(result.getTotalBoost());
            if (shouldTrigger(probability)) {
                return getDefaultBoostedRank();
            }
        }

        return null;
    }

    /**
     * 计算叠加后的总概率
     * 公式: P = 1 - (1-P1) * (1-P2) * (1-P3)
     */
    private double calculateTotalProbability(double totalBoost) {
        // 将加成系数转换为概率
        // 例如 boost=0.3 表示 30% 概率
        return Math.min(0.8, 0.1 + totalBoost * 0.5);
    }

    /**
     * 判断是否触发
     */
    private boolean shouldTrigger(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    /**
     * 获取默认加成牌型
     */
    private HandRank getDefaultBoostedRank() {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_STRAIGHT_FLUSH;
            case BULL: return HandRank.BULL_BULL;
            default: return null;
        }
    }

    /**
     * 判断是否为保底策略
     */
    private boolean isGuaranteeStrategy(DealStrategy strategy) {
        String name = strategy.getName();
        return name.contains("保底") || name.contains("Guarantee");
    }

    /**
     * 获取策略数量
     */
    public int size() {
        return strategies.size();
    }

    /**
     * 获取所有策略
     */
    public List<DealStrategy> getStrategies() {
        return new ArrayList<>(strategies);
    }

    /**
     * 清空策略
     */
    public void clear() {
        strategies.clear();
    }

    // ==================== 内部类 ====================

    /**
     * 叠加结果
     */
    public static class StackResult {
        private final Map<HandRank, Double> rankWeights = new HashMap<>();
        /**
         * -- GETTER --
         *  获取总加成系数
         */
        @Getter
        private double totalBoost = 0;

        /**
         * 添加牌型权重
         */
        public void addRankWeight(HandRank rank, double weight) {
            rankWeights.merge(rank, weight, Double::sum);
            totalBoost += weight;
        }

        /**
         * 添加加成系数
         */
        public void addBoost(double boost) {
            totalBoost += boost;
        }

        /**
         * 是否有牌型权重
         */
        public boolean hasRankWeights() {
            return !rankWeights.isEmpty();
        }

        /**
         * 是否有任何效果
         */
        public boolean hasAnyEffect() {
            return hasRankWeights() || totalBoost > 0;
        }

        /**
         * 获取牌型权重映射
         */
        public Map<HandRank, Double> getRankWeights() {
            return new HashMap<>(rankWeights);
        }

        /**
         * 按权重选择牌型
         */
        public HandRank selectRankByWeight() {
            double totalWeight = rankWeights.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();

            if (totalWeight <= 0) {
                return null;
            }

            double target = ThreadLocalRandom.current().nextDouble() * totalWeight;
            double cumulative = 0;

            for (Map.Entry<HandRank, Double> entry : rankWeights.entrySet()) {
                cumulative += entry.getValue();
                if (target <= cumulative) {
                    return entry.getKey();
                }
            }

            return rankWeights.keySet().iterator().next();
        }

        @Override
        public String toString() {
            return String.format("StackResult{rankWeights=%s, totalBoost=%.2f}",
                    rankWeights, totalBoost);
        }
    }
}
