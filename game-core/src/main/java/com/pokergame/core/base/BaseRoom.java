package com.pokergame.core.base;

import com.iohao.game.widget.light.room.SimpleRoom;
import com.pokergame.core.trustee.TrusteeshipManager;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房间基类，继承 ioGame 的 SimpleRoom，添加所有游戏房间共有的属性。
 * 各游戏的房间类应继承此类。
 * 注意：本类不包含任何游戏逻辑状态（如回合、出牌记录等），那些应由 BaseGameStateManager 管理。
 */
@Getter
@Setter
public abstract class BaseRoom extends SimpleRoom {

    /** 房主ID（创建房间的玩家） */
    private long ownerId;

    /** 房间最大玩家数 */
    private int maxPlayers;

    /**
     * 游戏状态字符串（仅用于客户端展示，如大厅房间列表）。
     * 实际游戏逻辑状态应使用 BaseGameStateManager 中的枚举。
     */
    private String gameStatus;

    // ==================== 抽象方法：子类必须实现 ====================

    /**
     * 更新游戏状态（同时更新房间展示字符串和内部游戏逻辑状态）
     * @param status 游戏状态枚举
     */
    public abstract void updateGameStatus(Enum<?> status);

    /**
     * 获取游戏状态枚举
     */
    public abstract Enum<?> getGameStatusEnum();

    /**
     * 获取托管管理器（子类实现，返回具体托管管理器实例）
     */
    public abstract TrusteeshipManager getTrusteeshipManager();

    /**
     * 判断指定玩家是否为当前回合玩家
     * @param userId 玩家ID
     * @return true 如果是当前回合玩家
     */
    public abstract boolean isCurrentPlayer(long userId);

    // ==================== 通用房间管理方法 ====================

    /**
     * 检查所有玩家是否都已准备（仅用于房间准备阶段）
     * @return true 如果所有玩家都已准备
     */
    public boolean isAllReady() {
        if (getPlayerMap().isEmpty()) {
            return false;
        }
        return getPlayerMap().values().stream().allMatch(p -> {
            BasePlayer bp = (BasePlayer) p;
            return bp.isReady();
        });
    }

    /**
     * 重置房间状态（清空玩家，重置基础属性）
     * 子类应重写此方法并调用 super.reset()
     */
    public void reset() {
        getPlayerMap().clear();
        getRealPlayerMap().clear();
        getRobotMap().clear();
        getPlayerSeatMap().clear();
        gameStatus = "WAITING";
        ownerId = 0;
        maxPlayers = 0;
    }

    /**
     * 判断房间是否为空
     * @return
     */
    public boolean isEmpty() {
        return getPlayerMap().isEmpty();
    }

    /**
     * 判断房间是否已满
     * @return
     */
    public boolean isFull() {
        return getPlayerMap().size() >= maxPlayers;
    }

    /**
     * 获取玩家数量
     * @return
     */
    public int getPlayerCount() {
        return getPlayerMap().size();
    }

    /**
     * 获取玩家出牌顺序列表
     * @return
     */
    public List<Long> getPlayerOrder() {
        return this.getPlayerSeatMap().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}