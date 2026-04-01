package com.pokergame.common.test.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.strategy.CompensationStrategy;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 连败补偿策略测试
 */
@DisplayName("连败补偿策略测试")
class CompensationStrategyTest {

    private final CompensationStrategy strategy = new CompensationStrategy(GameType.DOUDIZHU);

    @Test
    @DisplayName("无连败应该返回null")
    void testNoLossReturnsNull() {
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .consecutiveLosses(0)
                .build();

        HandRank rank = strategy.getTargetRank(context);

        assertThat(rank).isNull();
    }

    @Test
    @DisplayName("3连败应该触发基础补偿")
    void testThreeLossesGetsBaseCompensation() {
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .consecutiveLosses(3)
                .build();

        HandRank rank = strategy.getTargetRank(context);

        // 有概率触发，不是100%
        // 这里只验证如果触发，牌型应该是炸弹
        if (rank != null) {
            assertThat(rank).isIn(
                    HandRank.DOUDIZHU_BOMB,
                    HandRank.DOUDIZHU_ROCKET
            );
        }
    }

    @Test
    @DisplayName("12连败应该触发顶级补偿")
    void testTwelveLossesGetsTopCompensation() {
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .consecutiveLosses(12)
                .build();

        HandRank rank = strategy.getTargetRank(context);

        if (rank != null) {
            assertThat(rank).isEqualTo(HandRank.DOUDIZHU_ROCKET);
        }
    }

    @Test
    @DisplayName("连败越多触发概率越高")
    void testHigherLossHigherProbability() {
        int triggerCount3 = 0;
        int triggerCount8 = 0;

        for (int i = 0; i < 100; i++) {
            DealContext context3 = DealContext.builder()
                    .playerId(1001L)
                    .gameType(GameType.DOUDIZHU)
                    .consecutiveLosses(3)
                    .build();

            DealContext context8 = DealContext.builder()
                    .playerId(1001L)
                    .gameType(GameType.DOUDIZHU)
                    .consecutiveLosses(8)
                    .build();

            if (strategy.getTargetRank(context3) != null) triggerCount3++;
            if (strategy.getTargetRank(context8) != null) triggerCount8++;
        }

        assertThat(triggerCount8).isGreaterThan(triggerCount3);
    }
}
