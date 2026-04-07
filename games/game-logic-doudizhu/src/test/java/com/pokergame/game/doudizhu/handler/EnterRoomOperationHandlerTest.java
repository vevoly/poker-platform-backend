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
 * 进入房间操作处理器测试
 *
 * 测试目标：
 * 1. 玩家可以进入房间
 * 2. 房间已满时不能进入
 * 3. 游戏已开始时不能进入
 * 4. 玩家已在房间中不能重复进入
 *
 * @author poker-platform
 */
@DisplayName("进入房间操作处理器测试")
class EnterRoomOperationHandlerTest {

    private DoudizhuRoomService roomService;
    private DoudizhuRoom room;
    private DoudizhuPlayer newPlayer;

    @BeforeEach
    void setUp() {
        // 初始化房间服务
        roomService = DoudizhuRoomService.me();
        roomService.getRoomMap().clear();
        roomService.getUserRoomMap().clear();

        // 创建房间（最大3人）
        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
        room = roomService.createRoom(createContext);
        room.setOwnerId(1001L);
        room.setMaxPlayers(3);
        room.setGameStatus(DoudizhuGameStatus.WAITING);

        // 创建房主玩家并加入
        DoudizhuPlayer owner = new DoudizhuPlayer(1001L, "房主");
        room.addDoudizhuPlayer(owner);
        roomService.addPlayer(room, owner);

        // 创建第二个玩家并加入
        DoudizhuPlayer player2 = new DoudizhuPlayer(1002L, "玩家2");
        room.addDoudizhuPlayer(player2);
        roomService.addPlayer(room, player2);

        // 新玩家
        newPlayer = new DoudizhuPlayer(1003L, "新玩家");

        roomService.addRoom(room);
    }

    // ==================== 正常进入测试 ====================

    @Test
    @DisplayName("测试玩家进入房间")
    void testPlayerEnterRoom() {
        assertThat(room.getPlayerCount()).isEqualTo(2);

        room.addDoudizhuPlayer(newPlayer);
        roomService.addPlayer(room, newPlayer);

        assertThat(room.getPlayerCount()).isEqualTo(3);
        assertThat(room.getDoudizhuPlayer(1003L)).isNotNull();
        assertThat(roomService.getUserRoom(1003L).getRoomId()).isEqualTo(room.getRoomId());
    }

    @Test
    @DisplayName("测试进入房间后广播")
    void testEnterRoomBroadcast() {
        // 验证玩家进入后房间状态正确
        room.addDoudizhuPlayer(newPlayer);

        // 验证房间包含新玩家
        assertThat(room.getPlayerMap().containsKey(1003L)).isTrue();
        assertThat(room.getRealPlayerMap().containsKey(1003L)).isTrue();
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("测试房间已满时不能进入")
    void testCannotEnterFullRoom() {
        // 房间现有2人，再添加1人满员
        room.addDoudizhuPlayer(newPlayer);
        assertThat(room.isFull()).isTrue();

        // 创建第四个玩家
        DoudizhuPlayer player4 = new DoudizhuPlayer(1004L, "玩家4");

        // 房间已满，应该无法添加
        room.addDoudizhuPlayer(player4);

        // 验证玩家4没有加入成功
        assertThat(room.getPlayerCount()).isEqualTo(3);
        assertThat(room.getDoudizhuPlayer(1004L)).isNull();
    }

    @Test
    @DisplayName("测试游戏开始时不能进入房间")
    void testCannotEnterWhenGameStarted() {
        room.setGameStatus(DoudizhuGameStatus.PLAYING);

        DoudizhuPlayer player4 = new DoudizhuPlayer(1004L, "玩家4");
        room.addDoudizhuPlayer(player4);

        // 游戏进行中，不应添加新玩家
        assertThat(room.getDoudizhuPlayer(1004L)).isNull();
    }

    @Test
    @DisplayName("测试叫地主时不能进入房间")
    void testCannotEnterWhenBidding() {
        room.setGameStatus(DoudizhuGameStatus.BIDDING);

        DoudizhuPlayer player4 = new DoudizhuPlayer(1004L, "玩家4");
        room.addDoudizhuPlayer(player4);

        assertThat(room.getDoudizhuPlayer(1004L)).isNull();
    }

    @Test
    @DisplayName("测试游戏结束时不能进入房间")
    void testCannotEnterWhenFinished() {
        room.setGameStatus(DoudizhuGameStatus.FINISHED);

        DoudizhuPlayer player4 = new DoudizhuPlayer(1004L, "玩家4");
        room.addDoudizhuPlayer(player4);

        assertThat(room.getDoudizhuPlayer(1004L)).isNull();
    }

    @Test
    @DisplayName("测试玩家已在房间中不能重复进入")
    void testCannotReEnterSameRoom() {
        // 玩家已在房间中
        assertThat(room.getDoudizhuPlayer(1001L)).isNotNull();

        // 尝试再次添加
        DoudizhuPlayer duplicatePlayer = new DoudizhuPlayer(1001L, "重复玩家");
        room.addDoudizhuPlayer(duplicatePlayer);

        // 应该只有一个玩家1001
        assertThat(room.getPlayerCount()).isEqualTo(2); // 原有2人
    }

    @Test
    @DisplayName("测试房间不存在时不能进入")
    void testCannotEnterNonExistentRoom() {
        long nonExistentRoomId = 99999L;
        DoudizhuRoom nonExistentRoom = roomService.getDoudizhuRoom(nonExistentRoomId);

        assertThat(nonExistentRoom).isNull();
    }
}
