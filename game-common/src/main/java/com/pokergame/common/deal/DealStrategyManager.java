package com.pokergame.common.deal;

import com.pokergame.common.deal.strategy.*;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 发牌策略组合管理器 - 无状态版本（叠加模式）
 *
 * 职责：
 * 1. 管理策略的初始化
 * 2. 委托给 StrategyStack 实现策略叠加
 * 3. 支持连胜平衡调整
 *
 * 设计模式：委托模式
 * - StrategyStack 负责策略叠加逻辑
 * - DealStrategyManager 负责策略初始化和对外接口
 *
 * @author poker-platform
 */
@Slf4j
public class DealStrategyManager {

    private final GameType gameType;
    private final StrategyStack strategyStack;
    private final WinningBalanceStrategy winningBalanceStrategy;

    public DealStrategyManager(GameType gameType) {
        this.gameType = gameType;
        this.strategyStack = new StrategyStack(gameType);
        this.winningBalanceStrategy = new WinningBalanceStrategy(gameType);
        initStrategies();
        log.info("策略管理器初始化完成，共 {} 个策略（叠加模式）", strategyStack.size());
    }

    /**
     * 初始化策略列表
     */
    private void initStrategies() {
        // 保底策略（使用动态 Provider）
        strategyStack.addStrategy(createGuaranteeStrategy());

        // 道具加成策略
        strategyStack.addStrategy(new ItemBoostStrategy(gameType));

        // VIP特权策略
        strategyStack.addStrategy(new VipDealStrategy(gameType));

        // 回归奖励策略
        strategyStack.addStrategy(new ReturnBonusStrategy(gameType));

        // 连败补偿策略
        strategyStack.addStrategy(new CompensationStrategy(gameType));

        // 活动加成策略
        strategyStack.addStrategy(new EventDealStrategy(gameType, 0.2));

        // 新手保护策略
        strategyStack.addStrategy(new RookieDealStrategy(gameType));

        // AI难度策略
        strategyStack.addStrategy(new AIDealStrategy(gameType, 5));

        // 正态分布策略（基础概率控制，作为兜底）
        strategyStack.addStrategy(new NormalDistributionStrategy(gameType));

        log.debug("策略列表初始化完成，共 {} 个策略", strategyStack.size());
    }

    /**
     * 创建保底策略
     */
    private GuaranteeDealStrategy createGuaranteeStrategy() {
        return new GuaranteeDealStrategy(gameType, context -> {
            // 优先检查道具保底
            if (context.hasGuaranteeItem()) {
                return getGuaranteeRankByGameType(gameType);
            }
            // 检查连败保底（连败超过5局）
            if (context.getConsecutiveLosses() >= 5) {
                return getCompensationRankByLossCount(gameType, context.getConsecutiveLosses());
            }
            // VIP保底（VIP8以上）
            if (context.getVipLevel() >= 8) {
                return getVipGuaranteeRank(gameType);
            }
            return null;
        });
    }

    /**
     * 根据游戏类型获取保底牌型
     */
    private HandRank getGuaranteeRankByGameType(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_STRAIGHT_FLUSH;
            case BULL: return HandRank.BULL_BULL;
            default: return HandRank.DOUDIZHU_BOMB;
        }
    }

    /**
     * 根据连败次数获取补偿牌型
     */
    private HandRank getCompensationRankByLossCount(GameType gameType, int lossCount) {
        if (lossCount >= 10) {
            switch (gameType) {
                case DOUDIZHU: return HandRank.DOUDIZHU_ROCKET;
                case TEXAS: return HandRank.TEXAS_ROYAL_FLUSH;
                case BULL: return HandRank.BULL_FIVE_SMALL;
                default: return HandRank.DOUDIZHU_ROCKET;
            }
        } else if (lossCount >= 5) {
            switch (gameType) {
                case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
                case TEXAS: return HandRank.TEXAS_FULL_HOUSE;
                case BULL: return HandRank.BULL_FOUR_BOMB;
                default: return HandRank.DOUDIZHU_BOMB;
            }
        }
        return null;
    }

    /**
     * 获取VIP保底牌型
     */
    private HandRank getVipGuaranteeRank(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_FLUSH;
            case BULL: return HandRank.BULL_BULL;
            default: return HandRank.DOUDIZHU_BOMB;
        }
    }

    /**
     * 获取最终的目标牌型（委托给 StrategyStack）
     *
     * @param context 发牌上下文（包含玩家所有数据）
     * @return 目标牌型，如果没有策略触发则返回 null
     */
    public HandRank getTargetRank(DealContext context) {
        return strategyStack.getTargetRank(context);
    }

    /**
     * 调整牌型（用于连胜平衡）
     * 在发牌后调用，根据连胜次数调整实际牌型
     *
     * @param originalRank 原始牌型
     * @param context 发牌上下文
     * @return 调整后的牌型
     */
    public HandRank adjustRank(HandRank originalRank, DealContext context) {
        if (originalRank == null || context == null) {
            return originalRank;
        }

        int winCount = context.getConsecutiveWins();
        return winningBalanceStrategy.getAdjustedRank(originalRank, winCount);
    }

    /**
     * 添加策略
     */
    public void addStrategy(DealStrategy strategy) {
        strategyStack.addStrategy(strategy);
    }

    /**
     * 获取策略列表（用于调试）
     */
    public List<DealStrategy> getStrategies() {
        return strategyStack.getStrategies();
    }

    /**
     * 获取游戏类型
     */
    public GameType getGameType() {
        return gameType;
    }

    /**
     * 创建默认的 DealContext（用于快速测试）
     */
    public static DealContext createDefaultContext(long playerId, GameType gameType) {
        return DealContext.builder()
                .playerId(playerId)
                .gameType(gameType)
                .consecutiveLosses(0)
                .consecutiveWins(0)
                .totalGames(0)
                .vipLevel(0)
                .lastLoginTime(System.currentTimeMillis())
                .activeItems(List.of())
                .activeEvents(List.of())
                .isAI(false)
                .aiDifficulty(0)
                .isRookie(true)
                .build();
    }
}