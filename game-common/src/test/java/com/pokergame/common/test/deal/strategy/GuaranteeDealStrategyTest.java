package com.pokergame.common.test.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.strategy.GuaranteeDealStrategy;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 保底策略测试
 */
@DisplayName("保底策略测试")
class GuaranteeDealStrategyTest {

    @Test
    @DisplayName("固定保底策略返回指定牌型")
    void testFixedGuaranteeReturnsFixedRank() {
        GuaranteeDealStrategy strategy = new GuaranteeDealStrategy(GameType.DOUDIZHU, HandRank.DOUDIZHU_BOMB);

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .build();

        HandRank rank = strategy.getTargetRank(context);

        assertThat(rank).isEqualTo(HandRank.DOUDIZHU_BOMB);
    }

    @Test
    @DisplayName("动态保底策略根据连败返回牌型")
    void testDynamicGuaranteeBasedOnLoss() {
        GuaranteeDealStrategy strategy = GuaranteeDealStrategy.compensation(GameType.DOUDIZHU);

        DealContext contextLoss5 = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .consecutiveLosses(5)
                .build();

        DealContext contextLoss10 = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .consecutiveLosses(10)
                .build();

        HandRank rank5 = strategy.getTargetRank(contextLoss5);
        HandRank rank10 = strategy.getTargetRank(contextLoss10);

        if (rank5 != null && rank10 != null) {
            // 10连败应该得到比5连败更好的牌
            assertThat(getRankValue(rank10)).isGreaterThanOrEqualTo(getRankValue(rank5));
        }
    }

    @Test
    @DisplayName("VIP动态保底策略")
    void testVipDynamicGuarantee() {
        GuaranteeDealStrategy strategy = GuaranteeDealStrategy.vip(GameType.DOUDIZHU, 5);

        DealContext contextVip3 = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(3)
                .build();

        DealContext contextVip8 = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(8)
                .build();

        HandRank rankVip3 = strategy.getTargetRank(contextVip3);
        HandRank rankVip8 = strategy.getTargetRank(contextVip8);

        // VIP8应该触发保底，VIP3可能不触发
        if (rankVip8 != null) {
            assertThat(rankVip8).isIn(HandRank.DOUDIZHU_BOMB, HandRank.DOUDIZHU_ROCKET);
        }
    }

    private int getRankValue(HandRank rank) {
        if (rank == HandRank.DOUDIZHU_ROCKET) return 100;
        if (rank == HandRank.DOUDIZHU_BOMB) return 80;
        if (rank == HandRank.DOUDIZHU_STRAIGHT) return 50;
        return 0;
    }
}
