package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 连败补偿策略 - 无状态版本
 *
 * 功能：玩家连续输牌达到阈值后，给予更好的牌型
 * 使用场景：防止玩家因连续输牌而流失，提升用户体验
 *
 * 设计原则：
 * - 策略本身无状态，所有数据从 DealContext 获取
 * - 不存储玩家数据，数据由调用方传入
 *
 * @author poker-platform
 */
@Slf4j
public class CompensationStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;

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
    public HandRank getTargetRank(DealContext context) {
        // 从 context 获取连败次数
        int lossCount = context.getConsecutiveLosses();

        // 检查是否触发补偿
        if (!shouldCompensate(lossCount)) {
            return null;
        }

        // 根据连败次数决定补偿等级
        if (lossCount >= THRESHOLD_LEVEL_4) {
            log.debug("玩家{}连败{}局，触发顶级补偿", context.getPlayerId(), lossCount);
            return getTopCompensationRank();
        } else if (lossCount >= THRESHOLD_LEVEL_3) {
            log.debug("玩家{}连败{}局，触发高级补偿", context.getPlayerId(), lossCount);
            return getHighCompensationRank();
        } else if (lossCount >= THRESHOLD_LEVEL_2) {
            log.debug("玩家{}连败{}局，触发中级补偿", context.getPlayerId(), lossCount);
            return getMidCompensationRank();
        } else if (lossCount >= THRESHOLD_LEVEL_1) {
            log.debug("玩家{}连败{}局，触发基础补偿", context.getPlayerId(), lossCount);
            return getBaseCompensationRank();
        }

        return null;
    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        // 连败补偿需要根据实际玩家ID判断，此处返回空
        // 因为补偿条件在 getTargetRank 中根据 context 判断
        return List.of();
    }

    @Override
    public double getWeightFactor() {
        return 1.0;
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
}
