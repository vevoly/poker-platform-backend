package com.pokergame.common.deal;

import com.pokergame.common.deal.strategy.ItemBoostStrategy;
import com.pokergame.common.game.GameType;
import lombok.Builder;
import lombok.Data;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * 发牌上下文 - 包含发牌所需的所有玩家数据
 * 由游戏逻辑服从各服务获取后组装
 */
@Data
@Builder
public class DealContext {

    private long playerId;
    private GameType gameType;

    // 玩家统计数据（从 service-user 获取）
    private int consecutiveLosses;      // 连败次数
    private int consecutiveWins;        // 连胜次数
    private int totalGames;             // 总局数
    private int vipLevel;               // VIP等级
    private long lastLoginTime;         // 最后登录时间

    // 道具数据（从 service-item 获取）
    private List<ItemBoostStrategy.ActiveItem> activeItems;

    // 活动数据（从 service-activity 获取）
    private List<ActiveEvent> activeEvents;

    // 是否为AI
    private boolean isAI;
    private int aiDifficulty;           // AI难度 1-10

    // 是否为新手
    private boolean isRookie;

    // 扩展数据
    private Map<String, Object> extra;

    /**
     * 计算流失天数
     */
    public int getDaysAway() {
        if (lastLoginTime <= 0) return 0;
        return (int) ((System.currentTimeMillis() - lastLoginTime) / (24 * 60 * 60 * 1000));
    }

    /**
     * 获取道具总加成
     */
    public double getTotalItemBoost() {
        if (activeItems == null) return 0;
        return activeItems.stream()
                .mapToDouble(item -> item.getType().getBoostRate())
                .sum();
    }

    /**
     * 检查是否有保底道具
     */
    public boolean hasGuaranteeItem() {
        if (activeItems == null) return false;
        return activeItems.stream()
                .anyMatch(item -> item.getType() == ItemBoostStrategy.ItemType.BAODI_CARD);
    }
}
