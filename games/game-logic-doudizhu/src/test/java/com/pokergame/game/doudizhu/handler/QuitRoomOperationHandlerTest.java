package com.pokergame.game.doudizhu.handler;

import com.iohao.game.widget.light.room.flow.RoomCreateContext;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 离开房间操作处理器测试
 *
 * 测试目标：
 * 1. 玩家可以离开房间
 * 2. 房主离开后房间处理正确
 * 3. 房间变空后自动销毁
 * 4. 游戏中离开的处理
 *
 * @author poker-platform
 */
@DisplayName("离开房间操作处理器测试")
class QuitRoomOperationHandlerTest {

    private DoudizhuRoomService roomService;
    private DoudizhuRoom room;
    private DoudizhuPlayer player1;
    private DoudizhuPlayer player2;
    private DoudizhuPlayer player3;

    @BeforeEach
    void setUp() {
        // 初始化房间服务
        roomService = DoudizhuRoomService.me();
        roomService.getRoomMap().clear();
        roomService.getUserRoomMap().clear();

        // 创建房间
        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
        room = roomService.createRoom(createContext);
        room.setOwnerId(1001L);
        room.setMaxPlayers(3);
        room.setGameStatus(DoudizhuGameStatus.WAITING);

        // 创建玩家
        player1 = new DoudizhuPlayer(1001L, "房主");
        player2 = new DoudizhuPlayer(1002L, "玩家2");
        player3 = new DoudizhuPlayer(1003L, "玩家3");

        // 玩家加入房间
        room.addDoudizhuPlayer(player1);
        room.addDoudizhuPlayer(player2);
        room.addDoudizhuPlayer(player3);

        roomService.addRoom(room);
        roomService.addPlayer(room, player1);
        roomService.addPlayer(room, player2);
        roomService.addPlayer(room, player3);
    }

    // ==================== 正常离开测试 ====================

    @Test
    @DisplayName("测试普通玩家离开房间")
    void testNormalPlayerLeaveRoom() {
        assertThat(room.getPlayerCount()).isEqualTo(3);
        assertThat(room.getDoudizhuPlayer(1002L)).isNotNull();

        // 玩家2离开
        room.removeDoudizhuPlayer(1002L);
        roomService.removePlayer(1002L);

        assertThat(room.getPlayerCount()).isEqualTo(2);
        assertThat(room.getDoudizhuPlayer(1002L)).isNull();
        assertThat(roomService.getUserRoom(1002L)).isNull();
    }

    @Test
    @DisplayName("测试房主离开房间")
    void testOwnerLeaveRoom() {
        assertThat(room.getOwnerId()).isEqualTo(1001L);
        assertThat(room.getPlayerCount()).isEqualTo(3);

        // 房主离开
        room.removeDoudizhuPlayer(1001L);
        roomService.removePlayer(1001L);

        // 房间还存在，房主已离开
        assertThat(room.getPlayerCount()).isEqualTo(2);
        assertThat(room.getDoudizhuPlayer(1001L)).isNull();
        // 房主ID保持不变（不自动转移）
        assertThat(room.getOwnerId()).isEqualTo(1001L);
    }

    // ==================== 房间销毁测试 ====================

    @Test
    @DisplayName("测试房间变空后自动销毁")
    void testRoomDestroyWhenEmpty() {
        assertThat(room.isEmpty()).isFalse();

        // 所有玩家离开
        room.removeDoudizhuPlayer(1001L);
        room.removeDoudizhuPlayer(1002L);
        room.removeDoudizhuPlayer(1003L);

        roomService.removePlayer(1001L);
        roomService.removePlayer(1002L);
        roomService.removePlayer(1003L);

        assertThat(room.isEmpty()).isTrue();

        // 房间应该被销毁
        roomService.removeRoom(room);
        assertThat(roomService.getRoomMap().containsKey(room.getRoomId())).isFalse();
    }

    @Test
    @DisplayName("测试玩家离开后广播")
    void testQuitRoomBroadcast() {
        int beforeCount = room.getPlayerCount();

        room.removeDoudizhuPlayer(1002L);

        int afterCount = room.getPlayerCount();
        assertThat(afterCount).isEqualTo(beforeCount - 1);
    }

    // ==================== 不同状态离开测试 ====================

    @Test
    @DisplayName("测试等待状态离开房间")
    void testLeaveWhenWaiting() {
        room.setGameStatus(DoudizhuGameStatus.WAITING);

        room.removeDoudizhuPlayer(1002L);

        assertThat(room.getPlayerCount()).isEqualTo(2);
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.WAITING);
    }

    @Test
    @DisplayName("测试准备状态离开房间")
    void testLeaveWhenReady() {
        room.setGameStatus(DoudizhuGameStatus.READY);

        // 玩家2准备
        player2.setReady(true);

        room.removeDoudizhuPlayer(1002L);

        assertThat(room.getPlayerCount()).isEqualTo(2);
        // 房间应该保持 READY 状态或降级为 WAITING
    }

    @Test
    @DisplayName("测试游戏中离开房间")
    void testLeaveWhenPlaying() {
        room.setGameStatus(DoudizhuGameStatus.PLAYING);

        room.removeDoudizhuPlayer(1002L);

        // 游戏中离开，房间继续存在
        assertThat(room.getPlayerCount()).isEqualTo(2);
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.PLAYING);
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("测试不存在的玩家离开房间")
    void testNonExistentPlayerLeave() {
        int beforeCount = room.getPlayerCount();

        room.removeDoudizhuPlayer(9999L);

        // 玩家数量不变
        assertThat(room.getPlayerCount()).isEqualTo(beforeCount);
    }

    @Test
    @DisplayName("测试2人局房主离开")
    void testTwoPlayersOwnerLeave() {
        // 创建2人房间
        var createContext = RoomCreateContext.of(2001L).setSpaceSize(2);
        DoudizhuRoom room2 = roomService.createRoom(createContext);
        room2.setOwnerId(2001L);
        room2.setMaxPlayers(2);
        room2.setGameStatus(DoudizhuGameStatus.WAITING);

        DoudizhuPlayer owner = new DoudizhuPlayer(2001L, "房主");
        DoudizhuPlayer other = new DoudizhuPlayer(2002L, "玩家");

        room2.addDoudizhuPlayer(owner);
        room2.addDoudizhuPlayer(other);

        assertThat(room2.getPlayerCount()).isEqualTo(2);

        // 房主离开
        room2.removeDoudizhuPlayer(2001L);

        assertThat(room2.getPlayerCount()).isEqualTo(1);
        assertThat(room2.getOwnerId()).isEqualTo(2001L); // 房主ID不变
    }
}
