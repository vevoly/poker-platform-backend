package com.pokergame.core.trustee;

/**
 * 托管决策接口
 * @param <R> 房间类型
 * @param <P> 玩家类型
 */
public interface TrusteeshipDecision<R, P> {

    /**
     * 执行一次自动操作（出牌或过牌）
     * @param room 房间对象
     * @param player 当前玩家
     */
    void act(R room, P player);
}
