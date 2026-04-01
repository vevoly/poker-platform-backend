package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 保底策略 - 无状态版本
 *
 * 支持两种构造方式：
 * 1. 固定牌型保底：new GuaranteeDealStrategy(gameType, HandRank.DOUDIZHU_BOMB)
 * 2. 动态牌型保底：new GuaranteeDealStrategy(gameType, context -> { ... })
 */
@Slf4j
public class GuaranteeDealStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;
    /**
     * -- GETTER --
     *  获取最大尝试次数
     */
    @Getter
    private final int maxAttempts;

    /**
     * -- GETTER --
     *  获取保底类型
     */
    // 保底类型
    @Getter
    private final GuaranteeType guaranteeType;

    // 固定牌型（当 guaranteeType == FIXED 时使用）
    private final HandRank fixedGuaranteedRank;

    // 动态提供者（当 guaranteeType == DYNAMIC 时使用）
    private final GuaranteedRankProvider rankProvider;

    /**
     * 保底类型
     */
    public enum GuaranteeType {
        FIXED,      // 固定牌型保底
        DYNAMIC     // 动态牌型保底
    }

    /**
     * 保底牌型提供者接口
     */
    @FunctionalInterface
    public interface GuaranteedRankProvider {
        HandRank getRank(DealContext context);
    }

    // ==================== 构造函数 ====================

    /**
     * 固定牌型保底
     */
    public GuaranteeDealStrategy(GameType gameType, HandRank guaranteedRank) {
        this(gameType, guaranteedRank, true, 50);
    }

    public GuaranteeDealStrategy(GameType gameType, HandRank guaranteedRank,
                                 boolean isActive, int maxAttempts) {
        this.gameType = gameType;
        this.guaranteeType = GuaranteeType.FIXED;
        this.fixedGuaranteedRank = guaranteedRank;
        this.rankProvider = null;
        this.isActive = isActive;
        this.maxAttempts = maxAttempts;
    }

    /**
     * 动态牌型保底（使用提供者）
     */
    public GuaranteeDealStrategy(GameType gameType, GuaranteedRankProvider rankProvider) {
        this(gameType, rankProvider, true, 50);
    }

    public GuaranteeDealStrategy(GameType gameType, GuaranteedRankProvider rankProvider,
                                 boolean isActive, int maxAttempts) {
        this.gameType = gameType;
        this.guaranteeType = GuaranteeType.DYNAMIC;
        this.fixedGuaranteedRank = null;
        this.rankProvider = rankProvider;
        this.isActive = isActive;
        this.maxAttempts = maxAttempts;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建固定牌型保底策略
     */
    public static GuaranteeDealStrategy fixed(GameType gameType, HandRank guaranteedRank) {
        return new GuaranteeDealStrategy(gameType, guaranteedRank);
    }

    /**
     * 创建动态牌型保底策略
     */
    public static GuaranteeDealStrategy dynamic(GameType gameType, GuaranteedRankProvider rankProvider) {
        return new GuaranteeDealStrategy(gameType, rankProvider);
    }

    /**
     * 创建连败补偿保底策略（动态计算）
     * 根据连败次数自动决定保底牌型
     */
    public static GuaranteeDealStrategy compensation(GameType gameType) {
        return new GuaranteeDealStrategy(gameType, context -> {
            int lossCount = context.getConsecutiveLosses();
            if (lossCount >= 10) {
                return getTopCompensationRank(gameType);
            } else if (lossCount >= 5) {
                return getMidCompensationRank(gameType);
            } else if (lossCount >= 3) {
                return getBaseCompensationRank(gameType);
            }
            return null;
        });
    }

    /**
     * 创建VIP保底策略
     */
    public static GuaranteeDealStrategy vip(GameType gameType, int requiredVipLevel) {
        return new GuaranteeDealStrategy(gameType, context -> {
            if (context.getVipLevel() >= requiredVipLevel) {
                return getVipGuaranteeRank(gameType);
            }
            return null;
        });
    }

    /**
     * 创建道具保底策略
     */
    public static GuaranteeDealStrategy item(GameType gameType, String itemId) {
        return new GuaranteeDealStrategy(gameType, context -> {
            if (context.hasItem(itemId)) {
                return getItemGuaranteeRank(gameType);
            }
            return null;
        });
    }

    // ==================== 牌型等级获取方法 ====================

    private static HandRank getTopCompensationRank(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_ROCKET;
            case TEXAS: return HandRank.TEXAS_ROYAL_FLUSH;
            case BULL: return HandRank.BULL_FIVE_SMALL;
            default: return HandRank.DOUDIZHU_ROCKET;
        }
    }

    private static HandRank getMidCompensationRank(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_STRAIGHT_FLUSH;
            case BULL: return HandRank.BULL_FOUR_BOMB;
            default: return HandRank.DOUDIZHU_BOMB;
        }
    }

    private static HandRank getBaseCompensationRank(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_FULL_HOUSE;
            case BULL: return HandRank.BULL_BULL;
            default: return HandRank.DOUDIZHU_STRAIGHT;
        }
    }

    private static HandRank getVipGuaranteeRank(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_FLUSH;
            case BULL: return HandRank.BULL_BULL;
            default: return HandRank.DOUDIZHU_BOMB;
        }
    }

    private static HandRank getItemGuaranteeRank(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_TWO_PAIR;
            case BULL: return HandRank.BULL_BULL;
            default: return HandRank.DOUDIZHU_BOMB;
        }
    }

    // ==================== 接口实现 ====================

    @Override
    public String getName() {
        if (guaranteeType == GuaranteeType.FIXED && fixedGuaranteedRank != null) {
            return "保底策略-" + fixedGuaranteedRank.getName();
        }
        return "保底策略[DYNAMIC]";
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
        HandRank targetRank = null;

        switch (guaranteeType) {
            case FIXED:
                targetRank = fixedGuaranteedRank;
                break;
            case DYNAMIC:
                if (rankProvider != null) {
                    targetRank = rankProvider.getRank(context);
                }
                break;
        }

        if (targetRank != null) {
            log.debug("保底策略触发: player={}, type={}, rank={}",
                    context.getPlayerId(), guaranteeType, targetRank.getName());
        }

        return targetRank;
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
