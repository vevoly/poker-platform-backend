package com.pokergame.game.doudizhu.enums;

/**
 * 游戏状态枚举
 *
 * 状态转换规则：
 * WAITING -> READY -> BIDDING -> PLAYING -> FINISHED
 *
 * @author poker-platform
 */
public enum DoudizhuGameStatus {

    /** 等待玩家（房间刚创建，等待玩家加入） */
    WAITING,

    /** 准备中（玩家已就位，等待准备） */
    READY,

    /** 叫地主中（发牌完成，等待叫地主） */
    BIDDING,

    /** 游戏中（地主确定，正在出牌） */
    PLAYING,

    /** 已结束（游戏结束，等待销毁） */
    FINISHED;

    /**
     * 判断是否可以开始游戏
     *
     * @return true 如果状态是 READY 或 WAITING
     */
    public boolean canStart() {
        return this == READY || this == WAITING;
    }

    /**
     * 判断是否可以叫地主
     *
     * @return true 如果状态是 BIDDING
     */
    public boolean canBid() {
        return this == BIDDING;
    }

    /**
     * 判断是否可以出牌
     *
     * @return true 如果状态是 PLAYING
     */
    public boolean canPlay() {
        return this == PLAYING;
    }
}
