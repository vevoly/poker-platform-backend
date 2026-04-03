package com.pokergame.common.test.pattern;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.pattern.PatternResult;
import com.pokergame.common.pattern.texas.TexasPatternRecognizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 德州扑克牌型识别器测试
 *
 * Card ID 映射参考：
 * 黑桃: 3(0),4(1),5(2),6(3),7(4),8(5),9(6),10(7),J(8),Q(9),K(10),A(11),2(12)
 * 红桃: 3(13),4(14),5(15),6(16),7(17),8(18),9(19),10(20),J(21),Q(22),K(23),A(24),2(25)
 * 梅花: 3(26),4(27),5(28),6(29),7(30),8(31),9(32),10(33),J(34),Q(35),K(36),A(37),2(38)
 * 方块: 3(39),4(40),5(41),6(42),7(43),8(44),9(45),10(46),J(47),Q(48),K(49),A(50),2(51)
 * 小王: 52, 大王: 53
 */
@DisplayName("德州扑克牌型识别器测试")
class TexasPatternRecognizerTest {

    private TexasPatternRecognizer recognizer;

    @BeforeEach
    void setUp() {
        recognizer = new TexasPatternRecognizer();
    }

    // ==================== 基础牌型测试 ====================

    @Test
    @DisplayName("识别高牌")
    void testHighCard() {
        // 不同花色，无顺子，无同花
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(14),  // ♥4
                Card.of(28),  // ♣5
                Card.of(42),  // ♦6
                Card.of(8),   // ♠J
                Card.of(22),  // ♥Q
                Card.of(49)   // ♦K
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.HIGH_CARD);
    }

    @Test
    @DisplayName("识别一对")
    void testOnePair() {
        // 一对3，其他都是不同牌值
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(13),  // ♥3
                Card.of(1),   // ♠4
                Card.of(14),  // ♥4? 不对，这是红桃4，会形成两对
                Card.of(2),   // ♠5
                Card.of(15),  // ♥5
                Card.of(3)    // ♠6
        );
        // 修正：只保留一对3，其他用不会形成对子的牌
        List<Card> fixedCards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(13),  // ♥3
                Card.of(1),   // ♠4
                Card.of(28),  // ♣5
                Card.of(42),  // ♦6
                Card.of(8),   // ♠J
                Card.of(22)   // ♥Q
        );
        PatternResult result = recognizer.recognize(fixedCards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.ONE_PAIR);
    }

    @Test
    @DisplayName("识别两对")
    void testTwoPair() {
        // 一对3 + 一对4
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(13),  // ♥3
                Card.of(1),   // ♠4
                Card.of(14),  // ♥4
                Card.of(2),   // ♠5
                Card.of(28),  // ♣5? 不对，会形成三条
                Card.of(42)   // ♦6
        );
        // 修正：一对3 + 一对4 + 单牌5,6,J
        List<Card> fixedCards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(13),  // ♥3
                Card.of(1),   // ♠4
                Card.of(14),  // ♥4
                Card.of(2),   // ♠5
                Card.of(42),  // ♦6
                Card.of(8)    // ♠J
        );
        PatternResult result = recognizer.recognize(fixedCards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.TWO_PAIR);
    }

    @Test
    @DisplayName("识别三条")
    void testThreeOfKind() {
        // 三条3，其他都是不同牌值且不形成顺子/同花
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(13),  // ♥3
                Card.of(26),  // ♣3
                Card.of(1),   // ♠4
                Card.of(28),  // ♣5
                Card.of(42),  // ♦6
                Card.of(8)    // ♠J
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.THREE_OF_KIND);
    }

    @Test
    @DisplayName("识别顺子 - 3,4,5,6,7")
    void testStraight() {
        // 不同花色的 3,4,5,6,7
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(14),  // ♥4
                Card.of(28),  // ♣5
                Card.of(42),  // ♦6
                Card.of(4),   // ♠7
                Card.of(8),   // ♠J（干扰）
                Card.of(22)   // ♥Q（干扰）
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.STRAIGHT_POKER);
    }

    @Test
    @DisplayName("识别同花")
    void testFlush() {
        // 5张黑桃，但不是顺子
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(1),   // ♠4
                Card.of(2),   // ♠5
                Card.of(3),   // ♠6
                Card.of(8),   // ♠J（跳过7，避免顺子）
                Card.of(13),  // ♥3（干扰）
                Card.of(26)   // ♣3（干扰）
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.FLUSH);
    }

    @Test
    @DisplayName("识别葫芦")
    void testFullHouse() {
        // 三条3 + 一对4
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(13),  // ♥3
                Card.of(26),  // ♣3
                Card.of(1),   // ♠4
                Card.of(14),  // ♥4
                Card.of(2),   // ♠5（干扰）
                Card.of(28)   // ♣5（干扰）
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.FULL_HOUSE);
    }

    @Test
    @DisplayName("识别四条")
    void testFourOfKind() {
        // 四条3 + 单牌4
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(13),  // ♥3
                Card.of(26),  // ♣3
                Card.of(39),  // ♦3
                Card.of(1),   // ♠4
                Card.of(2),   // ♠5（干扰）
                Card.of(28)   // ♣5（干扰）
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.FOUR_OF_KIND);
    }

    @Test
    @DisplayName("识别同花顺")
    void testStraightFlush() {
        // 黑桃 3,4,5,6,7
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(1),   // ♠4
                Card.of(2),   // ♠5
                Card.of(3),   // ♠6
                Card.of(4),   // ♠7
                Card.of(13),  // ♥3（干扰）
                Card.of(26)   // ♣3（干扰）
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.STRAIGHT_FLUSH);
    }

    @Test
    @DisplayName("识别皇家同花顺")
    void testRoyalFlush() {
        // 黑桃 10,J,Q,K,A
        List<Card> cards = Arrays.asList(
                Card.of(7),   // ♠10
                Card.of(8),   // ♠J
                Card.of(9),   // ♠Q
                Card.of(10),  // ♠K
                Card.of(11),  // ♠A
                Card.of(13),  // ♥3（干扰）
                Card.of(26)   // ♣3（干扰）
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.ROYAL_FLUSH);
    }

    // ==================== 特殊顺子测试 ====================

    @Test
    @DisplayName("识别特殊顺子 A-2-3-4-5")
    void testAceLowStraight() {
        // A,2,3,4,5 不同花色
        List<Card> cards = Arrays.asList(
                Card.of(11),  // ♠A
                Card.of(12),  // ♠2
                Card.of(13),  // ♥3
                Card.of(14),  // ♥4
                Card.of(15),  // ♥5
                Card.of(0),   // ♠3（干扰）
                Card.of(1)    // ♠4（干扰）
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.STRAIGHT_POKER);
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("牌数不足5张返回PASS")
    void testInsufficientCards() {
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(1), Card.of(2), Card.of(3)
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.PASS);
    }

    @Test
    @DisplayName("空牌返回PASS")
    void testEmptyCards() {
        List<Card> cards = Arrays.asList();
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.PASS);
    }
}
