package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连胜平衡策略
 *
 * 功能：玩家连续赢牌后，适当降低牌型质量，防止垄断
 * 使用场景：保持游戏公平性，防止单一玩家长期占据优势
 *
 * 大厂实践：腾讯游戏平衡机制，根据ELO等级分动态调整
 *
 * @author poker-platform
 */
@Slf4j
public class WinningBalanceStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;

    // 玩家连胜次数记录
    private static final ConcurrentHashMap<Long, AtomicInteger> WIN_COUNTER = new ConcurrentHashMap<>();

    // 平衡阈值配置
    private static final int BALANCE_THRESHOLD_1 = 3;   // 3连胜 -> 轻微平衡
    private static final int BALANCE_THRESHOLD_2 = 5;   // 5连胜 -> 中度平衡
    private static final int BALANCE_THRESHOLD_3 = 8;   // 8连胜 -> 高度平衡
    private static final int BALANCE_THRESHOLD_4 = 12;  // 12连胜 -> 极限平衡

    // 平衡强度系数（降低牌型的程度）
    private static final double BALANCE_STRENGTH_LOW = 0.3;
    private static final double BALANCE_STRENGTH_MID = 0.5;
    private static final double BALANCE_STRENGTH_HIGH = 0.7;
    private static final double BALANCE_STRENGTH_MAX = 0.85;

    public WinningBalanceStrategy(GameType gameType) {
        this(gameType, true);
    }

    public WinningBalanceStrategy(GameType gameType, boolean isActive) {
        this.gameType = gameType;
        this.isActive = isActive;
    }

    @Override
    public String getName() {
        return "连胜平衡策略";
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
        throw new UnsupportedOperationException("请使用 getAdjustedRank(originalRank, winCount) 方法");
    }

    /**
     * 根据连胜次数调整牌型等级
     * @param originalRank 原始牌型
     * @param winCount 连胜次数
     * @return 调整后的牌型
     */
    public HandRank getAdjustedRank(HandRank originalRank, int winCount) {
        if (winCount < BALANCE_THRESHOLD_1) {
            return originalRank;
        }

        double strength = getBalanceStrength(winCount);

        // 根据平衡强度降低牌型
        return downgradeRank(originalRank, strength);
    }

    /**
     * 获取平衡强度系数
     */
    private double getBalanceStrength(int winCount) {
        if (winCount >= BALANCE_THRESHOLD_4) {
            return BALANCE_STRENGTH_MAX;
        } else if (winCount >= BALANCE_THRESHOLD_3) {
            return BALANCE_STRENGTH_HIGH;
        } else if (winCount >= BALANCE_THRESHOLD_2) {
            return BALANCE_STRENGTH_MID;
        } else {
            return BALANCE_STRENGTH_LOW;
        }
    }

    /**
     * 降低牌型等级
     */
    private HandRank downgradeRank(HandRank originalRank, double strength) {
        // 根据游戏类型获取牌型等级列表
        HandRank[] ranks = getRankLevels();

        int currentIndex = findRankIndex(originalRank, ranks);
        if (currentIndex <= 0) {
            return originalRank;
        }

        // 计算降级幅度
        int downgradeSteps = Math.max(1, (int)(currentIndex * strength));
        int newIndex = Math.max(0, currentIndex - downgradeSteps);

        HandRank newRank = ranks[newIndex];
        log.debug("牌型降级: {} -> {} (连胜{}局, 强度{})",
                originalRank.getName(), newRank.getName(),
                getWinCount(0), strength);

        return newRank;
    }

    private HandRank[] getRankLevels() {
        switch (gameType) {
            case DOUDIZHU:
                return new HandRank[]{
                        HandRank.DOUDIZHU_SINGLE, HandRank.DOUDIZHU_PAIR,
                        HandRank.DOUDIZHU_TRIPLE, HandRank.DOUDIZHU_STRAIGHT,
                        HandRank.DOUDIZHU_BOMB, HandRank.DOUDIZHU_ROCKET
                };
            case TEXAS:
                return new HandRank[]{
                        HandRank.TEXAS_HIGH_CARD, HandRank.TEXAS_ONE_PAIR,
                        HandRank.TEXAS_TWO_PAIR, HandRank.TEXAS_THREE_OF_KIND,
                        HandRank.TEXAS_STRAIGHT, HandRank.TEXAS_FLUSH,
                        HandRank.TEXAS_FULL_HOUSE, HandRank.TEXAS_FOUR_OF_KIND,
                        HandRank.TEXAS_STRAIGHT_FLUSH, HandRank.TEXAS_ROYAL_FLUSH
                };
            case BULL:
                return new HandRank[]{
                        HandRank.BULL_NO, HandRank.BULL_1, HandRank.BULL_2,
                        HandRank.BULL_3, HandRank.BULL_4, HandRank.BULL_5,
                        HandRank.BULL_6, HandRank.BULL_7, HandRank.BULL_8,
                        HandRank.BULL_9, HandRank.BULL_BULL,
                        HandRank.BULL_FOUR_BOMB, HandRank.BULL_FIVE_SMALL
                };
            default:
                return new HandRank[0];
        }
    }

    private int findRankIndex(HandRank rank, HandRank[] ranks) {
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i] == rank) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 记录玩家赢牌
     */
    public static void recordWin(long userId) {
        AtomicInteger counter = WIN_COUNTER.computeIfAbsent(userId, k -> new AtomicInteger(0));
        counter.incrementAndGet();
        log.debug("玩家{}连胜次数: {}", userId, counter.get());
    }

    /**
     * 记录玩家输牌（重置连胜计数）
     */
    public static void recordLoss(long userId) {
        WIN_COUNTER.remove(userId);
        log.debug("玩家{}输牌，重置连胜计数", userId);
    }

    /**
     * 获取玩家连胜次数
     */
    public static int getWinCount(long userId) {
        AtomicInteger counter = WIN_COUNTER.get(userId);
        return counter != null ? counter.get() : 0;
    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        return List.of();
    }

    @Override
    public double getWeightFactor() {
        return 1.0;
    }
}
