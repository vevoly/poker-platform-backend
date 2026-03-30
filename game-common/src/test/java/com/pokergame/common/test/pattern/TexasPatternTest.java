package com.pokergame.common.test.pattern;
import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.pattern.PatternResult;
import com.pokergame.common.pattern.TexasPatternRecognizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 德州扑克牌型识别测试
 */
@DisplayName("德州扑克牌型识别测试")
public class TexasPatternTest {

    private static TexasPatternRecognizer recognizer;

    @BeforeAll
    static void setUp() {
        recognizer = new TexasPatternRecognizer();
    }

    @Test
    @DisplayName("测试高牌识别")
    void testHighCard() {
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(14), Card.of(28), Card.of(42), Card.of(5), Card.of(19), Card.of(33)
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.HIGH_CARD);
    }

    @Test
    @DisplayName("测试一对识别")
    void testOnePair() {
        // 包含一对3
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13),  // 一对3
                Card.of(14), Card.of(28), Card.of(42), Card.of(5), Card.of(19)
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.ONE_PAIR);
    }

    @Test
    @DisplayName("测试同花识别")
    void testFlush() {
        // 构造一个只能组成同花、不能组成同花顺的牌型
        // 例如：5张黑桃，但不是顺子
        List<Card> cards = Arrays.asList(
                Card.of(0),   // 黑桃3
                Card.of(1),   // 黑桃4
                Card.of(2),   // 黑桃5
                Card.of(3),   // 黑桃6
                Card.of(5),   // 黑桃8（跳过7，避免顺子）
                Card.of(14),  // 红桃4（干扰）
                Card.of(28)   // 梅花5（干扰）
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.FLUSH);
    }

    @Test
    @DisplayName("测试同花顺识别")
    void testStraightFlush() {
        // 五张黑桃
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(1), Card.of(2), Card.of(3), Card.of(4),  // 黑桃3-7
                Card.of(14), Card.of(28)
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.STRAIGHT_FLUSH);
    }

    @Test
    @DisplayName("测试四条识别")
    void testFourOfKind() {
        // 3,4,5,6,7
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39), Card.of(1),
                Card.of(14), Card.of(28)
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.FOUR_OF_KIND);
    }

    @Test
    @DisplayName("测试顺子识别")
    void testStraight() {
        // 构造一个只能组成顺子、不能组成四条、同花顺的牌型
        // 例如：不同花色的 3,4,5,6,7
        List<Card> cards = Arrays.asList(
                Card.of(0),   // 黑桃3
                Card.of(14),  // 红桃4
                Card.of(28),  // 梅花5
                Card.of(42),  // 方块6
                Card.of(4),   // 黑桃7
                Card.of(18),  // 红桃8
                Card.of(32)   // 梅花9
        );
        // 这7张牌中，最优5张是 3,4,5,6,7 顺子
        // 不会形成四条或同花顺
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.STRAIGHT_POKER);
    }

    @Test
    @DisplayName("测试葫芦识别")
    void testFullHouse() {
        // 三条3 + 一对4
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26),  // 三条3
                Card.of(1), Card.of(14),               // 一对4
                Card.of(28), Card.of(42)
        );
        PatternResult result = recognizer.recognize(cards);

        assertThat(result.getPattern()).isEqualTo(CardPattern.FULL_HOUSE);
    }
}
