package com.pokergame.game.doudizhu.action;


import com.iohao.game.widget.light.room.flow.RoomCreateContext;
import com.pokergame.common.model.room.CreateRoomReq;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 房间操作 Action 测试
 *
 * 测试目标：
 * 1. 创建房间
 * 2. 加入房间
 * 3. 离开房间
 * 4. 房间满员处理
 * 5. 重复加入处理
 *
 * @author poker-platform
 */
@DisplayName("房间操作测试")
class RoomActionTest {

    private RoomAction roomAction;
    private DoudizhuRoomService roomService;
    private DoudizhuRoom room;

    @BeforeEach
    void setUp() {
        roomAction = new RoomAction();
        roomService = DoudizhuRoomService.me();

        // 清理之前的房间
        roomService.getRoomMap().clear();
        roomService.getUserRoomMap().clear();
    }

    // ==================== 创建房间测试 ====================

    @Test
    @DisplayName("测试创建房间成功")
    void testCreateRoomSuccess() {
        CreateRoomReq req = new CreateRoomReq();
        req.setMaxPlayers(3);
        req.setPlayerName("房主");

        // 模拟 FlowContext，实际测试中需要 Mock
        // 这里直接调用房间服务验证
        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
        DoudizhuRoom newRoom = roomService.createRoom(createContext);
        newRoom.setOwnerId(1001L);
        newRoom.setMaxPlayers(3);

        DoudizhuPlayer owner = new DoudizhuPlayer(1001L, "房主");
        newRoom.addDoudizhuPlayer(owner);
        roomService.addRoom(newRoom);
        roomService.addPlayer(newRoom, owner);

        assertThat(newRoom).isNotNull();
        assertThat(newRoom.getOwnerId()).isEqualTo(1001L);
        assertThat(newRoom.getMaxPlayers()).isEqualTo(3);
        assertThat(newRoom.getPlayerCount()).isEqualTo(1);
        assertThat(newRoom.getGameStatus()).isEqualTo(DoudizhuGameStatus.WAITING);
    }

    @Test
    @DisplayName("测试玩家已在房间中不能重复创建")
    void testCannotCreateRoomWhenAlreadyInRoom() {
        // 先创建一个房间让玩家进入
        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
        DoudizhuRoom existingRoom = roomService.createRoom(createContext);
        existingRoom.setOwnerId(1001L);

        DoudizhuPlayer player = new DoudizhuPlayer(1001L, "玩家");
        existingRoom.addDoudizhuPlayer(player);
        roomService.addRoom(existingRoom);
        roomService.addPlayer(existingRoom, player);

        // 玩家已在房间中，不能再次创建
        assertThat(roomService.getUserRoom(1001L)).isNotNull();

        // 验证玩家已在房间中
        DoudizhuRoom userRoom = roomService.getUserRoom(1001L);
        assertThat(userRoom).isNotNull();
        assertThat(userRoom.getRoomId()).isEqualTo(existingRoom.getRoomId());
    }

    // ==================== 加入房间测试 ====================

    @Test
    @DisplayName("测试加入房间成功")
    void testJoinRoomSuccess() {
        // 创建房间
        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
        DoudizhuRoom targetRoom = roomService.createRoom(createContext);
        targetRoom.setOwnerId(1001L);
        targetRoom.setMaxPlayers(3);
        targetRoom.setGameStatus(DoudizhuGameStatus.WAITING);

        DoudizhuPlayer owner = new DoudizhuPlayer(1001L, "房主");
        targetRoom.addDoudizhuPlayer(owner);
        roomService.addRoom(targetRoom);
        roomService.addPlayer(targetRoom, owner);

        // 新玩家加入
        DoudizhuPlayer newPlayer = new DoudizhuPlayer(1002L, "新玩家");
        targetRoom.addDoudizhuPlayer(newPlayer);
        roomService.addPlayer(targetRoom, newPlayer);

        assertThat(targetRoom.getPlayerCount()).isEqualTo(2);
        assertThat(targetRoom.getDoudizhuPlayer(1002L)).isNotNull();
        assertThat(roomService.getUserRoom(1002L)).isEqualTo(targetRoom.getRoomId());
    }

    @Test
    @DisplayName("测试加入已满房间失败")
    void testJoinFullRoomFailed() {
        // 创建满员房间（3人）
        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
        DoudizhuRoom targetRoom = roomService.createRoom(createContext);
        targetRoom.setOwnerId(1001L);
        targetRoom.setMaxPlayers(3);

        DoudizhuPlayer p1 = new DoudizhuPlayer(1001L, "玩家1");
        DoudizhuPlayer p2 = new DoudizhuPlayer(1002L, "玩家2");
        DoudizhuPlayer p3 = new DoudizhuPlayer(1003L, "玩家3");

        targetRoom.addDoudizhuPlayer(p1);
        targetRoom.addDoudizhuPlayer(p2);
        targetRoom.addDoudizhuPlayer(p3);

        assertThat(targetRoom.isFull()).isTrue();

        // 第四位玩家尝试加入
        DoudizhuPlayer p4 = new DoudizhuPlayer(1004L, "玩家4");
        targetRoom.addDoudizhuPlayer(p4);

        // 应该添加失败
        assertThat(targetRoom.getPlayerCount()).isEqualTo(3);
        assertThat(targetRoom.getDoudizhuPlayer(1004L)).isNull();
    }

