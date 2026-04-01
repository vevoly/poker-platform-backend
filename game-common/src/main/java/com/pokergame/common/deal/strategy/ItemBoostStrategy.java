package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.item.ActiveItem;
import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 道具加成策略 - 纯计算，无状态
 * 依赖 DealContext 中的 ActiveItem 数据
 *
 * @author poker-platform
 */
@Slf4j
public class ItemBoostStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;

    public ItemBoostStrategy(GameType gameType) {
        this(gameType, true);
    }

    public ItemBoostStrategy(GameType gameType, boolean isActive) {
        this.gameType = gameType;
        this.isActive = isActive;
    }

    @Override
    public String getName() {
        return "道具加成策略";
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
        List<ActiveItem> items = context.getActiveItems();
        if (items == null || items.isEmpty()) {
            return null;
        }

        // 1. 优先处理保底道具
        HandRank guaranteeRank = getGuaranteeRankFromItems(items);
        if (guaranteeRank != null) {
            log.debug("玩家{}触发保底道具，目标牌型: {}",
                    context.getPlayerId(), guaranteeRank.getName());
            return guaranteeRank;
        }

        // 2. 计算总加成系数
        double totalBoost = items.stream()
                .mapToDouble(ActiveItem::getBoostRate)
                .sum();

        if (totalBoost <= 0) {
            return null;
        }

        // 3. 概率判定
        double probability = 0.1 + totalBoost * 0.5;
        probability = Math.min(probability, 0.8);

        if (ThreadLocalRandom.current().nextDouble() >= probability) {
            return null;
        }

        // 4. 返回加成后的牌型
        HandRank boostedRank = getBoostedRank(totalBoost);
        log.debug("玩家{}道具加成触发，总加成={}, 目标牌型={}",
                context.getPlayerId(), totalBoost, boostedRank.getName());
        return boostedRank;
    }

    /**
     * 从道具列表中获取保底牌型 - 纯计算
     */
    private HandRank getGuaranteeRankFromItems(List<ActiveItem> items) {
        for (ActiveItem item : items) {
            String target = item.getGuaranteeTarget();
            if (target != null) {
                return parseHandRank(target);
            }
        }
        return null;
    }

    /**
     * 解析牌型名称 - 纯计算
     */
    private HandRank parseHandRank(String target) {
        try {
            return HandRank.valueOf(gameType.name() + "_" + target);
        } catch (IllegalArgumentException e) {
            log.warn("未知的牌型: {}", target);
            return null;
        }
    }

    /**
     * 根据加成系数获取目标牌型 - 纯计算
     */
    private HandRank getBoostedRank(double totalBoost) {
        double random = ThreadLocalRandom.current().nextDouble();

        switch (gameType) {
            case DOUDIZHU:
                if (random < 0.1 + totalBoost) {
                    return HandRank.DOUDIZHU_ROCKET;
                }
                if (random < 0.2 + totalBoost) {
                    return HandRank.DOUDIZHU_BOMB;
                }
                return HandRank.DOUDIZHU_STRAIGHT;
            case TEXAS:
                if (random < 0.05 + totalBoost) {
                    return HandRank.TEXAS_ROYAL_FLUSH;
                }
                if (random < 0.1 + totalBoost) {
                    return HandRank.TEXAS_STRAIGHT_FLUSH;
                }
                return HandRank.TEXAS_FLUSH;
            case BULL:
                if (random < 0.08 + totalBoost) {
                    return HandRank.BULL_FIVE_SMALL;
                }
                if (random < 0.15 + totalBoost) {
                    return HandRank.BULL_BULL;
                }
                return HandRank.BULL_7;
            default:
                return HandRank.DOUDIZHU_JUNK;
        }
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
