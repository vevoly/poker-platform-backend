package com.pokergame.game.doudizhu.bidding;

import com.iohao.game.widget.light.room.flow.RoomCreateContext;
import com.pokergame.common.card.Card;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import com.pokergame.game.doudizhu.turn.DoudizhuTurnManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 叫地主功能测试
 *
 * @author poker-platform
 */
@DisplayName("叫地主功能测试")
class BiddingManagerTest {

    private DoudizhuRoomService roomService;
    private DoudizhuRoom room;
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

        // ========== 初始化回合管理器（重要！） ==========
        DoudizhuTurnManager turnManager = new DoudizhuTurnManager(room);
        room.setTurnManager(turnManager);

        // 创建玩家
        player1 = new DoudizhuPlayer(1001L, "玩家1");
        player2 = new DoudizhuPlayer(1002L, "玩家2");
        player3 = new DoudizhuPlayer(1003L, "玩家3");

        // 加入房间
        room.addDoudizhuPlayer(player1);
        room.addDoudizhuPlayer(player2);
        room.addDoudizhuPlayer(player3);

        roomService.addRoom(room);

        // 设置游戏状态为叫地主阶段
        room.setGameStatus(DoudizhuGameStatus.BIDDING);

        // 设置底牌
        List<Card> extraCards = new ArrayList<>();
        extraCards.add(Card.of(0));
        extraCards.add(Card.of(1));
        extraCards.add(Card.of(2));
        room.setLandlordExtraCards(extraCards);

        // 初始化叫地主管理器
        List<Long> playerOrder = List.of(1001L, 1002L, 1003L);
        biddingManager = new BiddingManager(room, playerOrder);
        room.setBiddingManager(biddingManager);
    }

    // ==================== 正常流程测试 ====================

    @Test
    @DisplayName("测试正常抢地主 - 玩家1抢地主，其他不抢")
    void testNormalGrab() {
        // 玩家1抢地主
        biddingManager.handleGrab(1001L, 3);

        // 玩家2不抢
        biddingManager.handleNotGrab(1002L);

        // 玩家3不抢
        biddingManager.handleNotGrab(1003L);

        // 验证地主是玩家1
        assertThat(room.getLandlordId()).isEqualTo(1001L);
        assertThat(room.getCurrentMultiple()).isEqualTo(3);
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.PLAYING);

        // 验证地主拿到底牌
        DoudizhuPlayer landlord = room.getDoudizhuPlayer(1001L);
        assertThat(landlord.getCardCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试多人抢地主 - 倍数高的成为地主")
    void testMultipleGrabHigherMultipleWins() {
        // 玩家1抢地主（3倍）
        biddingManager.handleGrab(1001L, 3);

        // 玩家2抢地主（2倍）
        biddingManager.handleGrab(1002L, 2);

        // 玩家3不抢
        biddingManager.handleNotGrab(1003L);

        // 验证地主是玩家1（倍数更高）
        assertThat(room.getLandlordId()).isEqualTo(1001L);
        assertThat(room.getCurrentMultiple()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试多人抢地主 - 后抢的倍数更高")
    void testLaterGrabHigherMultipleWins() {
        // 玩家1抢地主（2倍）
        biddingManager.handleGrab(1001L, 2);

        // 玩家2抢地主（3倍）
        biddingManager.handleGrab(1002L, 3);

        // 玩家3不抢
        biddingManager.handleNotGrab(1003L);

        // 验证地主是玩家2（倍数更高）
        assertThat(room.getLandlordId()).isEqualTo(1002L);
        assertThat(room.getCurrentMultiple()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试无人抢地主 - 随机分配地主")
    void testNoOneGrabs() {
        // 所有玩家都不抢
        biddingManager.handleNotGrab(1001L);
        biddingManager.handleNotGrab(1002L);
        biddingManager.handleNotGrab(1003L);

        // 验证有地主（随机分配）
        assertThat(room.getLandlordId()).isNotEqualTo(0);
        assertThat(room.getCurrentMultiple()).isEqualTo(1);
        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.PLAYING);

        // 验证地主拿到底牌
        DoudizhuPlayer landlord = room.getDoudizhuPlayer(room.getLandlordId());
        assertThat(landlord.getCardCount()).isEqualTo(3);
    }

    // ==================== 超时测试 ====================

    @Test
    @DisplayName("测试玩家超时 - 自动不抢")
    void testTimeout() {
        // 玩家1超时（自动不抢）
        biddingManager.handleTimeout();

        // 验证玩家1被记录为不抢
        // 轮到玩家2

        // 玩家2抢地主
        biddingManager.handleGrab(1002L, 2);

        // 玩家3不抢
        biddingManager.handleNotGrab(1003L);

        // 验证地主是玩家2
        assertThat(room.getLandlordId()).isEqualTo(1002L);
    }

    // ==================== 状态校验测试 ====================

    @Test
    @DisplayName("测试非叫地主阶段不能抢地主")
    void testCannotGrabWhenNotBidding() {
        room.setGameStatus(DoudizhuGameStatus.PLAYING);

        // 应该抛出异常
        assertThatThrownBy(() -> biddingManager.handleGrab(1001L, 3))
                .hasMessageContaining("状态错误");
    }

    @Test
    @DisplayName("测试非当前玩家不能抢地主")
    void testNonCurrentPlayerCannotGrab() {
        // 当前玩家是1001，1002不能抢
        assertThatThrownBy(() -> biddingManager.handleGrab(1002L, 3))
                .hasMessageContaining("不是当前玩家");
    }

    // ==================== 轮次测试 ====================

    @Test
    @DisplayName("测试两轮叫地主")
    void testTwoRounds() {
        // 第一轮：所有玩家都不抢
        biddingManager.handleNotGrab(1001L);
        biddingManager.handleNotGrab(1002L);
        biddingManager.handleNotGrab(1003L);

        // 第二轮开始，应该还是从玩家1开始
        // 玩家1抢地主
        biddingManager.handleGrab(1001L, 2);

        // 玩家2不抢
        biddingManager.handleNotGrab(1002L);

        // 玩家3不抢
        biddingManager.handleNotGrab(1003L);

        // 验证地主是玩家1
        assertThat(room.getLandlordId()).isEqualTo(1001L);
        assertThat(room.getCurrentMultiple()).isEqualTo(2);
    }

    // ==================== 记录查询测试 ====================

    @Test
    @DisplayName("测试获取叫地主记录")
    void testGetBidRecords() {
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
