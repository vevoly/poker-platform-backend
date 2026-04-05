package com.pokergame.game.doudizhu.room;



import com.iohao.game.widget.light.room.Room;
import com.iohao.game.widget.light.room.RoomService;
import com.iohao.game.widget.light.room.flow.RoomCreateContext;
import com.iohao.game.widget.light.room.flow.RoomCreator;
import com.iohao.game.widget.light.room.operation.OperationFactory;
import com.iohao.game.widget.light.room.operation.OperationService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jctools.maps.NonBlockingHashMap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 斗地主房间服务
 *
 * 实现 RoomService、OperationService、RoomCreator 接口
 *
 * 职责：
 * 1. 管理所有房间（房间映射）
 * 2. 管理玩家与房间的映射
 * 3. 创建房间
 * 4. 提供操作工厂用于注册 OperationHandler
 *
 * 设计模式：单例模式（通过静态内部类实现线程安全的懒加载）
 *
 * @author poker-platform
 */
@Slf4j
@Getter
public final class DoudizhuRoomService implements RoomService, OperationService, RoomCreator {

    // ==================== 房间管理 ====================

    /** 房间映射 roomId -> Room */
    final Map<Long, Room> roomMap = new NonBlockingHashMap<>();

    /** 玩家映射 userId -> roomId */
    final Map<Long, Long> userRoomMap = new NonBlockingHashMap<>();

    /** 操作工厂（用于注册 OperationHandler） */
    final OperationFactory operationFactory = OperationFactory.of();

    /** 房间ID生成器 */
    private final AtomicLong roomIdGenerator = new AtomicLong(System.currentTimeMillis());

    // ==================== 单例实现 ====================

    private DoudizhuRoomService() {
        log.info("DoudizhuRoomService 初始化完成");
    }

    /**
     * 获取单例实例
     *
     * @return DoudizhuRoomService 单例
     */
    public static DoudizhuRoomService me() {
        return Holder.ME;
    }

    /**
     * 静态内部类实现线程安全的懒加载单例
     */
    private static class Holder {
        static final DoudizhuRoomService ME = new DoudizhuRoomService();
    }

    // ==================== RoomCreator 接口实现 ====================

    /**
     * 创建房间
     *
     * @param createContext 房间创建上下文
     * @return 创建的房间实例
     */
    @Override
    public DoudizhuRoom createRoom(RoomCreateContext createContext) {
        // 生成房间ID
        long roomId = roomIdGenerator.incrementAndGet();

        // 获取创建者ID和房间大小
        long ownerId = createContext.getCreatorUserId();
        int maxPlayers = createContext.getSpaceSize();

        // 创建房间实例
        DoudizhuRoom room = new DoudizhuRoom();
        room.setRoomId(roomId);
        room.setOwnerId(ownerId);
        room.setMaxPlayers(maxPlayers);
        room.setSpaceSize(maxPlayers);
        room.setRoomCreateContext(createContext);
        room.setOperationService(this);

        log.info("创建房间: roomId={}, ownerId={}, maxPlayers={}", roomId, ownerId, maxPlayers);

        return room;
    }

    // ==================== 便捷方法 ====================

    /**
     * 获取斗地主房间
     *
     * @param roomId 房间ID
     * @return 斗地主房间，如果不存在返回 null
     */
    public DoudizhuRoom getDoudizhuRoom(long roomId) {
        return (DoudizhuRoom) roomMap.get(roomId);
    }

    /**
     * 获取玩家所在的房间
     *
     * @param userId 玩家ID
     * @return 玩家所在的房间，如果不存在返回 null
     */
    public DoudizhuRoom getUserRoom(long userId) {
        Long roomId = userRoomMap.get(userId);
        if (roomId == null) {
            return null;
        }
        return getDoudizhuRoom(roomId);
    }

    /**
     * 添加房间
     *
     * @param room 房间实例
     */
    public void addRoom(DoudizhuRoom room) {
        roomMap.put(room.getRoomId(), room);
        log.debug("添加房间映射: roomId={}", room.getRoomId());
    }

    /**
     * 移除房间
     *
     * @param room 房间实例
     */
    public void removeRoom(DoudizhuRoom room) {
        roomMap.remove(room.getRoomId());
        log.info("移除房间: roomId={}", room.getRoomId());
    }

    /**
     * 添加玩家到房间映射
     *
     * @param room 房间实例
     * @param player 玩家实例
     */
    public void addPlayer(DoudizhuRoom room, DoudizhuPlayer player) {
        userRoomMap.put(player.getUserId(), room.getRoomId());
        log.debug("添加玩家映射: userId={} -> roomId={}", player.getUserId(), room.getRoomId());
    }

    /**
     * 移除玩家映射
     *
     * @param userId 玩家ID
     */
    public void removePlayer(long userId) {
        userRoomMap.remove(userId);
        log.debug("移除玩家映射: userId={}", userId);
    }
}
