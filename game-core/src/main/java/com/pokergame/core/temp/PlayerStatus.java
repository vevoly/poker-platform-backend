package com.pokergame.core.temp;

/**
 * 玩家状态枚举
 *
 * @author poker-platform
 */
public enum PlayerStatus {

    /** 等待中 */
    WAITING,

    /** 游戏中 */
    PLAYING,

    /** 已出完牌 */
    FINISHED,

    /** 已离开 */
    LEFT;
}
