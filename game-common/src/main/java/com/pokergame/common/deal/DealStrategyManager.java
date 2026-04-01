package com.pokergame.common.deal;

import com.pokergame.common.deal.strategy.*;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 发牌策略组合管理器 - 无状态版本
 *
 * 职责：
 * 1. 管理策略的优先级顺序
 * 2. 按优先级依次调用策略，获取目标牌型
 * 3. 支持连胜平衡调整
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

    /**
     * 初始化策略链（按优先级从高到低）
     *
     * 优先级顺序说明：
     * 1. 保底策略 - 最高优先级，保证玩家体验
     * 2. 道具加成 - 付费道具优先
     * 3. VIP特权 - VIP玩家体验
     * 4. 回归奖励 - 召回流失玩家
     * 5. 连败补偿 - 防流失机制
     * 6. 活动加成 - 运营活动
     * 7. 新手保护 - 新手体验
     * 8. AI难度 - AI控制
     * 9. 正态分布 - 基础概率（兜底）
     * 10. 连胜平衡 - 最后调整，不参与选择
     */
    private void initStrategies() {
        // 注意：GuaranteeDealStrategy 需要 HandRank 参数
        // 这里传 null 表示动态计算，策略内部会根据 context 决定保底牌型
        // 实际使用时，保底策略会通过工厂方法创建，这里使用带 Provider 的构造函数
        strategies.add(createGuaranteeStrategy());

        // 2. 道具加成策略
        strategies.add(new ItemBoostStrategy(gameType));

        // 3. VIP特权策略
        strategies.add(new VipDealStrategy(gameType));

        // 4. 回归奖励策略
        strategies.add(new ReturnBonusStrategy(gameType));

        // 5. 连败补偿策略
        strategies.add(new CompensationStrategy(gameType));

        // 6. 活动加成策略
        strategies.add(new EventDealStrategy(gameType, 0.2));

        // 7. 新手保护策略
        strategies.add(new RookieDealStrategy(gameType));

        // 8. AI难度策略
        strategies.add(new AIDealStrategy(gameType, 5));

        // 9. 正态分布策略（基础概率控制，作为兜底）
        strategies.add(new NormalDistributionStrategy(gameType));

        // 10. 连胜平衡策略（不参与 getTargetRank，只用于 adjustRank）
        // 注意：这个策略需要被添加到列表中，以便 adjustRank 能找到它
        strategies.add(new WinningBalanceStrategy(gameType));

        log.info("策略链初始化完成，共 {} 个策略", strategies.size());
    }

    /**
     * 创建保底策略
     * 使用动态 Provider，根据 context 决定保底牌型
     */
    private GuaranteeDealStrategy createGuaranteeStrategy() {
        // 使用动态 Provider 创建保底策略
        // 根据游戏类型和上下文动态决定保底牌型
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
     * 获取最终的目标牌型
     *
     * @param context 发牌上下文（包含玩家所有数据）
     * @return 目标牌型，如果没有策略触发则返回 null
     */
    public HandRank getTargetRank(DealContext context) {
        if (context == null) {
            log.warn("DealContext 为空");
            return null;
        }

        for (DealStrategy strategy : strategies) {
            if (!strategy.isEnabled()) {
                continue;
            }

            // 检查策略是否适用于当前游戏类型
            if (strategy.getGameType() != context.getGameType() &&
                    strategy.getGameType() != GameType.ALL) {
                continue;
            }

            HandRank rank = null;

            try {
                rank = strategy.getTargetRank(context);
            } catch (Exception e) {
                log.error("策略[{}]执行异常", strategy.getName(), e);
                continue;
            }

            if (rank != null) {
                log.debug("策略[{}]提供目标牌型: {}, 玩家: {}",
                        strategy.getName(), rank.getName(), context.getPlayerId());
                return rank;
            }
        }

        log.debug("没有策略触发，玩家: {}", context.getPlayerId());
        return null;
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

        // 查找连胜平衡策略
        for (DealStrategy strategy : strategies) {
            if (strategy instanceof WinningBalanceStrategy && strategy.isEnabled()) {
                WinningBalanceStrategy balanceStrategy = (WinningBalanceStrategy) strategy;
                int winCount = context.getConsecutiveWins();
                return balanceStrategy.getAdjustedRank(originalRank, winCount);
            }
        }

        return originalRank;
    }

    /**
     * 获取策略链（用于调试）
     */
    public List<DealStrategy> getStrategies() {
        return new ArrayList<>(strategies);
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