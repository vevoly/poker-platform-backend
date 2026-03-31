package com.pokergame.common.deal.pool;


import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 牌型池 - 可扩展的核心数据结构
 *
 * 支持：
 * - 权重配置
 * - 继承其他牌型池
 * - 动态权重调整
 * - todo 运行时热更新
 *
 * @author poker-platform
 */
@Slf4j
@Data
public class HandRankPool {

    /** 牌型池ID */
    private final String poolId;
    /** 牌型池名称 */
    private final String name;
    /** 牌型池游戏类型 */
    private final GameType gameType;
    /** 牌型池继承的父牌型池ID */
    private final String parentPoolId;          // 支持继承
    /** 牌型池牌型条目 */
    private final List<HandRankEntry> entries;
    /** 牌型池元数据 */
    private final Map<String, Object> metadata;

    // 运行时数据
    private final AtomicReference<WeightDistribution> weightDistribution;
    // 临时权重修正
    private final Map<String, Double> tempWeightModifiers;

    private HandRankPool(Builder builder) {
        this.poolId = builder.poolId;
        this.name = builder.name;
        this.gameType = builder.gameType;
        this.parentPoolId = builder.parentPoolId;
        this.entries = new ArrayList<>(builder.entries);
        this.metadata = builder.metadata;
        this.tempWeightModifiers = new ConcurrentHashMap<>();

        // 初始化权重分布
        this.weightDistribution = new AtomicReference<>(buildWeightDistribution());
    }

    /**
     * 随机选择一个牌型
     */
    public HandRank selectRandom() {
        WeightDistribution dist = weightDistribution.get();
        double target = ThreadLocalRandom.current().nextDouble() * dist.totalWeight;

        for (WeightEntry entry : dist.entries) {
            if (target <= entry.cumulativeWeight) {
                log.debug("牌型池[{}]选中牌型: {} (权重: {})",
                        poolId, entry.rank.getName(), entry.weight);
                return entry.rank;
            }
        }

        return dist.entries.length > 0 ? dist.entries[0].rank : null;
    }

    /**
     * 动态调整某个牌型的权重
     */
    public void adjustWeight(HandRank rank, double delta, int durationSeconds) {
        // 找到对应的条目
        for (HandRankEntry entry : entries) {
            if (entry.rank == rank) {
                double newWeight = Math.max(0, entry.baseWeight + delta);
                entry.tempWeight = newWeight;
                tempWeightModifiers.put(rank.name(), delta);

                // 重建权重分布
                weightDistribution.set(buildWeightDistribution());

                // 设置定时恢复
                if (durationSeconds > 0) {
                    scheduleWeightReset(rank, entry.baseWeight, durationSeconds);
                }
                break;
            }
        }
    }

    /**
     * 定时恢复权重
     * @param rank
     * @param originalWeight
     * @param durationSeconds
     */
    private void scheduleWeightReset(HandRank rank, double originalWeight, int durationSeconds) {
        // 异步任务，在指定时间后恢复权重
        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(durationSeconds * 1000L);
                resetWeight(rank, originalWeight);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * 重置权重
     * @param rank
     * @param originalWeight
     */
    private void resetWeight(HandRank rank, double originalWeight) {
        for (HandRankEntry entry : entries) {
            if (entry.rank == rank) {
                entry.tempWeight = originalWeight;
                tempWeightModifiers.remove(rank.name());
                weightDistribution.set(buildWeightDistribution());
                log.debug("牌型池[{}]恢复牌型权重: {} -> {}", poolId, rank.getName(), originalWeight);
                break;
            }
        }
    }

    /**
     * 构建权重分布
     * @return
     */
    private WeightDistribution buildWeightDistribution() {
        List<WeightEntry> weightEntries = new ArrayList<>();
        double cumulative = 0;

        for (HandRankEntry entry : entries) {
            double weight = entry.getEffectiveWeight();
            cumulative += weight;
            weightEntries.add(new WeightEntry(entry.rank, weight, cumulative));
        }

        return new WeightDistribution(weightEntries.toArray(new WeightEntry[0]), cumulative);
    }

    // ==================== 静态工厂方法 ====================

    public static Builder builder(String poolId, GameType gameType) {
        return new Builder(poolId, gameType);
    }

    // ==================== 内部类 ====================

    /**
     * 牌型条目
     */
    @Data
    public static class HandRankEntry {
        private final HandRank rank;
        private final double baseWeight;
        private double tempWeight;

        public HandRankEntry(HandRank rank, double weight) {
            this.rank = rank;
            this.baseWeight = weight;
            this.tempWeight = weight;
        }

        public double getEffectiveWeight() {
            return tempWeight;
        }
    }

    /**
     * 权重条目
     * @param rank
     * @param weight
     * @param cumulativeWeight
     */
    private record WeightEntry(HandRank rank, double weight, double cumulativeWeight) {}

    /**
     * 权重分布
     * @param entries
     * @param totalWeight
     */
    private record WeightDistribution(WeightEntry[] entries, double totalWeight) {}

    /**
     * 牌型池构建器
     */
    public static class Builder {
        private final String poolId;
        private final GameType gameType;
        private String name;
        private String parentPoolId;
        private final List<HandRankEntry> entries = new ArrayList<>();
        private final Map<String, Object> metadata = new HashMap<>();

        public Builder(String poolId, GameType gameType) {
            this.poolId = poolId;
            this.gameType = gameType;
            this.name = poolId;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder parent(String parentPoolId) {
            this.parentPoolId = parentPoolId;
            return this;
        }

        public Builder addRank(HandRank rank, double weight) {
            entries.add(new HandRankEntry(rank, weight));
            return this;
        }

        public Builder addRank(HandRank rank, double weight, Map<String, Object> extra) {
            HandRankEntry entry = new HandRankEntry(rank, weight);
            // 可以存储额外信息
            return this;
        }

        public Builder metadata(String key, Object value) {
            metadata.put(key, value);
            return this;
        }

        public HandRankPool build() {
            return new HandRankPool(this);
        }
    }
}
