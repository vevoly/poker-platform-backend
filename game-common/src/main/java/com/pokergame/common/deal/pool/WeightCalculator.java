package com.pokergame.common.deal.pool;

import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 权重计算器
 *
 * 支持：
 * - 基于玩家状态的动态权重
 * - 基于全局概率的调整
 * - 临时活动加成
 *
 * @author poker-platform
 */
@Slf4j
public class WeightCalculator {

    private static final WeightCalculator INSTANCE = new WeightCalculator();

    private final Map<String, WeightModifier> modifiers = new ConcurrentHashMap<>();

    private WeightCalculator() {}

    public static WeightCalculator getInstance() {
        return INSTANCE;
    }

    /**
     * 注册权重修正器
     */
    public void registerModifier(String name, WeightModifier modifier) {
        modifiers.put(name, modifier);
        log.info("注册权重修正器: {}", name);
    }

    /**
     * 计算调整后的权重
     */
    public double calculateWeight(HandRankPool pool, HandRank rank,
                                  HandRankPool.HandRankEntry entry,
                                  Map<String, Object> context) {
        double weight = entry.getEffectiveWeight();

        for (WeightModifier modifier : modifiers.values()) {
            if (modifier.shouldApply(pool.getGameType(), rank, context)) {
                weight = modifier.modify(weight, context);
                log.debug("权重修正器[{}]应用: {} -> {}",
                        modifier.getName(), entry.getEffectiveWeight(), weight);
            }
        }

        return Math.max(0, weight);
    }

    /**
     * 权重修正器接口
     */
    public interface WeightModifier {
        String getName();
        boolean shouldApply(GameType gameType, HandRank rank, Map<String, Object> context);
        double modify(double originalWeight, Map<String, Object> context);
    }

    /**
     * 连败补偿修正器
     */
    public static class LossCompensationModifier implements WeightModifier {
        @Override
        public String getName() { return "连败补偿"; }

        @Override
        public boolean shouldApply(GameType gameType, HandRank rank, Map<String, Object> context) {
            Integer lossCount = (Integer) context.get("consecutiveLosses");
            return lossCount != null && lossCount >= 3;
        }

        @Override
        public double modify(double originalWeight, Map<String, Object> context) {
            int lossCount = (Integer) context.get("consecutiveLosses");
            // 连败越多，高牌型权重增加越多
            double boost = Math.min(0.5, lossCount * 0.05);
            return originalWeight * (1 + boost);
        }
    }

    /**
     * VIP加成修正器
     */
    public static class VipBoostModifier implements WeightModifier {
        @Override
        public String getName() { return "VIP加成"; }

        @Override
        public boolean shouldApply(GameType gameType, HandRank rank, Map<String, Object> context) {
            Integer vipLevel = (Integer) context.get("vipLevel");
            return vipLevel != null && vipLevel > 0;
        }

        @Override
        public double modify(double originalWeight, Map<String, Object> context) {
            int vipLevel = (Integer) context.get("vipLevel");
            double boost = vipLevel * 0.05;  // 每级5%
            return originalWeight * (1 + boost);
        }
    }
}
