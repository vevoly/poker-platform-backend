package com.pokergame.core.temp;

/**
 * 游戏事件类型枚举
 */
public enum GameEventType {
    /** 玩家加入 */
    PLAYER_JOIN,
    /** 玩家离开 */
    PLAYER_LEAVE,
    /** 游戏开始 */
    GAME_START,
    /** 游戏结束 */
    GAME_END,
    /** 回合开始 */
    TURN_START,
    /** 回合结束 */
    TURN_END,
    /** 玩家出牌 */
    PLAYER_PLAY,
    /** 玩家过牌 */
    PLAYER_PASS,
    /** 玩家超时 */
    PLAYER_TIMEOUT,
    /** 房间销毁 */
    ROOM_DESTROY
}
