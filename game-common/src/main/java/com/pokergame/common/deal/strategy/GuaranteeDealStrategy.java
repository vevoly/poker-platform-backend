package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 保底策略
 *
 * 功能：确保玩家至少获得指定等级的牌型
 * 使用场景：
 * - 玩家使用了"保底卡"道具
 * - 新手保护（连续输牌后的补偿）
 * - 活动期间保证最低体验
 *
 * @author poker-platform
 */
@Slf4j
public class GuaranteeDealStrategy implements DealStrategy {

    private final GameType gameType;
    private final HandRank guaranteedRank;
    private final boolean isActive;
    private final int maxAttempts;

    /**
     * @param gameType 游戏类型
     * @param guaranteedRank 保底的牌型等级
     */
    public GuaranteeDealStrategy(GameType gameType, HandRank guaranteedRank) {
        this(gameType, guaranteedRank, true, 50);
    }

    /**
     * @param gameType 游戏类型
     * @param guaranteedRank 保底的牌型等级
     * @param isActive 是否激活
     * @param maxAttempts 最大尝试次数
     */
    public GuaranteeDealStrategy(GameType gameType, HandRank guaranteedRank,
                                 boolean isActive, int maxAttempts) {
        this.gameType = gameType;
        this.guaranteedRank = guaranteedRank;
        this.isActive = isActive;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public String getName() {
        return "保底策略-" + guaranteedRank.getName();
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
        return guaranteedRank;
    }

//    @Override
//    public List<Integer> getSpecialPlayerIndices(int playerCount) {
//        // 默认对所有玩家生效，也可通过构造函数指定特定玩家
//        java.util.List<Integer> all = new java.util.ArrayList<>();
//        for (int i = 0; i < playerCount; i++) {
//            all.add(i);
//        }
//        return all;
//    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        if (targetPlayer >= 0 && targetPlayer < playerCount) {
            return List.of(targetPlayer);
        }
        // 默认随机选择一名玩家
        int randomPlayer = ThreadLocalRandom.current().nextInt(playerCount);
        return List.of(randomPlayer);
    }

    @Override
    public double getWeightFactor() {
        return 1.0;
    }

    /**
     * 获取最大尝试次数
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * 创建一个针对特定玩家的保底策略
     */
    public static GuaranteeDealStrategy forPlayer(GameType gameType,
                                                  HandRank guaranteedRank,
                                                  int playerIndex) {
        GuaranteeDealStrategy strategy = new GuaranteeDealStrategy(gameType, guaranteedRank);
        strategy.setTargetPlayer(playerIndex);
        return strategy;
    }

    /**
     * 创建连续输牌后的补偿保底
     */
    public static GuaranteeDealStrategy createCompensation(GameType gameType,
                                                           int consecutiveLosses) {
        HandRank rank;
        if (consecutiveLosses >= 10) {
            rank = getTopRank(gameType);
        } else if (consecutiveLosses >= 5) {
            rank = getMidRank(gameType);
        } else {
            rank = getBaseRank(gameType);
        }
        log.info("玩家连输{}局，触发保底策略，目标牌型: {}", consecutiveLosses, rank.getName());
        return new GuaranteeDealStrategy(gameType, rank);
    }

    private static HandRank getTopRank(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_ROCKET;
            case TEXAS: return HandRank.TEXAS_STRAIGHT_FLUSH;
            case BULL: return HandRank.BULL_FIVE_SMALL;
            default: return HandRank.DOUDIZHU_BOMB;
        }
    }

    private static HandRank getMidRank(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_THREE_OF_KIND;
            case BULL: return HandRank.BULL_BULL;
            default: return HandRank.DOUDIZHU_STRAIGHT;
        }
    }

    private static HandRank getBaseRank(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_STRAIGHT;
            case TEXAS: return HandRank.TEXAS_ONE_PAIR;
            case BULL: return HandRank.BULL_7;
            default: return HandRank.DOUDIZHU_PAIR;
        }
    }

    private int targetPlayer = -1;

    private void setTargetPlayer(int playerIndex) {
        this.targetPlayer = playerIndex;
    }

}