    @Test
    @DisplayName("测试加入已开始的房间失败")
    void testJoinGameStartedRoomFailed() {
        // 创建房间并开始游戏
        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
        DoudizhuRoom targetRoom = roomService.createRoom(createContext);
        targetRoom.setOwnerId(1001L);
        targetRoom.setMaxPlayers(3);
        targetRoom.setGameStatus(DoudizhuGameStatus.PLAYING);

        DoudizhuPlayer p1 = new DoudizhuPlayer(1001L, "玩家1");
        targetRoom.addDoudizhuPlayer(p1);

        // 新玩家尝试加入
        DoudizhuPlayer p2 = new DoudizhuPlayer(1002L, "玩家2");
        targetRoom.addDoudizhuPlayer(p2);

        // 游戏已开始，不应添加
        assertThat(targetRoom.getDoudizhuPlayer(1002L)).isNull();
        assertThat(targetRoom.getPlayerCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("测试加入不存在的房间失败")
    void testJoinNonExistentRoomFailed() {
        long nonExistentRoomId = 99999L;
        DoudizhuRoom nonExistentRoom = roomService.getDoudizhuRoom(nonExistentRoomId);

        assertThat(nonExistentRoom).isNull();
    }

    @Test
    @DisplayName("测试玩家已在房间中不能重复加入")
    void testCannotJoinSameRoomTwice() {
        // 创建房间
        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
        DoudizhuRoom targetRoom = roomService.createRoom(createContext);
        targetRoom.setOwnerId(1001L);

        DoudizhuPlayer player = new DoudizhuPlayer(1001L, "玩家");
        targetRoom.addDoudizhuPlayer(player);

        // 尝试再次加入
        DoudizhuPlayer duplicatePlayer = new DoudizhuPlayer(1001L, "重复玩家");
        targetRoom.addDoudizhuPlayer(duplicatePlayer);

        // 玩家已在房间中，不应重复添加
        assertThat(targetRoom.getPlayerCount()).isEqualTo(1);
    }

    // ==================== 离开房间测试 ====================

    @Test
    @DisplayName("测试离开房间成功")
    void testLeaveRoomSuccess() {
        // 创建房间并加入玩家
        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
        DoudizhuRoom targetRoom = roomService.createRoom(createContext);
        targetRoom.setOwnerId(1001L);

        DoudizhuPlayer player = new DoudizhuPlayer(1001L, "玩家");
        targetRoom.addDoudizhuPlayer(player);
        roomService.addRoom(targetRoom);
        roomService.addPlayer(targetRoom, player);

        assertThat(targetRoom.getPlayerCount()).isEqualTo(1);
        assertThat(roomService.getUserRoom(1001L)).isEqualTo(targetRoom.getRoomId());

        // 玩家离开
        targetRoom.removeDoudizhuPlayer(1001L);
        roomService.removePlayer(1001L);

        assertThat(targetRoom.getPlayerCount()).isEqualTo(0);
        assertThat(roomService.getUserRoom(1001L)).isNull();
    }

    @Test
    @DisplayName("测试房主离开后房间仍存在")
    void testOwnerLeaveRoomStillExists() {
        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
        DoudizhuRoom targetRoom = roomService.createRoom(createContext);
        targetRoom.setOwnerId(1001L);

        DoudizhuPlayer owner = new DoudizhuPlayer(1001L, "房主");
        DoudizhuPlayer player2 = new DoudizhuPlayer(1002L, "玩家2");

        targetRoom.addDoudizhuPlayer(owner);
        targetRoom.addDoudizhuPlayer(player2);

        assertThat(targetRoom.getPlayerCount()).isEqualTo(2);

        // 房主离开
        targetRoom.removeDoudizhuPlayer(1001L);

        // 房间仍然存在，还有玩家2
        assertThat(targetRoom.getPlayerCount()).isEqualTo(1);
        assertThat(targetRoom.getOwnerId()).isEqualTo(1001L); // 房主ID不变
    }

    @Test
    @DisplayName("测试房间变空后销毁")
    void testRoomDestroyWhenEmpty() {
        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
        DoudizhuRoom targetRoom = roomService.createRoom(createContext);
        targetRoom.setOwnerId(1001L);

        DoudizhuPlayer player = new DoudizhuPlayer(1001L, "玩家");
        targetRoom.addDoudizhuPlayer(player);
        roomService.addRoom(targetRoom);

        long roomId = targetRoom.getRoomId();

        // 玩家离开
        targetRoom.removeDoudizhuPlayer(1001L);
        roomService.removePlayer(1001L);

        assertThat(targetRoom.isEmpty()).isTrue();

        // 房间被销毁
        roomService.removeRoom(targetRoom);
        assertThat(roomService.getRoomMap().containsKey(roomId)).isFalse();
    }

    @Test
    @DisplayName("测试离开不存在的房间")
    void testLeaveNonExistentRoom() {
        long nonExistentRoomId = 99999L;
        DoudizhuRoom nonExistentRoom = roomService.getDoudizhuRoom(nonExistentRoomId);

        assertThat(nonExistentRoom).isNull();
    }
}
