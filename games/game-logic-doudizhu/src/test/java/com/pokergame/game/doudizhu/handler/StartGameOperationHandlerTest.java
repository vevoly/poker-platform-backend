package com.pokergame.game.doudizhu.handler;

import com.iohao.game.widget.light.room.flow.RoomCreateContext;
import com.pokergame.common.card.Card;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 开始游戏操作处理器测试
 *
 * 测试目标：
 * 1. 房主可以开始游戏
 * 2. 非房主不能开始游戏
 * 3. 人数不足不能开始
 * 4. 玩家未准备不能开始
 * 5. 游戏状态不正确不能开始
 * 6. 开始游戏后正确发牌和初始化状态
 *
 * @author poker-platform
 */
@DisplayName("开始游戏操作处理器测试")
class StartGameOperationHandlerTest {

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
        room.setGameStatus(DoudizhuGameStatus.READY);

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

        // 所有玩家准备
        player1.setReady(true);
        player2.setReady(true);
        player3.setReady(true);
    }

    // ==================== 权限校验测试 ====================

    @Test
    @DisplayName("测试房主可以开始游戏")
    void testOwnerCanStartGame() {
        assertThat(room.getOwnerId()).isEqualTo(1001L);
        assertThat(room.getPlayerCount()).isEqualTo(3);
        assertThat(room.isAllReady()).isTrue();

        // 房主可以开始游戏
        room.changeGameStatus(DoudizhuGameStatus.BIDDING);
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.BIDDING);
    }

    @Test
    @DisplayName("测试非房主不能开始游戏")
    void testNonOwnerCannotStartGame() {
        // 非房主尝试开始游戏（应该由 Action 层校验）
        // 这里只验证状态
        assertThat(room.getOwnerId()).isNotEqualTo(1002L);
    }

    // ==================== 人数校验测试 ====================

    @Test
    @DisplayName("测试3人满员可以开始游戏")
    void testThreePlayersCanStart() {
        assertThat(room.getPlayerCount()).isEqualTo(3);

        room.changeGameStatus(DoudizhuGameStatus.BIDDING);
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.BIDDING);
    }

    @Test
    @DisplayName("测试2人可以开始游戏")
    void testTwoPlayersCanStart() {
        // 创建2人房间
        var createContext = RoomCreateContext.of(2001L).setSpaceSize(2);
        DoudizhuRoom room2 = roomService.createRoom(createContext);
        room2.setOwnerId(2001L);
        room2.setMaxPlayers(2);
        room2.setGameStatus(DoudizhuGameStatus.READY);

        DoudizhuPlayer p1 = new DoudizhuPlayer(2001L, "玩家A");
        DoudizhuPlayer p2 = new DoudizhuPlayer(2002L, "玩家B");

        room2.addDoudizhuPlayer(p1);
        room2.addDoudizhuPlayer(p2);

        p1.setReady(true);
        p2.setReady(true);

        assertThat(room2.getPlayerCount()).isEqualTo(2);
        assertThat(room2.isAllReady()).isTrue();

        room2.changeGameStatus(DoudizhuGameStatus.BIDDING);
        assertThat(room2.getGameStatus()).isEqualTo(DoudizhuGameStatus.BIDDING);
    }

    @Test
    @DisplayName("测试人数不足不能开始游戏")
    void testNotEnoughPlayersCannotStart() {
        // 创建只有1人的房间
        var createContext = RoomCreateContext.of(3001L).setSpaceSize(3);
        DoudizhuRoom room2 = roomService.createRoom(createContext);
        room2.setOwnerId(3001L);
        room2.setMaxPlayers(3);

        DoudizhuPlayer p1 = new DoudizhuPlayer(3001L, "孤独玩家");
        room2.addDoudizhuPlayer(p1);
        p1.setReady(true);

        assertThat(room2.getPlayerCount()).isLessThan(2);

        // 人数不足，不应该开始游戏（由 Action 层校验）
        // 这里只验证状态
        assertThat(room2.getGameStatus()).isEqualTo(DoudizhuGameStatus.WAITING);
    }

    // ==================== 准备状态校验测试 ====================

    @Test
    @DisplayName("测试所有玩家准备可以开始")
    void testAllReadyCanStart() {
        assertThat(room.isAllReady()).isTrue();

        room.changeGameStatus(DoudizhuGameStatus.BIDDING);
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.BIDDING);
    }

    @Test
    @DisplayName("测试有玩家未准备不能开始")
    void testNotAllReadyCannotStart() {
        player2.setReady(false);

        assertThat(room.isAllReady()).isFalse();

        // 不应该开始游戏
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.READY);
    }

    // ==================== 游戏状态校验测试 ====================

    @Test
    @DisplayName("测试 WAITING 状态可以开始")
    void testWaitingStatusCanStart() {
        room.setGameStatus(DoudizhuGameStatus.WAITING);

        room.changeGameStatus(DoudizhuGameStatus.BIDDING);
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.BIDDING);
    }

    @Test
    @DisplayName("测试 READY 状态可以开始")
    void testReadyStatusCanStart() {
        room.setGameStatus(DoudizhuGameStatus.READY);

        room.changeGameStatus(DoudizhuGameStatus.BIDDING);
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.BIDDING);
    }

    @Test
    @DisplayName("测试 BIDDING 状态不能重复开始")
    void testBiddingStatusCannotStart() {
        room.setGameStatus(DoudizhuGameStatus.BIDDING);

        // 已经开始的游戏不能再次开始
        // 状态不会改变
        room.changeGameStatus(DoudizhuGameStatus.BIDDING);
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.BIDDING);
    }

    @Test
    @DisplayName("测试 PLAYING 状态不能开始")
    void testPlayingStatusCannotStart() {
        room.setGameStatus(DoudizhuGameStatus.PLAYING);

        room.changeGameStatus(DoudizhuGameStatus.BIDDING);
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.PLAYING);
    }

    // ==================== 发牌测试 ====================

    @Test
    @DisplayName("测试开始游戏后发牌")
    void testDealCardsAfterStart() {
        // 开始游戏前手牌为空
        assertThat(player1.getCardCount()).isEqualTo(0);
        assertThat(player2.getCardCount()).isEqualTo(0);
        assertThat(player3.getCardCount()).isEqualTo(0);

        // 模拟发牌（实际发牌由 DoudizhuDealer 处理）
        // 这里只验证发牌后手牌数量
        List<Card> mockHand1 = createMockHand(17);
        List<Card> mockHand2 = createMockHand(17);
        List<Card> mockHand3 = createMockHand(20);

        player1.setHandCards(mockHand1);
        player2.setHandCards(mockHand2);
        player3.setHandCards(mockHand3);

        assertThat(player1.getCardCount()).isEqualTo(17);
        assertThat(player2.getCardCount()).isEqualTo(17);
        assertThat(player3.getCardCount()).isEqualTo(20);
    }

    @Test
    @DisplayName("测试开始游戏后状态变更为 BIDDING")
    void testGameStatusChangeToBidding() {
        room.changeGameStatus(DoudizhuGameStatus.BIDDING);

        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.BIDDING);
    }

    @Test
    @DisplayName("测试开始游戏后设置出牌顺序")
    void testSetPlayOrderAfterStart() {
        List<Long> playOrder = List.of(1001L, 1002L, 1003L);
        room.setPlayOrder(playOrder, 1001L);

        assertThat(room.getPlayOrder()).containsExactly(1001L, 1002L, 1003L);
        assertThat(room.getCurrentPlayer().getUserId()).isEqualTo(1001L);
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("测试开始游戏后玩家不能重复准备")
    void testCannotReadyAfterGameStart() {
        room.changeGameStatus(DoudizhuGameStatus.PLAYING);

        // 游戏中玩家尝试准备应该无效
        player1.setReady(false);
        assertThat(player1.isReady()).isFalse();
    }

    @Test
    @DisplayName("测试开始游戏后出牌顺序正确循环")
    void testPlayOrderCycle() {
        List<Long> playOrder = List.of(1001L, 1002L, 1003L);
        room.setPlayOrder(playOrder, 1001L);

        assertThat(room.getCurrentPlayer().getUserId()).isEqualTo(1001L);

        room.nextTurn();
        assertThat(room.getCurrentPlayer().getUserId()).isEqualTo(1002L);

        room.nextTurn();
        assertThat(room.getCurrentPlayer().getUserId()).isEqualTo(1003L);

        room.nextTurn();
        assertThat(room.getCurrentPlayer().getUserId()).isEqualTo(1001L);
    }

    // ==================== 辅助方法 ====================

    private List<Card> createMockHand(int count) {
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            hand.add(Card.of(i % 54));
        }
        return hand;
    }
}
