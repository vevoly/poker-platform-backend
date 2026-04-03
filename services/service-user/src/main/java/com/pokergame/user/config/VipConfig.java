package com.pokergame.user.config;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VIP策略配置 - 支持热更新
 *
 * @author poker-platform
 */
@Data
public class VipConfig {

    /** VIP等级对应的触发概率 */
    private Map<Integer, Double> triggerProbabilities;

    /** VIP等级对应的加成系数 */
    private Map<Integer, Double> boostRates;

    /** VIP等级对应的专属牌型触发概率 */
    private Map<Integer, Double> specialProbabilities;

    /** 各等级好牌概率基准值 */
    private Map<Integer, Double> baseGoodRankProbabilities;

    public VipConfig() {
        this.triggerProbabilities = new ConcurrentHashMap<>();
        this.boostRates = new ConcurrentHashMap<>();
        this.specialProbabilities = new ConcurrentHashMap<>();
        this.baseGoodRankProbabilities = new ConcurrentHashMap<>();
        initDefault();
    }

    /**
     * 初始化默认配置（可被配置文件覆盖）
     */
    private void initDefault() {
        // VIP1-3: 较低概率
        for (int i = 1; i <= 3; i++) {
            triggerProbabilities.put(i, 0.05 + (i - 1) * 0.02);
            boostRates.put(i, 0.05 + (i - 1) * 0.02);
            specialProbabilities.put(i, 0.0);
            baseGoodRankProbabilities.put(i, 0.05);
        }

        // VIP4-6: 中等概率
        for (int i = 4; i <= 6; i++) {
            triggerProbabilities.put(i, 0.12 + (i - 4) * 0.03);
            boostRates.put(i, 0.12 + (i - 4) * 0.03);
            specialProbabilities.put(i, 0.0);
            baseGoodRankProbabilities.put(i, 0.10);
        }

        // VIP7-9: 较高概率 + 专属牌型
        for (int i = 7; i <= 9; i++) {
            triggerProbabilities.put(i, 0.22 + (i - 7) * 0.04);
            boostRates.put(i, 0.22 + (i - 7) * 0.04);
            specialProbabilities.put(i, 0.03 + (i - 7) * 0.02);
            baseGoodRankProbabilities.put(i, 0.18);
        }
    }

    public double getTriggerProbability(int vipLevel) {
        return triggerProbabilities.getOrDefault(vipLevel, 0.0);
    }

    public double getBoostRate(int vipLevel) {
        return boostRates.getOrDefault(vipLevel, 0.0);
    }

    public double getSpecialProbability(int vipLevel) {
        return specialProbabilities.getOrDefault(vipLevel, 0.0);
    }

    public double getBaseGoodRankProbability(int vipLevel) {
        return baseGoodRankProbabilities.getOrDefault(vipLevel, 0.05);
    }
}
