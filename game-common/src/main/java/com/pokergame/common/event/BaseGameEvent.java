package com.pokergame.common.event;

import com.pokergame.common.enums.GameEventType;
import com.pokergame.common.game.GameType;
import lombok.Data;

import java.io.Serializable;

/**
 * 游戏事件基类 - 所有游戏事件的父类
 *
 * 设计原则：
 * - 实现 Serializable，支持跨进程传输
 * - 包含所有事件通用的字段
 * - 子类可以扩展特定字段
 *
 * @author poker-platform
 */
@Data
public abstract class BaseGameEvent implements Serializable {

    /** 事件ID（全局唯一） */
    private final String eventId;

    /** 事件类型 */
    private final GameEventType eventType;

    /** 游戏类型 */
    private final GameType gameType;

    /** 房间ID */
    private final String roomId;

    /** 触发时间 */
    private final long timestamp;

    /** 来源逻辑服 */
    private final String sourceServer;

    public BaseGameEvent(GameEventType eventType, GameType gameType, String roomId) {
        this.eventId = generateEventId();
        this.eventType = eventType;
        this.gameType = gameType;
        this.roomId = roomId;
        this.timestamp = System.currentTimeMillis();
        this.sourceServer = getCurrentServerId();
    }

    private static String generateEventId() {
        return System.currentTimeMillis() + "-" + Thread.currentThread().getId() + "-" + System.nanoTime();
    }

    private static String getCurrentServerId() {
        // 从环境变量或配置中获取
        return System.getProperty("server.id", "unknown");
    }
}
