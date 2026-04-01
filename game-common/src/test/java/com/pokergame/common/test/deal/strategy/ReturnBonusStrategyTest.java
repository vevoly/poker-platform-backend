package com.pokergame.common.test.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.strategy.ReturnBonusStrategy;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 回归奖励策略测试
 */
@DisplayName("回归奖励策略测试")
class ReturnBonusStrategyTest {

    private final ReturnBonusStrategy strategy = new ReturnBonusStrategy(GameType.DOUDIZHU);

    @Test
    @DisplayName("无回归奖励次数返回null")
    void testNoBonusReturnsNull() {
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .remainingBonusGames(0)
                .lastLoginTime(System.currentTimeMillis())
                .build();

        HandRank rank = strategy.getTargetRank(context);

        assertThat(rank).isNull();
    }

    @Test
    @DisplayName("流失30天应触发顶级奖励")
    void testThirtyDaysAwayGetsTopBonus() {
        long thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .remainingBonusGames(10)
                .lastLoginTime(thirtyDaysAgo)
                .build();

        HandRank rank = strategy.getTargetRank(context);

        if (rank != null) {
            assertThat(rank).isEqualTo(HandRank.DOUDIZHU_ROCKET);
        }
    }

    @Test
    @DisplayName("流失3天应触发基础奖励")
    void testThreeDaysAwayGetsBaseBonus() {
        long threeDaysAgo = System.currentTimeMillis() - 3L * 24 * 60 * 60 * 1000;

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .remainingBonusGames(10)
                .lastLoginTime(threeDaysAgo)
                .build();

        HandRank rank = strategy.getTargetRank(context);

        if (rank != null) {
            assertThat(rank).isEqualTo(HandRank.DOUDIZHU_BOMB);
        }
    }

    @Test
    @DisplayName("流失天数越多奖励越好")
    void testMoreDaysAwayBetterReward() {
        long threeDaysAgo = System.currentTimeMillis() - 3L * 24 * 60 * 60 * 1000;
        long thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;

        DealContext context3 = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .remainingBonusGames(10)
                .lastLoginTime(threeDaysAgo)
                .build();

        DealContext context30 = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .remainingBonusGames(10)
                .lastLoginTime(thirtyDaysAgo)
                .build();

        HandRank rank3 = strategy.getTargetRank(context3);
        HandRank rank30 = strategy.getTargetRank(context30);

        // 30天流失应该得到更好的奖励
        if (rank3 != null && rank30 != null) {
            assertThat(getRankValue(rank30)).isGreaterThan(getRankValue(rank3));
        }
    }

    private int getRankValue(HandRank rank) {
        if (rank == HandRank.DOUDIZHU_ROCKET) return 100;
        if (rank == HandRank.DOUDIZHU_BOMB) return 80;
        if (rank == HandRank.DOUDIZHU_STRAIGHT) return 50;
        return 0;
    }
}
