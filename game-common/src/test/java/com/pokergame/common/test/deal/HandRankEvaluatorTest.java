package com.pokergame.common.test.deal;

import com.pokergame.common.card.Card;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.HandRankEvaluator;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 手牌强度评估测试
 */
@DisplayName("手牌强度评估测试")
public class HandRankEvaluatorTest {

    // ==================== 斗地主强度测试 ====================

    @Test
    @DisplayName("斗地主 - 王炸")
    void testDoudizhuRocket() {
        List<Card> cards = Arrays.asList(
                Card.of(52),  // 小王
                Card.of(53)   // 大王
        );
        HandRank rank = HandRankEvaluator.evaluate(GameType.DOUDIZHU, cards);
        assertThat(rank).isEqualTo(HandRank.DOUDIZHU_ROCKET);
    }

    @Test
    @DisplayName("斗地主 - 炸弹")
    void testDoudizhuBomb() {
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39)  // 四张3
        );
        HandRank rank = HandRankEvaluator.evaluate(GameType.DOUDIZHU, cards);
        assertThat(rank).isEqualTo(HandRank.DOUDIZHU_BOMB);
    }

    @Test
    @DisplayName("斗地主 - 顺子")
    void testDoudizhuStraight() {
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(1), Card.of(2), Card.of(3), Card.of(4)  // 3,4,5,6,7
        );
        HandRank rank = HandRankEvaluator.evaluate(GameType.DOUDIZHU, cards);
        assertThat(rank).isEqualTo(HandRank.DOUDIZHU_STRAIGHT);
    }

    // ==================== 德州扑克强度测试 ====================

    @Test
    @DisplayName("德州 - 皇家同花顺")
    void testTexasRoyalFlush() {
        // 10,J,Q,K,A 同花
        List<Card> cards = Arrays.asList(
                Card.of(7),   // ♠10
                Card.of(8),   // ♠J
                Card.of(9),   // ♠Q
                Card.of(10),  // ♠K
                Card.of(11)   // ♠A
        );
        HandRank rank = HandRankEvaluator.evaluate(GameType.TEXAS, cards);
        assertThat(rank).isEqualTo(HandRank.TEXAS_ROYAL_FLUSH);
    }

    @Test
    @DisplayName("德州 - 四条")
    void testTexasFourOfKind() {
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(13), Card.of(26), Card.of(39),  // 四张3
                Card.of(1)                                          // 单张4
        );
        HandRank rank = HandRankEvaluator.evaluate(GameType.TEXAS, cards);
        assertThat(rank).isEqualTo(HandRank.TEXAS_FOUR_OF_KIND);
    }

    // ==================== 牛牛强度测试 ====================

    @Test
    @DisplayName("牛牛 - 五小牛")
    void testBullFiveSmall() {
        List<Card> cards = Arrays.asList(
                Card.of(11),  // ♠A (1点)
                Card.of(24),  // ♥A (1点)
                Card.of(12),  // ♠2 (2点)
                Card.of(25),  // ♥2 (2点)
                Card.of(13)   // ♥3 (3点)
        );
        HandRank rank = HandRankEvaluator.evaluate(GameType.BULL, cards);
        assertThat(rank).isEqualTo(HandRank.BULL_FIVE_SMALL);
    }

    @Test
    @DisplayName("牛牛 - 牛牛")
    void testBullBull() {
        // 3,4,3=10；5,5=10
        List<Card> cards = Arrays.asList(
                Card.of(0),   // ♠3
                Card.of(14),  // ♥4
                Card.of(26),  // ♣3
                Card.of(2),   // ♠5
                Card.of(15)   // ♥5
        );
        HandRank rank = HandRankEvaluator.evaluate(GameType.BULL, cards);
        assertThat(rank).isEqualTo(HandRank.BULL_BULL);
    }

    @Test
    @DisplayName("牛牛 - 无牛")
    void testBullNo() {
        List<Card> cards = Arrays.asList(
                Card.of(0), Card.of(1), Card.of(2), Card.of(3), Card.of(4)  // 3,4,5,6,7
        );
        HandRank rank = HandRankEvaluator.evaluate(GameType.BULL, cards);
        assertThat(rank).isEqualTo(HandRank.BULL_NO);
    }
}