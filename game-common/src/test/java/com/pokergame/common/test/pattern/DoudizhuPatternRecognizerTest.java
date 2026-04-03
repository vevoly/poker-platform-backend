package com.pokergame.common.test.pattern;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.pattern.PatternResult;
import com.pokergame.common.pattern.doudizhu.DoudizhuPatternRecognizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 斗地主牌型识别器测试
 *
 * @author poker-platform
 */
@DisplayName("斗地主牌型识别器测试")
class DoudizhuPatternRecognizerTest {

    private DoudizhuPatternRecognizer recognizer;

    @BeforeEach
    void setUp() {
        recognizer = new DoudizhuPatternRecognizer();
    }

    // ==================== 基础牌型测试 ====================

    @Test
    @DisplayName("识别单张")
    void testRecognizeSingle() {
        List<Card> cards = Arrays.asList(Card.of(0));  // ♠3
        PatternResult result = recognizer.recognize(cards);

        assertThat(result).isNotNull();
        assertThat(result.getPattern()).isEqualTo(CardPattern.SINGLE);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("识别对子")
    void testRecognizePair() {
        List<Card> cards = Arrays.asList(Card.of(0), Card.of(13));  // ♠3, ♥3
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.PAIR);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("识别三张")
    void testRecognizeTriplet() {
        List<Card> cards = Arrays.asList(Card.of(0), Card.of(13), Card.of(26));  // ♠3, ♥3, ♣3
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.THREE);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("识别顺子 - 3,4,5,6,7")
    void testRecognizeStraight() {
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(1),   // ♠4
                Card.of(2),   // ♠5
                Card.of(3),   // ♠6
                Card.of(4)    // ♠7
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.STRAIGHT);
        assertThat(result.getMainRank()).isEqualTo(3);
        assertThat(result.getSubRank()).isEqualTo(5);  // 5张顺子
    }

    @Test
    @DisplayName("识别炸弹")
    void testRecognizeBomb() {
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39)  // 四张3
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.BOMB);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("识别王炸")
    void testRecognizeRocket() {
        List<Card> cards = Arrays.asList(Card.of(52), Card.of(53));  // 小王, 大王
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.ROCKET);
    }

    // ==================== 复杂牌型测试 ====================

    @Test
    @DisplayName("识别三带一")
    void testRecognizeThreeWithSingle() {
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26),  // 三张3
                Card.of(1)                              // 单张4
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.THREE_WITH_SINGLE);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("识别三带二")
    void testRecognizeThreeWithPair() {
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26),  // 三张3
                Card.of(1), Card.of(14)                 // 一对4
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.THREE_WITH_PAIR);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("识别连对 - 33,44,55")
    void testRecognizeStraightPair() {
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13),   // 33
                Card.of(1), Card.of(14),   // 44
                Card.of(2), Card.of(15)    // 55
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.STRAIGHT_PAIR);
        assertThat(result.getMainRank()).isEqualTo(3);
        assertThat(result.getSubRank()).isEqualTo(3);  // 3连对
    }

    @Test
    @DisplayName("识别飞机 - 333,444")
    void testRecognizePlane() {
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26),   // 333
                Card.of(1), Card.of(14), Card.of(27)    // 444
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.PLANE);
        assertThat(result.getMainRank()).isEqualTo(3);
        assertThat(result.getSubRank()).isEqualTo(2);  // 2连飞机
    }

    @Test
    @DisplayName("识别四带二单")
    void testRecognizeFourWithSingles() {
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39),  // 四张3
                Card.of(1), Card.of(2)                               // 单张4,5
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.FOUR_WITH_SINGLE);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("空牌返回null")
    void testEmptyCards() {
        List<Card> cards = Arrays.asList();
        PatternResult result = recognizer.recognize(cards);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("null返回null")
    void testNullCards() {
        PatternResult result = recognizer.recognize(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("2不能进顺子")
    void testTwoCannotInStraight() {
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(1),   // ♠4
                Card.of(2),   // ♠5
                Card.of(3),   // ♠6
                Card.of(12)   // ♠2 (2不能进顺子)
        );
        PatternResult result = recognizer.recognize(cards);

        // 不应该识别为顺子
        assertThat(result.getPattern()).isNotEqualTo(CardPattern.STRAIGHT);
    }
}