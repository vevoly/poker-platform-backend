package com.pokergame.game.doudizhu.bidding;

import com.iohao.game.widget.light.room.flow.RoomCreateContext;
import com.pokergame.common.card.Card;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import com.pokergame.game.doudizhu.state.DoudizhuGameStateManager;
import com.pokergame.game.doudizhu.turn.DoudizhuTurnManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 叫地主功能测试（适配重构后的 BiddingManager）
 *
 * @author poker-platform
 */
@DisplayName("叫地主功能测试")
class BiddingManagerTest {

    private DoudizhuRoomService roomService;
    private DoudizhuRoom room;
    private DoudizhuGameStateManager gameState;
    private DoudizhuPlayer player1;
    private DoudizhuPlayer player2;
    private DoudizhuPlayer player3;
    private BiddingManager biddingManager;

    @BeforeEach
    void setUp() {
        roomService = DoudizhuRoomService.me();
        roomService.getRoomMap().clear();
        roomService.getUserRoomMap().clear();

        // 创建房间
        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
        room = roomService.createRoom(createContext);
        room.setOwnerId(1001L);
        room.setMaxPlayers(3);

        // 必须调用 initGameState 来创建 gameState 和管理器
        room.initGameState();
        gameState = room.getStateManager();

        // 创建玩家
        player1 = new DoudizhuPlayer(1001L, "玩家1");
        player2 = new DoudizhuPlayer(1002L, "玩家2");
        player3 = new DoudizhuPlayer(1003L, "玩家3");

        // 加入房间（会自动同步到 gameState）
        room.addPlayer(player1);
        room.addPlayer(player2);
        room.addPlayer(player3);

        // 手动设置玩家顺序（座位顺序）
        List<Long> playerOrder = List.of(1001L, 1002L, 1003L);
        gameState.setPlayOrder(playerOrder, 1001L);

        // 设置游戏状态为叫地主阶段
        room.updateGameStatus(DoudizhuGameStatus.BIDDING);
        gameState.changeStatus(DoudizhuGameStatus.BIDDING);

        // 设置底牌到 gameState
        List<Card> extraCards = new ArrayList<>();
        extraCards.add(Card.of(0));
        extraCards.add(Card.of(1));
        extraCards.add(Card.of(2));
        gameState.setLandlordExtraCards(extraCards);

        // 创建 BiddingManager（使用新的构造函数）
        biddingManager = new BiddingManager(room);
        room.setBiddingManager(biddingManager);

        // 创建 TurnManager 并关联
        DoudizhuTurnManager turnManager = new DoudizhuTurnManager(room);
        turnManager.setBiddingManager(biddingManager);
        room.setTurnManager(turnManager);
    }

    // ==================== 正常流程测试 ====================

    @Test
    @DisplayName("测试正常抢地主 - 玩家1抢地主，其他不抢")
    void testNormalGrab() {
        biddingManager.start();

        biddingManager.handleGrab(1001L, 3);
        biddingManager.handleNotGrab(1002L);
        biddingManager.handleNotGrab(1003L);

        assertThat(gameState.getLandlordId()).isEqualTo(1001L);
        assertThat(gameState.getMultiplier()).isEqualTo(3);
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.PLAYING.name());
        assertThat(gameState.getCurrentStatus()).isEqualTo(DoudizhuGameStatus.PLAYING);

        DoudizhuPlayer landlord = room.getDoudizhuPlayer(1001L);
        assertThat(landlord.getCardCount()).isEqualTo(3); // 底牌3张
    }

    @Test
    @DisplayName("测试多人抢地主 - 倍数高的成为地主")
    void testMultipleGrabHigherMultipleWins() {
        biddingManager.start();

        biddingManager.handleGrab(1001L, 3);
        biddingManager.handleGrab(1002L, 2);
        biddingManager.handleNotGrab(1003L);

        assertThat(gameState.getLandlordId()).isEqualTo(1001L);
        assertThat(gameState.getMultiplier()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试多人抢地主 - 后抢的倍数更高")
    void testLaterGrabHigherMultipleWins() {
        biddingManager.start();

        biddingManager.handleGrab(1001L, 2);
        biddingManager.handleGrab(1002L, 3);
        biddingManager.handleNotGrab(1003L);

        assertThat(gameState.getLandlordId()).isEqualTo(1002L);
        assertThat(gameState.getMultiplier()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试无人抢地主 - 随机分配地主")
    void testNoOneGrabs() {
        biddingManager.start();

        biddingManager.handleNotGrab(1001L);
        biddingManager.handleNotGrab(1002L);
        biddingManager.handleNotGrab(1003L);

        assertThat(gameState.getLandlordId()).isNotEqualTo(0);
        assertThat(gameState.getMultiplier()).isEqualTo(1);
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.PLAYING.name());

        DoudizhuPlayer landlord = room.getDoudizhuPlayer(gameState.getLandlordId());
        assertThat(landlord.getCardCount()).isEqualTo(3);
    }

    // ==================== 超时测试 ====================

    @Test
    @DisplayName("测试玩家超时 - 自动不抢")
    void testTimeout() {
        biddingManager.start();

        // 模拟当前玩家（1001）超时
        biddingManager.handleTimeout();

        // 现在当前玩家应为1002
        biddingManager.handleGrab(1002L, 2);
        biddingManager.handleNotGrab(1003L);

        assertThat(gameState.getLandlordId()).isEqualTo(1002L);
        assertThat(gameState.getMultiplier()).isEqualTo(2);
    }

    // ==================== 状态校验测试 ====================

    @Test
    @DisplayName("测试非叫地主阶段不能抢地主")
    void testCannotGrabWhenNotBidding() {
        room.updateGameStatus(DoudizhuGameStatus.PLAYING);
        gameState.changeStatus(DoudizhuGameStatus.PLAYING);

        assertThatThrownBy(() -> biddingManager.handleGrab(1001L, 3))
                .hasMessageContaining("状态错误");
    }

    @Test
    @DisplayName("测试非当前玩家不能抢地主")
    void testNonCurrentPlayerCannotGrab() {
        biddingManager.start();
        // 当前玩家是1001，1002不能抢
        assertThatThrownBy(() -> biddingManager.handleGrab(1002L, 3))
                .hasMessageContaining("不是当前玩家");
    }

    // ==================== 轮次测试 ====================

    @Test
    @DisplayName("测试两轮叫地主")
    void testTwoRounds() {
        biddingManager.start();

        // 第一轮：所有玩家都不抢
        biddingManager.handleNotGrab(1001L);
        biddingManager.handleNotGrab(1002L);
        biddingManager.handleNotGrab(1003L);

        // 第二轮开始，应该还是从玩家1开始
        biddingManager.handleGrab(1001L, 2);
        biddingManager.handleNotGrab(1002L);
        biddingManager.handleNotGrab(1003L);

        assertThat(gameState.getLandlordId()).isEqualTo(1001L);
        assertThat(gameState.getMultiplier()).isEqualTo(2);
    }

    // ==================== 记录查询测试 ====================

    @Test
    @DisplayName("测试获取叫地主记录")
    void testGetBidRecords() {
        biddingManager.start();

        biddingManager.handleGrab(1001L, 3);
        biddingManager.handleNotGrab(1002L);

        var records = biddingManager.getBidRecords();

        assertThat(records).containsKey(1001L);
        assertThat(records.get(1001L).isGrab()).isTrue();
        assertThat(records.get(1001L).getMultiple()).isEqualTo(3);

        assertThat(records).containsKey(1002L);
        assertThat(records.get(1002L).isGrab()).isFalse();
    }
}
