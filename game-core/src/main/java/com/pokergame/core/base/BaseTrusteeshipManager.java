package com.pokergame.core.base;

import com.pokergame.core.trustee.TrusteeshipDecision;
import com.pokergame.core.trustee.TrusteeshipManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 托管管理器基类
 * @param <R> 房间类型
 */
@Slf4j
public abstract class BaseTrusteeshipManager<R, P> implements TrusteeshipManager {

    /** 房间实例 */
    protected final R room;

    /** 托管决策 */
    protected final TrusteeshipDecision<R, P> decision;

    /** 玩家托管状态 */
    protected final Map<Long, Boolean> trusteeMap = new ConcurrentHashMap<>();

    public BaseTrusteeshipManager(R room, TrusteeshipDecision<R, P> decision) {
        this.room = room;
        this.decision = decision;
    }

    /**
     * 设置玩家托管状态
     * @param userId 玩家ID
     * @param enabled true:开启托管, false:取消托管
     */
    @Override
    public void setTrustee(long userId, boolean enabled) {
        boolean old = isTrustee(userId);
        if (old == enabled) return;
        if (enabled) {
            trusteeMap.put(userId, true);
            onTrusteeStart(userId);
        } else {
            trusteeMap.remove(userId);
            onTrusteeEnd(userId);
        }
    }

    @Override
    public boolean isTrustee(long userId) {
        return trusteeMap.getOrDefault(userId, false);
    }

    /**
     * 执行一次自动操作（当轮到托管玩家时调用）
     * 具体决策由子类通过策略实现
     * @param userId 玩家ID
     */
    @Override
    public void autoAct(long userId) {
        if (!isTrustee(userId)) return;
        // 获取玩家对象的方法由子类提供
        P player = getPlayer(userId);
        if (player != null && isCurrentTurn(player)) {
            decision.act(room, player);
        }
    }

    @Override
    public void cancelTrustee(long userId) {
        setTrustee(userId, false);
    }

    /**
     * 托管开始时触发的钩子（子类可重写，例如广播状态变化）
     */
    protected void onTrusteeStart(long userId) {
        // 默认空实现
    }

    /**
     * 托管结束时触发的钩子
     */
    protected void onTrusteeEnd(long userId) {
        // 默认空实现
    }

    /**
     * 获取玩家对象
     * @param userId 玩家ID
     * @return 玩家对象
     */
    protected abstract P getPlayer(long userId);

    /**
     * 判断当前轮到该玩家
     * @param player 玩家对象
     * @return true:是当前轮玩家, false:不是当前轮玩家
     */
    protected abstract boolean isCurrentTurn(P player);
}
