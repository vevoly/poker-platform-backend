package com.pokergame.common.test.pattern;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.pattern.PatternResult;
import com.pokergame.common.pattern.bull.BullPatternRecognizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 牛牛牌型识别器测试
 *
 * 牛牛规则：
 * - 五小牛：5张牌点数总和 < 10
 * - 炸弹：4张相同点数的牌
 * - 五花牛：5张牌全是花牌（J、Q、K）
 * - 牛牛：3张牌之和为10的倍数，另2张之和也为10的倍数
 * - 牛X：3张牌之和为10的倍数，另2张之和除以10的余数为X
 * - 无牛：无法组成3张牌和为10的倍数
 *
 * @author poker-platform
 */
@DisplayName("牛牛牌型识别器测试")
class BullPatternRecognizerTest {

    private BullPatternRecognizer recognizer;

    @BeforeEach
    void setUp() {
        recognizer = new BullPatternRecognizer();
    }

    // ==================== 特殊牌型测试 ====================

    @Test
    @DisplayName("识别五小牛")
    void testFiveSmall() {
        // A,A,2,2,3 = 1+1+2+2+3=9 < 10
        List<Card> cards = Arrays.asList(
                Card.of(11),  // ♠A (1点)
                Card.of(24),  // ♥A (1点)
                Card.of(12),  // ♠2 (2点)
                Card.of(25),  // ♥2 (2点)
                Card.of(13)   // ♥3 (3点)
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.FIVE_SMALL);
    }

    @Test
    @DisplayName("识别炸弹")
    void testFourBomb() {
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39),  // 四张3
                Card.of(1)  // 任意单牌
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.FOUR_BOMB);
    }

    @Test
    @DisplayName("识别五花牛")
    void testFiveFlower() {
        List<Card> cards = Arrays.asList(
                Card.of(8),   // ♠J
                Card.of(9),   // ♠Q
                Card.of(10),  // ♠K
                Card.of(21),  // ♥J
                Card.of(22)   // ♥Q
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.BULL_BULL);
    }

    @Test
    @DisplayName("识别四花牛")
    void testFourFlower() {
        List<Card> cards = Arrays.asList(
                Card.of(8),   // ♠J
                Card.of(9),   // ♠Q
                Card.of(10),  // ♠K
                Card.of(21),  // ♥J
                Card.of(7)    // ♠10 (10点)
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.BULL_BULL);
    }

    // ==================== 牛牛测试 ====================

    @Test
    @DisplayName("识别牛牛")
    void testBullBull() {
        // 3,4,3=10；5,5=10
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(14),  // ♥4
                Card.of(26),  // ♣3
                Card.of(2),   // ♠5
                Card.of(15)   // ♥5
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.BULL_BULL);
    }

    // ==================== 牛X测试 ====================

    @Test
    @DisplayName("识别牛九")
    void testBullNine() {
        // 3,3,4=10；5,4=9
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(26),  // ♣3
                Card.of(14),  // ♥4
                Card.of(2),   // ♠5
                Card.of(40)   // ♦4
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.BULL_9);
    }

    @Test
    @DisplayName("识别牛八")
    void testBullEight() {
        // 3,3,4=10；5,3=8
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(26),  // ♣3
                Card.of(14),  // ♥4
                Card.of(2),   // ♠5
                Card.of(39)   // ♦3
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.BULL_8);
    }

    @Test
    @DisplayName("识别牛一")
    void testBullOne() {
        // 3,4,3=10；5,6=11 → 牛1
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(14),  // ♥4
                Card.of(26),  // ♣3
                Card.of(2),   // ♠5
                Card.of(42)   // ♦6
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.BULL_1);
    }

    // ==================== 无牛测试 ====================

    @Test
    @DisplayName("识别无牛")
    void testNoBull() {
        // 3,4,5,6,7 无法组成10的倍数
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(1), Card.of(2), Card.of(3), Card.of(4)
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.NO_BULL);
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("牌数不足5张返回PASS")
    void testInsufficientCards() {
        List<Card> cards = Arrays.asList(Card.of(0), Card.of(1), Card.of(2), Card.of(3));
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
