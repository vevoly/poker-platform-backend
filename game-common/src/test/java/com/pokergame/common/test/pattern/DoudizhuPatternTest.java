package com.pokergame.common.test.pattern;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.pattern.PatternResult;
import com.pokergame.common.pattern.DoudizhuPatternRecognizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 斗地主牌型识别测试
 */
@DisplayName("斗地主牌型识别测试")
public class DoudizhuPatternTest {

    private static DoudizhuPatternRecognizer recognizer;

    @BeforeAll
    static void setUp() {
        recognizer = new DoudizhuPatternRecognizer();
    }

    @Test
    @DisplayName("测试单张识别")
    void testSingle() {
        List<Card> cards = Arrays.asList(Card.of(0)); // 黑桃3
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.SINGLE);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试对子识别")
    void testPair() {
        List<Card> cards = Arrays.asList(Card.of(0), Card.of(13)); // 黑桃3和红桃3
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.PAIR);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试顺子识别")
    void testStraight() {
        // 3,4,5,6,7
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39), Card.of(1)
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.STRAIGHT);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试炸弹识别")
    void testBomb() {
        // 四张3
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39)
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.BOMB);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试王炸识别")
    void testRocket() {
        List<Card> cards = Arrays.asList(Card.of(52), Card.of(53)); // 小王和大王
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.ROCKET);
    }

    @Test
    @DisplayName("测试三带一识别")
    void testThreeWithSingle() {
        // 三张3 + 单张4
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26),  // 三张3
                Card.of(1)                             // 单张4
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.THREE_WITH_SINGLE);
        assertThat(result.getMainRank()).isEqualTo(3);
    }

    @Test
    @DisplayName("测试牌型比较 - 炸弹大于顺子")
    void testCompare() {
        List<Card> straight = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39), Card.of(1)
        );
        List<Card> bomb = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39)
        );

        PatternResult straightResult = recognizer.recognize(straight);
        PatternResult bombResult = recognizer.recognize(bomb);

        assertThat(recognizer.canBeat(straightResult, bombResult)).isTrue();
        assertThat(recognizer.canBeat(bombResult, straightResult)).isFalse();
    }
}