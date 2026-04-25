package com.pokergame.game.doudizhu.room;



import com.iohao.game.widget.light.room.Room;
import com.iohao.game.widget.light.room.RoomService;
import com.iohao.game.widget.light.room.flow.RoomCreateContext;
import com.iohao.game.widget.light.room.flow.RoomCreator;
import com.iohao.game.widget.light.room.operation.OperationFactory;
import com.iohao.game.widget.light.room.operation.OperationService;
import com.pokergame.core.base.BaseRoomService;
import com.pokergame.game.doudizhu.enums.InternalOperation;
import com.pokergame.game.doudizhu.handler.PassOperationHandler;
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
public final class DoudizhuRoomService extends BaseRoomService {

    private static final DoudizhuRoomService INSTANCE = new DoudizhuRoomService();
    private final AtomicLong roomIdGenerator = new AtomicLong(System.currentTimeMillis());

    private DoudizhuRoomService() {
        // 在此注册斗地主的 OperationHandler

        // 过牌
        operationFactory.mapping(InternalOperation.PASS, new PassOperationHandler());
        log.info("斗地主操作处理器注册完成，DoudizhuRoomService 初始化完成！");
    }

    public static DoudizhuRoomService me() {
        return INSTANCE;
    }

    @Override
    public DoudizhuRoom createRoom(RoomCreateContext createContext) {
        long roomId = roomIdGenerator.incrementAndGet();
        long ownerId = createContext.getCreatorUserId();
        int maxPlayers = createContext.getSpaceSize();

        DoudizhuRoom room = new DoudizhuRoom();
        room.setRoomId(roomId);
        room.setOwnerId(ownerId);
        room.setMaxPlayers(maxPlayers);
        room.setSpaceSize(maxPlayers);
        room.setRoomCreateContext(createContext);
        // 设置 OperationService 为当前实例（BaseRoomService 实现了 OperationService）
        room.setOperationService(this);

        // 将房间添加到映射中
        addRoom(room);

        log.info("创建斗地主房间: roomId={}, ownerId={}, maxPlayers={}", roomId, ownerId, maxPlayers);
        return room;
    }

    // ========== 类型安全的查询方法（协变返回） ==========

    @Override
    public DoudizhuRoom getUserRoom(long userId) {
        return (DoudizhuRoom) super.getUserRoom(userId);
    }

    @Override
    public DoudizhuRoom getRoom(long roomId) {
        return (DoudizhuRoom) super.getRoom(roomId);
    }

    /**
     * 添加斗地主玩家到房间（类型安全）
     * @param room 房间
     * @param player 玩家
     */
    public void addPlayer(DoudizhuRoom room, DoudizhuPlayer player) {
        super.addPlayer(room, player);
    }

    /**
     * 移除玩家（通过 userId）
     * @param room 房间
     * @param userId 玩家ID
     */
    public void removePlayer(DoudizhuRoom room, long userId) {
        super.removePlayer(room, userId);
    }

    /**
     * 移除房间
     * @param room 房间
     */
    public void removeRoom(DoudizhuRoom room) {
        super.removeRoom(room);
    }
}
