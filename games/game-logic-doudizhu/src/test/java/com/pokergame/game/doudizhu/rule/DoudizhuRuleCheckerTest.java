package com.pokergame.game.doudizhu.rule;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.rule.ValidationResult;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 斗地主出牌校验测试（不使用 Mock）
 *
 * 测试目标：
 * 1. 牌型识别正确性
 * 2. 大小比较正确性
 * 3. 首出限制
 * 4. 手牌完整性校验
 *
 * @author poker-platform
 */
@DisplayName("斗地主出牌校验测试")
class DoudizhuRuleCheckerTest {

    private DoudizhuRoom room;
    private DoudizhuRuleChecker ruleChecker;
    private DoudizhuPlayer player;

    @BeforeEach
    void setUp() {
        // 创建真实房间对象
        room = new DoudizhuRoom();
        room.setRoomId(10001L);
        room.setOwnerId(1001L);
        room.setMaxPlayers(3);
        room.setGameStatus(DoudizhuGameStatus.PLAYING);

        // 创建真实玩家
        player = new DoudizhuPlayer(1001L, "测试玩家");
        room.addDoudizhuPlayer(player);

        // 设置玩家手牌（完整的17张手牌，用于手牌完整性测试）
        player.setHandCards(createFullHandCards());

        // 设置出牌顺序
        room.setPlayOrder(List.of(1001L, 1002L, 1003L), 1001L);

        // 创建规则检查器
        ruleChecker = new DoudizhuRuleChecker(room);
    }

    // ==================== 基础牌型测试 ====================

