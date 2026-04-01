package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 正态分布策略 - 无状态版本
 *
 * 功能：控制牌型出现概率符合正态分布
 * 使用场景：全局牌型概率控制，保证大量牌局的统计公平性
 *
 * 设计原则：
 * - 策略本身无状态，概率配置从配置表加载
 * - 全局统计信息由调用方维护，通过 DealContext 传入
 * - 支持动态概率调整
 *
 * 大厂实践：腾讯棋牌专利技术 - 牌型配置表 + 正态分布函数
 *
 * @author poker-platform
 */
@Slf4j
public class NormalDistributionStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;

    // 牌型概率配置（从配置加载，静态不变）
    private static final Map<GameType, HandRankProbability[]> RANK_PROBABILITIES = new ConcurrentHashMap<>();

    static {
        initProbabilities();
    }

    public NormalDistributionStrategy(GameType gameType) {
        this(gameType, true);
    }

    public NormalDistributionStrategy(GameType gameType, boolean isActive) {
        this.gameType = gameType;
        this.isActive = isActive;
    }

    /**
     * 初始化牌型概率配置表
     * 配置可改为从 JSON 文件加载，便于运营调整
     */
    private static void initProbabilities() {
        // 斗地主牌型概率配置（基于正态分布）
        RANK_PROBABILITIES.put(GameType.DOUDIZHU, new HandRankProbability[]{
                new HandRankProbability(HandRank.DOUDIZHU_SINGLE, 0.25, 0, 2),
                new HandRankProbability(HandRank.DOUDIZHU_PAIR, 0.20, 1, 3),
                new HandRankProbability(HandRank.DOUDIZHU_TRIPLE, 0.15, 2, 4),
                new HandRankProbability(HandRank.DOUDIZHU_STRAIGHT, 0.12, 3, 5),
                new HandRankProbability(HandRank.DOUDIZHU_BOMB, 0.05, 4, 6),
                new HandRankProbability(HandRank.DOUDIZHU_ROCKET, 0.01, 5, 7),
                new HandRankProbability(HandRank.DOUDIZHU_JUNK, 0.22, -1, 1)
        });

        // 德州扑克牌型概率配置
        RANK_PROBABILITIES.put(GameType.TEXAS, new HandRankProbability[]{
                new HandRankProbability(HandRank.TEXAS_HIGH_CARD, 0.50, 0, 2),
                new HandRankProbability(HandRank.TEXAS_ONE_PAIR, 0.42, 1, 3),
                new HandRankProbability(HandRank.TEXAS_TWO_PAIR, 0.05, 2, 4),
                new HandRankProbability(HandRank.TEXAS_THREE_OF_KIND, 0.02, 3, 5),
                new HandRankProbability(HandRank.TEXAS_STRAIGHT, 0.005, 4, 6),
                new HandRankProbability(HandRank.TEXAS_FLUSH, 0.003, 5, 7),
                new HandRankProbability(HandRank.TEXAS_FULL_HOUSE, 0.001, 6, 8),
                new HandRankProbability(HandRank.TEXAS_FOUR_OF_KIND, 0.0005, 7, 9),
                new HandRankProbability(HandRank.TEXAS_STRAIGHT_FLUSH, 0.0001, 8, 10),
                new HandRankProbability(HandRank.TEXAS_ROYAL_FLUSH, 0.00001, 9, 11)
        });

        // 牛牛牌型概率配置
        RANK_PROBABILITIES.put(GameType.BULL, new HandRankProbability[]{
                new HandRankProbability(HandRank.BULL_NO, 0.25, 0, 2),
                new HandRankProbability(HandRank.BULL_1, 0.10, 1, 3),
                new HandRankProbability(HandRank.BULL_2, 0.10, 2, 4),
                new HandRankProbability(HandRank.BULL_3, 0.10, 3, 5),
                new HandRankProbability(HandRank.BULL_4, 0.10, 4, 6),
                new HandRankProbability(HandRank.BULL_5, 0.10, 5, 7),
                new HandRankProbability(HandRank.BULL_6, 0.08, 6, 8),
                new HandRankProbability(HandRank.BULL_7, 0.06, 7, 9),
                new HandRankProbability(HandRank.BULL_8, 0.04, 8, 10),
                new HandRankProbability(HandRank.BULL_9, 0.03, 9, 11),
                new HandRankProbability(HandRank.BULL_BULL, 0.02, 10, 12),
                new HandRankProbability(HandRank.BULL_FOUR_BOMB, 0.005, 11, 13),
                new HandRankProbability(HandRank.BULL_FIVE_SMALL, 0.001, 12, 14)
        });

        log.info("正态分布策略概率配置初始化完成");
    }

    @Override
    public String getName() {
        return "正态分布策略";
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    @Override
    public GameType getGameType() {
        return gameType;
    }

    @Override
    public HandRank getTargetRank(DealContext context) {
        HandRankProbability[] probs = RANK_PROBABILITIES.get(gameType);
        if (probs == null) {
            return null;
        }

        // 从 context 获取全局统计信息
        GlobalStatistics stats = context.getGlobalStatistics();

        // 根据全局概率偏差调整选择
        return selectRankWithNormalDistribution(probs, stats);
    }

    /**
     * 基于正态分布选择牌型
     *
     * @param probs 牌型概率配置
     * @param stats 全局统计信息（从外部传入）
     * @return 选中的牌型
     */
    private HandRank selectRankWithNormalDistribution(HandRankProbability[] probs,
                                                      GlobalStatistics stats) {
        // 使用正态分布随机选择
        double random = ThreadLocalRandom.current().nextGaussian() * 0.5 + 0.5;
        random = Math.max(0, Math.min(1, random));

        double cumulative = 0;
        for (HandRankProbability prob : probs) {
            // 根据实际频率调整理论概率
            double adjustedProb = getAdjustedProbability(prob, stats);
            cumulative += adjustedProb;
            if (random <= cumulative) {
                return prob.rank;
            }
        }

        return probs[probs.length - 1].rank;
    }

    /**
     * 获取调整后的概率（根据实际出现频率）
     */
    private double getAdjustedProbability(HandRankProbability prob, GlobalStatistics stats) {
        double theoreticalProb = prob.probability;

        if (stats == null || stats.getTotalGames() < 100) {
            return theoreticalProb;
        }

        int actualCount = stats.getRankCount(prob.rank);
        double actualProb = (double) actualCount / stats.getTotalGames();

        // 计算偏差并调整
        double deviation = actualProb - theoreticalProb;

        // 如果实际概率高于理论概率，降低选择概率
        if (deviation > TOLERANCE) {
            double reduced = theoreticalProb * (1 - (deviation - TOLERANCE) * 2);
            return Math.max(0, reduced);
        }
        // 如果实际概率低于理论概率，提高选择概率
        else if (deviation < -TOLERANCE) {
            double increased = theoreticalProb * (1 + (Math.abs(deviation) - TOLERANCE) * 2);
            return Math.min(1.0, increased);
        }

        return theoreticalProb;
    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        return List.of();
    }

    @Override
    public double getWeightFactor() {
        return 1.0;
    }

    /**
     * 获取牌型概率配置（供外部查询）
     */
    public static HandRankProbability[] getProbabilities(GameType gameType) {
        return RANK_PROBABILITIES.get(gameType);
    }

    // ==================== 配置常量 ====================

    /** 全局概率偏差容忍度 */
    private static final double TOLERANCE = 0.05;

    // ==================== 内部类 ====================

    /**
     * 全局统计信息 - 纯数据结构
     * 由调用方维护，通过 DealContext 传入
     */
    public static class GlobalStatistics {
        private final int totalGames;
        private final Map<HandRank, Integer> rankCounts;

        public GlobalStatistics(int totalGames, Map<HandRank, Integer> rankCounts) {
            this.totalGames = totalGames;
            this.rankCounts = rankCounts != null ? rankCounts : Map.of();
        }

        public int getTotalGames() {
            return totalGames;
        }

        public int getRankCount(HandRank rank) {
            return rankCounts.getOrDefault(rank, 0);
        }

        public Map<HandRank, Integer> getRankCounts() {
            return rankCounts;
        }
    }

    /**
     * 牌型概率配置内部类
     */
    public static class HandRankProbability {
        public final HandRank rank;
        public final double probability;
        public final int mean;      // 正态分布均值
        public final int stdDev;    // 正态分布标准差

        public HandRankProbability(HandRank rank, double probability, int mean, int stdDev) {
            this.rank = rank;
            this.probability = probability;
            this.mean = mean;
            this.stdDev = stdDev;
        }
    }
}
