package com.pokergame.core.base;

import com.iohao.game.widget.light.room.Room;
import com.iohao.game.widget.light.room.RoomService;
import com.iohao.game.widget.light.room.flow.RoomCreateContext;
import com.iohao.game.widget.light.room.flow.RoomCreator;
import com.iohao.game.widget.light.room.operation.OperationFactory;
import com.iohao.game.widget.light.room.operation.OperationService;

import java.util.Collection;
import java.util.Map;

/**
 * 房间服务基类，组合 ioGame 的 SimpleRoomService 实现房间与玩家的映射管理。
 * 各游戏（斗地主、德州、牛牛）的房间服务应继承此类，并实现 createRoom 方法。
 *
 * @author poker-platform
 */
public abstract class BaseRoomService implements OperationService, RoomCreator {

    /** ioGame 默认房间服务实现 */
    private final RoomService delegate = RoomService.of();

    /** ioGame 操作工厂 */
    protected final OperationFactory operationFactory = OperationFactory.of();

    // ========== 查询方法（返回 BaseRoom 类型，子类可强转） ==========

    public BaseRoom getUserRoom(long userId) {
        return (BaseRoom) delegate.getRoomByUserId(userId);
    }

    public BaseRoom getRoom(long roomId) {
        return (BaseRoom) delegate.getRoom(roomId);
    }

    // ========== 委托方法（参数使用 BaseRoom，内部调用 delegate） ==========

    public void addRoom(BaseRoom room) {
        delegate.addRoom(room);
    }

    public void removeRoom(BaseRoom room) {
        delegate.removeRoom(room);
    }

    public void addPlayer(BaseRoom room, BasePlayer player) {
        delegate.addPlayer(room, player);
    }

    public void removePlayer(BaseRoom room, long userId) {
        delegate.removePlayer(room, userId);
    }

    public Collection<Room> listRoom() {
        return delegate.listRoom();
    }

    public Map<Long, Room> getRoomMap() {
        return delegate.getRoomMap();
    }

    public Map<Long, Long> getUserRoomMap() {
        return delegate.getUserRoomMap();
    }

    // ========== 抽象方法：子类实现具体房间创建 ==========

    /**
     * 创建房间（子类实现，并调用 addRoom 加入映射）
     * @param createContext 创建上下文
     * @return 具体房间实例
     */
    @Override
    public abstract BaseRoom createRoom(RoomCreateContext createContext);

    @Override
    public OperationFactory getOperationFactory() {
        return operationFactory;
    }

}
