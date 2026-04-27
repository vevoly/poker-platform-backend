package com.pokergame.core.trustee;

public interface TrusteeshipManager {
    /**
     * 设置玩家托管状态
     *
     * @param userId  玩家ID
     * @param enabled true: 托管, false: 取消托管
     */
    void setTrustee(long userId, boolean enabled);

    /**
     * 检查玩家是否托管中
     */
    boolean isTrustee(long userId);

    /**
     * 托管状态下自动执行一次操作（出牌/过牌）
     *
     * @param userId 玩家ID
     */
    void autoAct(long userId);

    /**
     * 取消托管（通常由玩家主动取消或重连时调用）
     */
    void cancelTrustee(long userId);

}
