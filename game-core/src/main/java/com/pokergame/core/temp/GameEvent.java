package com.pokergame.core.temp;

import lombok.Getter;

/**
 * 游戏事件 - 抽象基类
 *
 * @author poker-platform
 */
@Getter
public abstract class GameEvent {

    /**
     * 事件类型
     */
    private final GameEventType type;

    /**
     * 房间ID
     */
    private final String roomId;

    /**
     * 触发时间
     */
    private final long timestamp;

    /**
     * 事件数据
     */
    private final Object data;

    protected GameEvent(GameEventType type, String roomId, Object data) {
        this.type = type;
        this.roomId = roomId;
        this.timestamp = System.currentTimeMillis();
        this.data = data;
    }
}

