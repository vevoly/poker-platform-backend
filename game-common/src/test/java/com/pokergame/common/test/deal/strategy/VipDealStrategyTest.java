package com.pokergame.common.test.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.strategy.VipDealStrategy;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VIP特权策略测试
 */
@DisplayName("VIP特权策略测试")
class VipDealStrategyTest {

    private final VipDealStrategy strategy = new VipDealStrategy(GameType.DOUDIZHU);

    @Test
    @DisplayName("VIP0应该返回null")
    void testVip0ReturnsNull() {
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(0)
                .build();

        HandRank rank = strategy.getTargetRank(context);

        assertThat(rank).isNull();
    }

    @Test
    @DisplayName("VIP5应该有较高概率获得好牌")
    void testVip5GetsBetterRank() {
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(5)
                .build();

        int goodRankCount = 0;

        for (int i = 0; i < 100; i++) {
            HandRank rank = strategy.getTargetRank(context);
            if (rank == HandRank.DOUDIZHU_ROCKET || rank == HandRank.DOUDIZHU_BOMB) {
                goodRankCount++;
            }
        }

        // VIP5应该有一定概率获得好牌
        assertThat(goodRankCount).isGreaterThan(10);
    }

    @Test
    @DisplayName("VIP9应该比VIP5获得更多好牌")
    void testVip9BetterThanVip5() {
        DealContext contextVip5 = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(5)
                .build();

        DealContext contextVip9 = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(9)
                .build();

        int vip5GoodCount = 0;
        int vip9GoodCount = 0;

        for (int i = 0; i < 100; i++) {
            HandRank rank5 = strategy.getTargetRank(contextVip5);
            HandRank rank9 = strategy.getTargetRank(contextVip9);

            if (rank5 == HandRank.DOUDIZHU_ROCKET || rank5 == HandRank.DOUDIZHU_BOMB) {
                vip5GoodCount++;
            }
            if (rank9 == HandRank.DOUDIZHU_ROCKET || rank9 == HandRank.DOUDIZHU_BOMB) {
                vip9GoodCount++;
            }
        }

        assertThat(vip9GoodCount).isGreaterThan(vip5GoodCount);
    }
}