    @Test
    @DisplayName("测试单张出牌")
    void testSingleCard() {
        // 清空上家出牌（首出）
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        List<Card> cards = Arrays.asList(Card.of(0)); // ♠3

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.SINGLE);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试对子出牌")
    void testPairCards() {
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        List<Card> cards = Arrays.asList(Card.of(0), Card.of(13)); // ♠3, ♥3

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.PAIR);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试三张出牌")
    void testTripleCards() {
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        List<Card> cards = Arrays.asList(Card.of(0), Card.of(13), Card.of(26)); // ♠3, ♥3, ♣3

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.THREE);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试三带一出牌")
    void testThreeWithSingleCards() {
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), // 三张3
                Card.of(1)                            // 单张4
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.THREE_WITH_SINGLE);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试顺子出牌 - 3,4,5,6,7")
    void testStraightCards() {
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(1), Card.of(2), Card.of(3), Card.of(4)
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.STRAIGHT);
        assertThat(result.getMainRank()).isEqualTo(3);
        assertThat(result.getSubRank()).isEqualTo(5); // 5张顺子
    }

    @Test
    @DisplayName("测试连对出牌 - 33,44,55")
    void testStraightPairCards() {
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), // 33
                Card.of(1), Card.of(14), // 44
                Card.of(2), Card.of(15)  // 55
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.STRAIGHT_PAIR);
        assertThat(result.getMainRank()).isEqualTo(3);
        assertThat(result.getSubRank()).isEqualTo(3); // 3连对
    }

    @Test
    @DisplayName("测试炸弹出牌")
    void testBombCards() {
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39) // 四张3
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.BOMB);
        assertThat(result.isBomb()).isTrue();
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试王炸出牌")
    void testRocketCards() {
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        List<Card> cards = Arrays.asList(Card.of(52), Card.of(53)); // 小王, 大王

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.ROCKET);
        assertThat(result.isRocket()).isTrue();
    }

    // ==================== 大小比较测试 ====================

    @Test
    @DisplayName("测试单张比较 - 5 > 3 能压")
    void testSingleCanBeat() {
        // 设置上家出牌：♠3
        List<Card> lastCards = Arrays.asList(Card.of(0));
        room.setLastPlayCards(lastCards);
        room.updateLastPlay(1002L, lastCards, CardPattern.SINGLE, 3, 0);

        // 当前出牌：♠5
        List<Card> currentCards = Arrays.asList(Card.of(2));

        ValidationResult result = ruleChecker.validatePlay(1001L, currentCards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.SINGLE);
        assertThat(result.getMainRank()).isEqualTo(5);
    }

    @Test
    @DisplayName("测试单张比较 - 3 < 5 不能压")
    void testSingleCannotBeat() {
        // 设置上家出牌：♠5
        List<Card> lastCards = Arrays.asList(Card.of(2));
        room.setLastPlayCards(lastCards);
        room.updateLastPlay(1002L, lastCards, CardPattern.SINGLE, 5, 0);

        // 当前出牌：♠3
        List<Card> currentCards = Arrays.asList(Card.of(0));

        ValidationResult result = ruleChecker.validatePlay(1001L, currentCards);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo(30002);
        assertThat(result.getErrorMessage()).isEqualTo("不能压过上家的牌");
    }

    @Test
    @DisplayName("测试对子比较 - 55 > 33 能压")
    void testPairCanBeat() {
        // 上家出牌：33
        List<Card> lastCards = Arrays.asList(Card.of(0), Card.of(13));
        room.setLastPlayCards(lastCards);
        room.updateLastPlay(1002L, lastCards, CardPattern.PAIR, 3, 0);

        // 当前出牌：55
        List<Card> currentCards = Arrays.asList(Card.of(2), Card.of(15));

        ValidationResult result = ruleChecker.validatePlay(1001L, currentCards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.PAIR);
        assertThat(result.getMainRank()).isEqualTo(5);
    }

    @Test
    @DisplayName("测试炸弹可以压任何非炸弹")
    void testBombBeatAnything() {
        // 上家出牌：单张5
        List<Card> lastCards = Arrays.asList(Card.of(2));
        room.setLastPlayCards(lastCards);
        room.updateLastPlay(1002L, lastCards, CardPattern.SINGLE, 5, 0);

        // 当前出牌：炸弹3
        List<Card> currentCards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39)
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, currentCards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.BOMB);
        assertThat(result.isBomb()).isTrue();
    }

    @Test
    @DisplayName("测试炸弹比较大小 - 炸弹4 > 炸弹3")
    void testBombCompare() {
        // 上家出牌：炸弹3
        List<Card> lastCards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39)
        );
        room.setLastPlayCards(lastCards);
        room.updateLastPlay(1002L, lastCards, CardPattern.BOMB, 3, 0);

        // 当前出牌：炸弹4
        List<Card> currentCards = Arrays.asList(
                Card.of(1), Card.of(14), Card.of(27), Card.of(40)
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, currentCards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.BOMB);
        assertThat(result.getMainRank()).isEqualTo(4);
    }

    @Test
    @DisplayName("测试王炸可以压任何牌")
    void testRocketBeatAnything() {
        // 上家出牌：炸弹4
        List<Card> lastCards = Arrays.asList(
                Card.of(1), Card.of(14), Card.of(27), Card.of(40)
        );
        room.setLastPlayCards(lastCards);
        room.updateLastPlay(1002L, lastCards, CardPattern.BOMB, 4, 0);

        // 当前出牌：王炸
        List<Card> currentCards = Arrays.asList(Card.of(52), Card.of(53));

        ValidationResult result = ruleChecker.validatePlay(1001L, currentCards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.ROCKET);
        assertThat(result.isRocket()).isTrue();
    }

    // ==================== 首出限制测试 ====================

    @Test
    @DisplayName("测试首出不能出炸弹")
    void testFirstPlayCannotPlayBomb() {
        // 清空上家出牌（首出）
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39) // 炸弹
        );

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo(30006);
        assertThat(result.getErrorMessage()).isEqualTo("首出不能出炸弹");
    }

    @Test
    @DisplayName("测试首出可以出单张")
    void testFirstPlayCanPlaySingle() {
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        List<Card> cards = Arrays.asList(Card.of(0)); // ♠3

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getPattern()).isEqualTo(CardPattern.SINGLE);
    }

    // ==================== 手牌完整性测试 ====================

    @Test
    @DisplayName("测试手牌中没有要出的牌")
    void testCardsNotInHand() {
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        // 出一张不存在的牌（ID=99 不存在）
        List<Card> cards = Arrays.asList(Card.of(99));

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo(30003);
        assertThat(result.getErrorMessage()).isEqualTo("手牌中没有这些牌");
    }

    @Test
    @DisplayName("测试出牌数量超过手牌数量")
    void testPlayMoreCardsThanHand() {
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        // 玩家只有17张手牌，尝试出18张（不可能）
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            cards.add(Card.of(i));
        }

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isFalse();
        // 应该返回牌型无效或手牌不足
        assertThat(result.getErrorCode()).isIn(30001, 30003);
    }

    // ==================== 非法操作测试 ====================

    @Test
    @DisplayName("测试不是当前回合玩家不能出牌")
    void testNotCurrentPlayerCannotPlay() {
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        // 当前玩家是 1001，用 1002 出牌
        List<Card> cards = Arrays.asList(Card.of(0));

        ValidationResult result = ruleChecker.validatePlay(1002L, cards);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo(20002);
        assertThat(result.getErrorMessage()).isEqualTo("不是你的回合");
    }

    @Test
    @DisplayName("测试游戏未开始时不能出牌")
    void testGameNotStartedCannotPlay() {
        room.setGameStatus(DoudizhuGameStatus.WAITING);
        room.setLastPlayCards(null);
        room.setLastPattern(null);

        List<Card> cards = Arrays.asList(Card.of(0));

        ValidationResult result = ruleChecker.validatePlay(1001L, cards);

        assertThat(result.isValid()).isFalse();
        // 应该在调用前就校验游戏状态，这里由 Action 层处理
        // RuleChecker 只校验回合和手牌
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建完整的17张手牌（用于手牌完整性测试）
     */
    private List<Card> createFullHandCards() {
        List<Card> handCards = new ArrayList<>();
        // 添加各种牌值，确保手牌完整
        for (int i = 0; i < 17; i++) {
            handCards.add(Card.of(i));
        }
        return handCards;
    }
}
