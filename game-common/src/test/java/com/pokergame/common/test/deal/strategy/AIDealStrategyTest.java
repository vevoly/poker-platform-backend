package com.pokergame.common.test.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.strategy.AIDealStrategy;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
/**
 * AI难度策略测试
 */
@DisplayName("AI难度策略测试")
class AIDealStrategyTest {

    @Test
    @DisplayName("AI玩家应该返回牌型")
    void testAIPlayerGetsRank() {
        AIDealStrategy strategy = new AIDealStrategy(GameType.DOUDIZHU, 5);

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .isAI(true)
                .aiDifficulty(5)
                .build();

        HandRank rank = strategy.getTargetRank(context);

        assertThat(rank).isNotNull();
        assertThat(rank).isInstanceOf(HandRank.class);
    }

    @Test
    @DisplayName("非AI玩家应该返回null")
    void testNonAIPlayerReturnsNull() {
        AIDealStrategy strategy = new AIDealStrategy(GameType.DOUDIZHU, 5);

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .isAI(false)
                .build();

        HandRank rank = strategy.getTargetRank(context);

        assertThat(rank).isNull();
    }

    @Test
    @DisplayName("不同难度返回不同牌型")
    void testDifferentDifficulty() {
        // 低难度 AI
        AIDealStrategy easyStrategy = new AIDealStrategy(GameType.DOUDIZHU, 2);
        // 高难度 AI
        AIDealStrategy hardStrategy = new AIDealStrategy(GameType.DOUDIZHU, 9);

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .isAI(true)
                .build();

        // 多次测试，高难度应该更容易拿到好牌
        int easyHighRankCount = 0;
        int hardHighRankCount = 0;

        for (int i = 0; i < 100; i++) {
            HandRank easyRank = easyStrategy.getTargetRank(context);
            HandRank hardRank = hardStrategy.getTargetRank(context);

            if (easyRank == HandRank.DOUDIZHU_ROCKET || easyRank == HandRank.DOUDIZHU_BOMB) {
                easyHighRankCount++;
            }
            if (hardRank == HandRank.DOUDIZHU_ROCKET || hardRank == HandRank.DOUDIZHU_BOMB) {
                hardHighRankCount++;
            }
        }

        // 高难度 AI 拿到好牌的概率应该更高
        assertThat(hardHighRankCount).isGreaterThan(easyHighRankCount);
    }
}
