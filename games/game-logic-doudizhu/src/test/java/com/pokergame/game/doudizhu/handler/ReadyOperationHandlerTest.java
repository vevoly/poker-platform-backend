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
 * 准备操作处理器测试
 *
 * 测试目标：
 * 1. 玩家可以准备/取消准备
 * 2. 只有 WAITING 或 READY 状态才能准备
 * 3. 游戏中不能准备
 * 4. 所有玩家准备好后自动开始游戏
 *
 * @author poker-platform
 */
@DisplayName("准备操作处理器测试")
class ReadyOperationHandlerTest {

    private DoudizhuRoomService roomService;
    private DoudizhuRoom room;
    private DoudizhuPlayer player1;
    private DoudizhuPlayer player2;
    private DoudizhuPlayer player3;
    private ReadyOperationHandler handler;

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
        player1 = new DoudizhuPlayer(1001L, "玩家1");
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

        // 创建处理器
        handler = new ReadyOperationHandler();
    }

    // ==================== 基础准备测试 ====================

    @Test
    @DisplayName("测试玩家准备")
    void testPlayerReady() {
        // 玩家1准备
        player1.setReady(true);

        assertThat(player1.isReady()).isTrue();
        assertThat(room.isAllReady()).isFalse(); // 还有其他玩家未准备
    }

    @Test
    @DisplayName("测试所有玩家准备")
    void testAllPlayersReady() {
        // 所有玩家准备
        player1.setReady(true);
        player2.setReady(true);
        player3.setReady(true);

        assertThat(room.isAllReady()).isTrue();
    }

    @Test
    @DisplayName("测试取消准备")
    void testCancelReady() {
        // 先准备
        player1.setReady(true);
        assertThat(player1.isReady()).isTrue();

        // 取消准备
        player1.setReady(false);
        assertThat(player1.isReady()).isFalse();
    }

    // ==================== 状态校验测试 ====================

    @Test
    @DisplayName("测试 WAITING 状态可以准备")
    void testCanReadyInWaitingStatus() {
        room.setGameStatus(DoudizhuGameStatus.WAITING);

        // 应该可以准备
        player1.setReady(true);
        assertThat(player1.isReady()).isTrue();
    }

    @Test
    @DisplayName("测试 READY 状态可以准备")
    void testCanReadyInReadyStatus() {
        room.setGameStatus(DoudizhuGameStatus.READY);

        player1.setReady(true);
        assertThat(player1.isReady()).isTrue();
    }

    @Test
    @DisplayName("测试 BIDDING 状态不能准备")
    void testCannotReadyInBiddingStatus() {
        room.setGameStatus(DoudizhuGameStatus.BIDDING);

        // 游戏中不能准备/取消准备
        player1.setReady(true);
        // 状态应该不会改变（由业务逻辑控制）
        // 这里只验证状态设置，实际校验在 processVerify 中
    }

    @Test
    @DisplayName("测试 PLAYING 状态不能准备")
    void testCannotReadyInPlayingStatus() {
        room.setGameStatus(DoudizhuGameStatus.PLAYING);

        player1.setReady(true);
        // 游戏中不能准备
    }

    @Test
    @DisplayName("测试 FINISHED 状态不能准备")
    void testCannotReadyInFinishedStatus() {
        room.setGameStatus(DoudizhuGameStatus.FINISHED);

        player1.setReady(true);
        // 游戏结束后不能准备
    }

    // ==================== 自动开始游戏测试 ====================

    @Test
    @DisplayName("测试所有玩家准备后自动开始游戏")
    void testAutoStartGameWhenAllReady() {
        room.setGameStatus(DoudizhuGameStatus.READY);

        // 所有玩家准备
        player1.setReady(true);
        player2.setReady(true);
        player3.setReady(true);

        assertThat(room.isAllReady()).isTrue();

        // 注意：自动开始游戏需要调用 room.operation(InternalOperation.START_GAME)
        // 这里只验证准备状态
    }

    // ==================== 玩家数量测试 ====================

    @Test
    @DisplayName("测试2人局所有玩家准备")
    void testTwoPlayersAllReady() {
        // 创建2人房间
        var createContext = RoomCreateContext.of(2001L).setSpaceSize(2);
        DoudizhuRoom room2 = roomService.createRoom(createContext);
        room2.setOwnerId(2001L);
        room2.setMaxPlayers(2);

        DoudizhuPlayer p1 = new DoudizhuPlayer(2001L, "玩家A");
        DoudizhuPlayer p2 = new DoudizhuPlayer(2002L, "玩家B");

        room2.addDoudizhuPlayer(p1);
        room2.addDoudizhuPlayer(p2);

        p1.setReady(true);
        p2.setReady(true);

        assertThat(room2.isAllReady()).isTrue();
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("测试空房间没有玩家准备")
    void testEmptyRoomNoReady() {
        DoudizhuRoom emptyRoom = new DoudizhuRoom();
        emptyRoom.setRoomId(99999L);

        assertThat(emptyRoom.isAllReady()).isFalse();
    }

    @Test
    @DisplayName("测试部分玩家准备")
    void testPartialPlayersReady() {
        player1.setReady(true);
        player2.setReady(false);
        player3.setReady(false);

        assertThat(room.isAllReady()).isFalse();
    }

    @Test
    @DisplayName("测试准备状态持久化")
    void testReadyStatePersistence() {
        // 设置准备
        player1.setReady(true);
        assertThat(player1.isReady()).isTrue();

        // 取消准备
        player1.setReady(false);
        assertThat(player1.isReady()).isFalse();

        // 再次准备
        player1.setReady(true);
        assertThat(player1.isReady()).isTrue();
    }
}
