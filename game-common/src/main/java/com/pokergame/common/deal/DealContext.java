package com.pokergame.common.deal;

import com.pokergame.common.deal.strategy.NormalDistributionStrategy;
import com.pokergame.common.game.GameType;
import com.pokergame.common.item.ActiveItem;
import com.pokergame.common.event.ActiveEvent;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 发牌上下文 - 包含发牌所需的所有玩家数据
 * 由游戏逻辑服从各服务获取后组装
 */
@Data
@Builder
public class DealContext {

    // ==================== 基础信息 ====================

    /** 玩家ID */
    private long playerId;
    /** 玩家在房间中的索引 */
    private int playerIndex;
    /** 游戏类型 */
    private GameType gameType;

    // ==================== 玩家统计数据（从 service-user 获取） ====================

    /** 连败次数 */
    private int consecutiveLosses;
    /** 连胜次数 */
    private int consecutiveWins;
    /** 总局数 */
    private int totalGames;
    /** VIP等级 */
    private int vipLevel;
    /** VIP配置数据（从 service-user 获取） */
    private VipConfigData vipConfig;
    /** 剩余回归奖励局数（从 service-user 获取） */
    private int remainingBonusGames;
    /** 最后登录时间 */
    private long lastLoginTime;
    /** 全局统计信息（用于正态分布策略） */
    private NormalDistributionStrategy.GlobalStatistics globalStatistics;
    /** 是否为新手（由 service-user 计算） */
    private boolean isRookie;
    /** 注册时间（毫秒，可选） */
    private long registerTime;

    // ==================== 道具数据（从 service-item 获取） ====================

    /** 生效中的道具列表 */
    private List<ActiveItem> activeItems;

    // ==================== 活动数据（从 service-activity 获取） ====================

    /** 生效中的活动列表 */
    private List<ActiveEvent> activeEvents;

    // ==================== AI相关 ====================

    // 是否为AI
    private boolean isAI;
    private int aiDifficulty;           // AI难度 1-10

    // 扩展数据
    private Map<String, Object> extra;

    // ==================== 便捷方法 ====================

    /**
     * 计算流失天数
     */
    public int getDaysAway() {
        if (lastLoginTime <= 0) return 0;
        return (int) ((System.currentTimeMillis() - lastLoginTime) / (24 * 60 * 60 * 1000));
    }

    /**
     * 检查是否有指定道具
     */
    public boolean hasItem(String itemId) {
        if (activeItems == null) return false;
        return activeItems.stream()
                .anyMatch(item -> item.getItemId().equals(itemId));
    }

    /**
     * 检查是否有保底道具
     */
    public boolean hasGuaranteeItem() {
        if (activeItems == null) return false;
        return activeItems.stream()
                .anyMatch(item -> "guarantee".equals(item.getEffects().get("type")));
    }

    /**
     * 获取道具总加成系数
     */
    public double getTotalItemBoost() {
        if (activeItems == null) return 0;
        return activeItems.stream()
                .mapToDouble(ActiveItem::getBoostRate)
                .sum();
    }

    /**
     * 获取胜率（需外部传入）
     */
    public double getWinRate() {
        if (extra == null) return 0;
        Object winRate = extra.get("winRate");
        return winRate instanceof Number ? ((Number) winRate).doubleValue() : 0;
    }

    /**
     * 计算注册天数
     */
    public int getDaysSinceRegister() {
        if (registerTime <= 0) return Integer.MAX_VALUE;
        return (int) ((System.currentTimeMillis() - registerTime) / (24 * 60 * 60 * 1000));
    }
}
