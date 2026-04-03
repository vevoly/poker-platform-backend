package com.pokergame.core.room;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 房间管理器 - 单例，管理所有游戏房间
 *
 * @author poker-platform
 */
@Slf4j
public class RoomManager {

    private static final RoomManager INSTANCE = new RoomManager();

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    private RoomManager() {}

    public static RoomManager getInstance() {
        return INSTANCE;
    }

    /**
     * 注册房间
     */
    public void registerRoom(GameRoom room) {
        rooms.put(room.getRoomId(), room);
        log.info("注册房间: roomId={}, gameType={}", room.getRoomId(), room.getGameType());
    }

    /**
     * 获取房间
     */
    @SuppressWarnings("unchecked")
    public <T extends GameRoom> T getRoom(String roomId) {
        return (T) rooms.get(roomId);
    }

    /**
     * 移除房间
     */
    public void removeRoom(String roomId) {
        GameRoom room = rooms.remove(roomId);
        if (room != null) {
            log.info("移除房间: roomId={}", roomId);
        }
    }

    /**
     * 获取房间数量
     */
    public int getRoomCount() {
        return rooms.size();
    }

    /**
     * 检查房间是否存在
     */
    public boolean exists(String roomId) {
        return rooms.containsKey(roomId);
    }
}
