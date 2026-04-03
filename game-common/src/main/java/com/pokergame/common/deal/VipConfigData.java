package com.pokergame.common.deal;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VIP配置数据 - 纯数据结构
 * 由 service-user 提供，通过 DealContext 传递
 *
 * @author poker-platform
 */
@Data
@Builder
public class VipConfigData {

    /** VIP等级对应的触发概率（1-9级） */
    private Map<Integer, Double> triggerProbabilities;

    /** VIP等级对应的加成系数（1-9级） */
    private Map<Integer, Double> boostRates;

    /** VIP等级对应的专属牌型触发概率（1-9级） */
    private Map<Integer, Double> specialProbabilities;

    /**
     * 创建默认配置
     */
    public static VipConfigData createDefault() {
        Map<Integer, Double> triggers = new ConcurrentHashMap<>();
        Map<Integer, Double> boosts = new ConcurrentHashMap<>();
        Map<Integer, Double> specials = new ConcurrentHashMap<>();

        // 默认配置
        double[] triggerDefaults = {0.08, 0.10, 0.12, 0.14, 0.16, 0.18, 0.20, 0.22, 0.25};
        double[] boostDefaults = {0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50};
        double[] specialDefaults = {0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.05, 0.08, 0.12};

        for (int i = 1; i <= 9; i++) {
            triggers.put(i, triggerDefaults[i - 1]);
            boosts.put(i, boostDefaults[i - 1]);
            specials.put(i, specialDefaults[i - 1]);
        }

        return VipConfigData.builder()
                .triggerProbabilities(triggers)
                .boostRates(boosts)
                .specialProbabilities(specials)
                .build();
    }

    /**
     * 获取触发概率
     */
    public double getTriggerProbability(int vipLevel) {
        if (triggerProbabilities == null) return 0;
        return triggerProbabilities.getOrDefault(vipLevel, 0.0);
    }

    /**
     * 获取加成系数
     */
    public double getBoostRate(int vipLevel) {
        if (boostRates == null) return 0;
        return boostRates.getOrDefault(vipLevel, 0.0);
    }

    /**
     * 获取专属牌型触发概率
     */
    public double getSpecialProbability(int vipLevel) {
        if (specialProbabilities == null) return 0;
        return specialProbabilities.getOrDefault(vipLevel, 0.0);
    }
}
