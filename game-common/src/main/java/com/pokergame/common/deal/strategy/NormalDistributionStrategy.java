package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 正态分布策略
 *
 * 功能：控制牌型出现概率符合正态分布
 * 使用场景：全局牌型概率控制，保证大量牌局的统计公平性
 *
 * 大厂实践：腾讯棋牌专利技术 - 牌型配置表 + 正态分布函数
 *
 * @author poker-platform
 */
@Slf4j
public class NormalDistributionStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;

    // 牌型配置表（牌型 -> 正态分布概率）
    private static final ConcurrentHashMap<GameType, HandRankProbability[]> RANK_PROBABILITIES = new ConcurrentHashMap<>();

    // 随机数生成器
    private static final Random RANDOM = new Random();

    // 牌局计数器（用于全局概率统计）
    private static final ConcurrentHashMap<GameType, Integer> GAME_COUNTER = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<GameType, ConcurrentHashMap<HandRank, Integer>> RANK_COUNTER = new ConcurrentHashMap<>();

    // 全局概率偏差容忍度
    private static final double TOLERANCE = 0.05;

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
    public HandRank getTargetRank(int playerIndex) {
        HandRankProbability[] probs = RANK_PROBABILITIES.get(gameType);
        if (probs == null) {
            return null;
        }

        // 根据当前全局概率偏差调整选择
        return selectRankWithNormalDistribution(probs);
    }

    /**
     * 基于正态分布选择牌型
     */
    private HandRank selectRankWithNormalDistribution(HandRankProbability[] probs) {
        // 计算当前各牌型的实际出现频率
        ConcurrentHashMap<HandRank, Integer> counter = RANK_COUNTER.computeIfAbsent(gameType, k -> new ConcurrentHashMap<>());
        int totalGames = GAME_COUNTER.getOrDefault(gameType, 0);

        // 使用正态分布随机选择
        double random = RANDOM.nextGaussian() * 0.5 + 0.5;  // 均值0.5，标准差0.5
        random = Math.max(0, Math.min(1, random));

        double cumulative = 0;
        for (HandRankProbability prob : probs) {
            // 根据实际频率调整理论概率
            double adjustedProb = getAdjustedProbability(prob, counter, totalGames);
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
    private double getAdjustedProbability(HandRankProbability prob,
                                          ConcurrentHashMap<HandRank, Integer> counter,
                                          int totalGames) {
        double theoreticalProb = prob.probability;

        if (totalGames < 100) {
            return theoreticalProb;
        }

        int actualCount = counter.getOrDefault(prob.rank, 0);
        double actualProb = (double) actualCount / totalGames;

        // 计算偏差并调整
        double deviation = actualProb - theoreticalProb;

        // 如果实际概率高于理论概率，降低选择概率
        if (deviation > TOLERANCE) {
            return theoreticalProb * (1 - (deviation - TOLERANCE) * 2);
        }
        // 如果实际概率低于理论概率，提高选择概率
        else if (deviation < -TOLERANCE) {
            return theoreticalProb * (1 + (Math.abs(deviation) - TOLERANCE) * 2);
        }

        return theoreticalProb;
    }

    /**
     * 记录牌局结果（用于全局概率统计）
     */
    public static void recordGameResult(GameType gameType, HandRank rank) {
        GAME_COUNTER.merge(gameType, 1, Integer::sum);
        ConcurrentHashMap<HandRank, Integer> counter = RANK_COUNTER.computeIfAbsent(gameType, k -> new ConcurrentHashMap<>());
        counter.merge(rank, 1, Integer::sum);

        // 每1000局输出一次统计
        int total = GAME_COUNTER.get(gameType);
        if (total % 1000 == 0) {
            log.info("{}牌型统计 (总{}局): {}", gameType, total, counter);
        }
    }

    /**
     * 获取牌型概率配置
     */
    public static HandRankProbability[] getProbabilities(GameType gameType) {
        return RANK_PROBABILITIES.get(gameType);
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
