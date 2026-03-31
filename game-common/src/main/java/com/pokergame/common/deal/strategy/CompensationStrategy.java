package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连败补偿策略
 *
 * 功能：玩家连续输牌达到阈值后，给予更好的牌型
 * 使用场景：防止玩家因连续输牌而流失，提升用户体验
 *
 * 大厂实践：腾讯棋牌专利技术，根据连败次数动态调整牌型等级
 *
 * @author poker-platform
 */
@Slf4j
public class CompensationStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;

    // 玩家连败次数记录（实际应从数据库/缓存获取）
    private static final ConcurrentHashMap<Long, AtomicInteger> LOSS_COUNTER = new ConcurrentHashMap<>();

    // 补偿阈值配置
    private static final int THRESHOLD_LEVEL_1 = 3;   // 3连败 -> 基础补偿
    private static final int THRESHOLD_LEVEL_2 = 5;   // 5连败 -> 中级补偿
    private static final int THRESHOLD_LEVEL_3 = 8;   // 8连败 -> 高级补偿
    private static final int THRESHOLD_LEVEL_4 = 12;  // 12连败 -> 顶级补偿

    // 补偿概率配置（避免100%补偿，保持真实性）
    private static final double COMPENSATION_PROB_BASE = 0.6;   // 基础补偿概率60%
    private static final double COMPENSATION_PROB_MAX = 0.95;   // 最大补偿概率95%

    public CompensationStrategy(GameType gameType) {
        this(gameType, true);
    }

    public CompensationStrategy(GameType gameType, boolean isActive) {
        this.gameType = gameType;
        this.isActive = isActive;
    }

    @Override
    public String getName() {
        return "连败补偿策略";
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
        // 这个方法在连败补偿中需要动态获取玩家ID
        throw new UnsupportedOperationException("请使用 getTargetRank(playerId, lossCount) 方法");
    }

    /**
     * 根据连败次数获取目标牌型
     * @param lossCount 连败次数
     * @return 目标牌型
     */
    public HandRank getTargetRankByLossCount(int lossCount) {
        // 检查是否触发补偿
        if (!shouldCompensate(lossCount)) {
            return null;
        }

        // 根据连败次数决定补偿等级
        if (lossCount >= THRESHOLD_LEVEL_4) {
            log.debug("连败{}局，触发顶级补偿", lossCount);
            return getTopCompensationRank();
        } else if (lossCount >= THRESHOLD_LEVEL_3) {
            log.debug("连败{}局，触发高级补偿", lossCount);
            return getHighCompensationRank();
        } else if (lossCount >= THRESHOLD_LEVEL_2) {
            log.debug("连败{}局，触发中级补偿", lossCount);
            return getMidCompensationRank();
        } else if (lossCount >= THRESHOLD_LEVEL_1) {
            log.debug("连败{}局，触发基础补偿", lossCount);
            return getBaseCompensationRank();
        }

        return null;
    }

    /**
     * 判断是否触发补偿
     */
    private boolean shouldCompensate(int lossCount) {
        if (lossCount < THRESHOLD_LEVEL_1) {
            return false;
        }

        // 计算补偿概率（连败越多，概率越高）
        double probability = COMPENSATION_PROB_BASE +
                (Math.min(lossCount, THRESHOLD_LEVEL_4) - THRESHOLD_LEVEL_1) * 0.05;
        probability = Math.min(probability, COMPENSATION_PROB_MAX);

        return Math.random() < probability;
    }

    private HandRank getBaseCompensationRank() {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_TWO_PAIR;
            case BULL: return HandRank.BULL_BULL;
            default: return HandRank.DOUDIZHU_STRAIGHT;
        }
    }

    private HandRank getMidCompensationRank() {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_ROCKET;
            case TEXAS: return HandRank.TEXAS_FLUSH;
            case BULL: return HandRank.BULL_FOUR_BOMB;
            default: return HandRank.DOUDIZHU_BOMB;
        }
    }

    private HandRank getHighCompensationRank() {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_ROCKET;
            case TEXAS: return HandRank.TEXAS_FULL_HOUSE;
            case BULL: return HandRank.BULL_FIVE_SMALL;
            default: return HandRank.DOUDIZHU_ROCKET;
        }
    }

    private HandRank getTopCompensationRank() {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_ROCKET;
            case TEXAS: return HandRank.TEXAS_ROYAL_FLUSH;
            case BULL: return HandRank.BULL_FIVE_SMALL;
            default: return HandRank.DOUDIZHU_ROCKET;
        }
    }

    /**
     * 记录玩家输牌（每次对局结束后调用）
     */
    public static void recordLoss(long userId) {
        AtomicInteger counter = LOSS_COUNTER.computeIfAbsent(userId, k -> new AtomicInteger(0));
        int newCount = counter.incrementAndGet();
        log.debug("玩家{}连败次数: {}", userId, newCount);
    }

    /**
     * 记录玩家赢牌（重置连败计数）
     */
    public static void recordWin(long userId) {
        LOSS_COUNTER.remove(userId);
        log.debug("玩家{}赢牌，重置连败计数", userId);
    }

    /**
     * 获取玩家连败次数
     */
    public static int getLossCount(long userId) {
        AtomicInteger counter = LOSS_COUNTER.get(userId);
        return counter != null ? counter.get() : 0;
    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        // 连败补偿需要根据实际玩家ID判断，此处返回空
        return List.of();
    }

    @Override
    public double getWeightFactor() {
        return 1.0;
    }
}
