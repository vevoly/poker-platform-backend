package com.pokergame.common.deal;

import com.pokergame.common.event.ActiveEvent;
import com.pokergame.common.item.ActiveItem;
import com.pokergame.common.deal.strategy.NormalDistributionStrategy;
import com.pokergame.common.game.GameType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 多玩家发牌上下文
 * 用于发牌器层，包含所有玩家的数据
 *
 * @author poker-platform
 */
@Data
@Builder
public class MultiPlayerDealContext {

    // ==================== 基础信息 ====================
    private GameType gameType;
    private int playerCount;
    private int landlordIndex;              // 地主索引（斗地主用）

    // ==================== 所有玩家的数据 ====================
    private List<Long> playerIds;

    // 玩家统计数据
    private List<Integer> consecutiveLosses;
    private List<Integer> consecutiveWins;
    private List<Integer> totalGames;
    private List<Integer> vipLevels;
    private List<Integer> remainingBonusGames;
    private List<Long> lastLoginTimes;
    private List<Long> registerTimes;
    private List<Boolean> rookieFlags;

    // AI相关
    private List<Boolean> aiFlags;
    private List<Integer> aiDifficulties;

    // 道具/活动数据
    private List<List<ActiveItem>> activeItemsList;
    private List<List<ActiveEvent>> activeEventsList;

    // 全局统计（用于正态分布）
    private NormalDistributionStrategy.GlobalStatistics globalStatistics;

    // 扩展数据
    private Object extra;

    // ==================== 便捷方法 ====================

    /**
     * 获取单个玩家的上下文
     */
    public DealContext getPlayerContext(int playerIndex) {
        return DealContext.builder()
                .playerId(getPlayerId(playerIndex))
                .playerIndex(playerIndex)
                .gameType(gameType)
                .consecutiveLosses(getValueInt(consecutiveLosses, playerIndex))
                .consecutiveWins(getValueInt(consecutiveWins, playerIndex))
                .totalGames(getValueInt(totalGames, playerIndex))
                .vipLevel(getValueInt(vipLevels, playerIndex))
                .remainingBonusGames(getValueInt(remainingBonusGames, playerIndex))
                .lastLoginTime(getValueLong(lastLoginTimes, playerIndex))
                .registerTime(getValueLong(registerTimes, playerIndex))
                .isRookie(getValueBoolean(rookieFlags, playerIndex))
                .isAI(getValueBoolean(aiFlags, playerIndex))
                .aiDifficulty(getValueInt(aiDifficulties, playerIndex))
                .activeItems(getValue(activeItemsList, playerIndex))
                .activeEvents(getValue(activeEventsList, playerIndex))
                .globalStatistics(globalStatistics)
                .build();
    }

    private long getPlayerId(int index) {
        return playerIds != null && index < playerIds.size() ? playerIds.get(index) : 0;
    }

    private <T> T getValue(List<T> list, int index) {
        return list != null && index < list.size() ? list.get(index) : null;
    }

    private int getValueInt(List<?> list, int index) {
        Object value = list != null && index < list.size() ? list.get(index) : 0;
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        } else {
            return 0;
        }
    }

    private long getValueLong(List<?> list, int index) {
        Object value = list != null && index < list.size() ? list.get(index) : 0;
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else {
            return 0;
        }
    }

    private boolean getValueBoolean(List<?> list, int index) {
        Object value = list != null && index < list.size() ? list.get(index) : false;
        return value instanceof Boolean ? (Boolean) value : false;
    }
}
