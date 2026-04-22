package com.pokergame.game.doudizhu.action;
import com.iohao.game.widget.light.room.flow.RoomCreateContext;
import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.rule.ValidationResult;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import com.pokergame.game.doudizhu.rule.DoudizhuRuleChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 游戏操作 Action 测试
 *
 * 测试目标：
 * 1. 准备操作
 * 2. 抢地主/不抢地主
 * 3. 出牌操作
 * 4. 过牌操作
 * 5. 回合管理
 * 6. 游戏结束
 *
 * @author poker-platform
 */
//@DisplayName("游戏操作测试")
//class GameActionTest {
//
//    private DoudizhuRoomService roomService;
//    private DoudizhuRoom room;
//    private DoudizhuPlayer player1;
//    private DoudizhuPlayer player2;
//    private DoudizhuPlayer player3;
//    private DoudizhuRuleChecker ruleChecker;
//
//    @BeforeEach
//    void setUp() {
//        roomService = DoudizhuRoomService.me();
//        roomService.getRoomMap().clear();
//        roomService.getUserRoomMap().clear();
//
//        // 创建房间 - 初始状态为 WAITING
//        var createContext = RoomCreateContext.of(1001L).setSpaceSize(3);
//        room = roomService.createRoom(createContext);
//        room.setOwnerId(1001L);
//        room.setMaxPlayers(3);
//        room.setGameStatus(DoudizhuGameStatus.WAITING);  // 初始状态为 WAITING
//
//        // 创建玩家
//        player1 = new DoudizhuPlayer(1001L, "玩家1");
//        player2 = new DoudizhuPlayer(1002L, "玩家2");
//        player3 = new DoudizhuPlayer(1003L, "玩家3");
//
//        // 加入房间 - WAITING 状态可以加入
//        room.addDoudizhuPlayer(player1);
//        room.addDoudizhuPlayer(player2);
//        room.addDoudizhuPlayer(player3);
//
//        roomService.addRoom(room);
//        roomService.addPlayer(room, player1);
//        roomService.addPlayer(room, player2);
//        roomService.addPlayer(room, player3);
//
//        // 设置出牌顺序
//        room.setPlayOrder(List.of(1001L, 1002L, 1003L), 1001L);
//
//        // 设置玩家手牌
//        player1.setHandCards(createFullHandCards());
//        player2.setHandCards(createFullHandCards());
//        player3.setHandCards(createFullHandCards());
//
//        // 设置游戏状态为 PLAYING（开始游戏后）
//        room.setGameStatus(DoudizhuGameStatus.PLAYING);
//
//        ruleChecker = new DoudizhuRuleChecker(room);
//    }
//
//    // ==================== 准备操作测试 ====================
//
//    @Test
//    @DisplayName("测试玩家准备")
//    void testPlayerReady() {
//        player1.setReady(true);
//        assertThat(player1.isReady()).isTrue();
//    }
//
//    @Test
//    @DisplayName("测试玩家取消准备")
//    void testPlayerCancelReady() {
//        player1.setReady(true);
//        assertThat(player1.isReady()).isTrue();
//
//        player1.setReady(false);
//        assertThat(player1.isReady()).isFalse();
//    }
//
//    @Test
//    @DisplayName("测试所有玩家准备后自动开始")
//    void testAllReadyAutoStart() {
//        player1.setReady(true);
//        player2.setReady(true);
//        player3.setReady(true);
//
//        assertThat(room.isAllReady()).isTrue();
//    }
//
//    // ==================== 叫地主测试 ====================
//
//    @Test
//    @DisplayName("测试抢地主")
//    void testGrabLandlord() {
//        room.setGameStatus(DoudizhuGameStatus.BIDDING);
//
//        player1.setLandlord(true);
//        player1.setBidMultiple(3);
//
//        assertThat(player1.isLandlord()).isTrue();
//        assertThat(player1.getBidMultiple()).isEqualTo(3);
//    }
//
//    @Test
//    @DisplayName("测试不抢地主")
//    void testNotGrabLandlord() {
//        room.setGameStatus(DoudizhuGameStatus.BIDDING);
//
//        player1.setLandlord(false);
//
//        assertThat(player1.isLandlord()).isFalse();
//    }
//
//    @Test
//    @DisplayName("测试叫地主状态才能抢地主")
//    void testOnlyBiddingStatusCanGrab() {
//        room.setGameStatus(DoudizhuGameStatus.PLAYING);
//
//        player1.setLandlord(false);
//        assertThat(player1.isLandlord()).isFalse();
//    }
//
//    // ==================== 出牌操作测试 ====================
//
//    @Test
//    @DisplayName("测试当前玩家出牌成功")
//    void testCurrentPlayerPlayCardSuccess() {
//        // 设置上家出牌（非首出）
//        List<Card> lastCards = Arrays.asList(Card.of(1)); // ♠4
//        room.setLastPlayCards(lastCards);
//        room.updateLastPlay(1002L, lastCards, CardPattern.SINGLE, 4, 0);
//
//        // 当前玩家是 player1 (1001)
//        assertThat(room.isCurrentPlayer(1001L)).isTrue();
//
//        // 出牌：♠5
//        List<Card> cards = Arrays.asList(Card.of(2));
//
//        ValidationResult result = ruleChecker.validatePlay(1001L, cards);
//
//        assertThat(result.isValid()).isTrue();
//        assertThat(result.getPattern()).isEqualTo(CardPattern.SINGLE);
//        assertThat(result.getMainRank()).isEqualTo(5);
//    }
//
//    @Test
//    @DisplayName("测试非当前玩家不能出牌")
//    void testNonCurrentPlayerCannotPlay() {
//        // 当前玩家是 player1 (1001)
//        assertThat(room.isCurrentPlayer(1001L)).isTrue();
//
//        // player2 尝试出牌
//        List<Card> cards = Arrays.asList(Card.of(2));
//
//        ValidationResult result = ruleChecker.validatePlay(1002L, cards);
//
//        assertThat(result.isValid()).isFalse();
//        assertThat(result.getErrorCode()).isEqualTo(20002);
//        assertThat(result.getErrorMessage()).isEqualTo("不是你的回合");
//    }
//
//    @Test
//    @DisplayName("测试游戏未开始时不能出牌")
//    void testCannotPlayWhenGameNotStarted() {
//        room.setGameStatus(DoudizhuGameStatus.WAITING);
//
//        List<Card> cards = Arrays.asList(Card.of(2));
//
//        ValidationResult result = ruleChecker.validatePlay(1001L, cards);
//
//        // RuleChecker 不校验游戏状态，由 Action 层校验
//        assertThat(result.isValid()).isTrue();
//    }
//
//    @Test
//    @DisplayName("测试出牌后手牌减少")
//    void testHandCardsDecreaseAfterPlay() {
//        room.setLastPlayCards(null);
//
//        int beforeCount = player1.getCardCount();
//        List<Card> cards = Arrays.asList(Card.of(2));
//
//        ValidationResult result = ruleChecker.validatePlay(1001L, cards);
//
//        if (result.isValid()) {
//            player1.removeCards(cards);
//            int afterCount = player1.getCardCount();
//            assertThat(afterCount).isEqualTo(beforeCount - 1);
//        }
//    }
//
//    @Test
//    @DisplayName("测试出牌后切换下一个玩家")
//    void testNextPlayerAfterPlay() {
//        assertThat(room.getCurrentPlayer().getUserId()).isEqualTo(1001L);
//
//        room.nextTurn();
//        assertThat(room.getCurrentPlayer().getUserId()).isEqualTo(1002L);
//
//        room.nextTurn();
//        assertThat(room.getCurrentPlayer().getUserId()).isEqualTo(1003L);
//
//        room.nextTurn();
//        assertThat(room.getCurrentPlayer().getUserId()).isEqualTo(1001L);
//    }
//
//    @Test
//    @DisplayName("测试炸弹出牌增加倍率")
//    void testBombIncreaseMultiplier() {
//        int beforeMultiplier = room.getMultiplier();
//
//        room.addBomb();
//
//        assertThat(room.getMultiplier()).isEqualTo(beforeMultiplier + 1);
//    }
//
//    // ==================== 过牌操作测试 ====================
//
//    @Test
//    @DisplayName("测试过牌成功")
//    void testPassSuccess() {
//        // 验证当前玩家是 player1
//        assertThat(room.getCurrentPlayer()).isNotNull();
//        assertThat(room.getCurrentPlayer().getUserId()).isEqualTo(1001L);
//
//        room.nextTurn();
//
//        // 验证切换到 player2
//        assertThat(room.getCurrentPlayer()).isNotNull();
//        assertThat(room.getCurrentPlayer().getUserId()).isEqualTo(1002L);
//    }
//
//    @Test
//    @DisplayName("测试过牌后切换下一个玩家")
//    void testNextPlayerAfterPass() {
//        assertThat(room.getCurrentPlayer().getUserId()).isEqualTo(1001L);
//
//        room.nextTurn();
//        assertThat(room.getCurrentPlayer().getUserId()).isEqualTo(1002L);
//    }
//
//    // ==================== 游戏结束测试 ====================
//
//    @Test
//    @DisplayName("测试玩家出完牌游戏结束")
//    void testGameEndWhenPlayerOutOfCards() {
//        player1.setHandCards(new ArrayList<>());
//        assertThat(player1.getCardCount()).isEqualTo(0);
//
//        room.changeGameStatus(DoudizhuGameStatus.FINISHED);
//        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.FINISHED);
//    }
//
//    @Test
//    @DisplayName("测试游戏结束后不能出牌")
//    void testCannotPlayAfterGameEnd() {
//        room.setGameStatus(DoudizhuGameStatus.FINISHED);
//
//        List<Card> cards = Arrays.asList(Card.of(2));
//
//        assertThat(room.getGameStatus()).isEqualTo(DoudizhuGameStatus.FINISHED);
//    }
//
//    // ==================== 边界测试 ====================
//
//    @Test
//    @DisplayName("测试空手牌不能出牌")
//    void testEmptyHandCannotPlay() {
//        player1.setHandCards(new ArrayList<>());
//        assertThat(player1.getCardCount()).isEqualTo(0);
//
//        List<Card> cards = Arrays.asList(Card.of(2));
//
//        ValidationResult result = ruleChecker.validatePlay(1001L, cards);
//
//        assertThat(result.isValid()).isFalse();
//        assertThat(result.getErrorCode()).isEqualTo(30003);
//    }
//
//    @Test
//    @DisplayName("测试出牌数量超过手牌数量")
//    void testPlayMoreCardsThanHand() {
//        int handSize = player1.getCardCount();
//        List<Card> cards = new ArrayList<>();
//        for (int i = 0; i < handSize + 1; i++) {
//            cards.add(Card.of(i));
//        }
//
//        ValidationResult result = ruleChecker.validatePlay(1001L, cards);
//
//        assertThat(result.isValid()).isFalse();
//    }
//
//    // ==================== 辅助方法 ====================
//
//    private List<Card> createFullHandCards() {
//        List<Card> handCards = new ArrayList<>();
//        for (int i = 0; i < 20; i++) {
//            handCards.add(Card.of(i));
//        }
//        return handCards;
//    }
//}
