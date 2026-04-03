package com.pokergame.core.temp;

/**
 * 玩家加入事件
 */
public class PlayerJoinEvent extends GameEvent {

    /** 玩家ID */
    private final long playerId;
    /** 玩家名称 */
    private final String playerName;

    public PlayerJoinEvent(String roomId, long playerId, String playerName) {
        super(GameEventType.PLAYER_JOIN, roomId, null);
        this.playerId = playerId;
        this.playerName = playerName;
    }
}
