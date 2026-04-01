package com.pokergame.common.test.deal.strategy;

import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.strategy.WinningBalanceStrategy;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 连胜平衡策略测试
 */
@DisplayName("连胜平衡策略测试")
class WinningBalanceStrategyTest {

    private final WinningBalanceStrategy strategy = new WinningBalanceStrategy(GameType.DOUDIZHU);

    @Test
    @DisplayName("无连胜不应该调整牌型")
    void testNoWinNoAdjustment() {
        HandRank original = HandRank.DOUDIZHU_ROCKET;
        HandRank adjusted = strategy.getAdjustedRank(original, 0);

        assertThat(adjusted).isEqualTo(original);
    }

    @Test
    @DisplayName("3连胜应该轻微调整")
    void testThreeWinsLightAdjustment() {
        HandRank original = HandRank.DOUDIZHU_ROCKET;
        HandRank adjusted = strategy.getAdjustedRank(original, 3);

        // 应该降级，但可能不是最低
        assertThat(adjusted).isNotEqualTo(original);
        assertThat(adjusted).isIn(
                HandRank.DOUDIZHU_BOMB,
                HandRank.DOUDIZHU_STRAIGHT,
                HandRank.DOUDIZHU_TRIPLE,
                HandRank.DOUDIZHU_PAIR,
                HandRank.DOUDIZHU_SINGLE
        );
    }

    @Test
    @DisplayName("12连胜应该最大调整")
    void testTwelveWinsMaxAdjustment() {
        HandRank original = HandRank.DOUDIZHU_ROCKET;
        HandRank adjusted = strategy.getAdjustedRank(original, 12);

        // 应该降到最低级别
        assertThat(adjusted).isIn(
                HandRank.DOUDIZHU_SINGLE,
                HandRank.DOUDIZHU_PAIR
        );
    }

    @Test
    @DisplayName("连胜越多调整幅度越大")
    void testMoreWinsMoreAdjustment() {
        HandRank original = HandRank.DOUDIZHU_ROCKET;

        HandRank adjusted3 = strategy.getAdjustedRank(original, 3);
        HandRank adjusted8 = strategy.getAdjustedRank(original, 8);

        // 8连胜应该比3连胜调整得更低
        int index3 = getRankIndex(adjusted3);
        int index8 = getRankIndex(adjusted8);

        assertThat(index8).isLessThanOrEqualTo(index3);
    }

    private int getRankIndex(HandRank rank) {
        HandRank[] ranks = {
                HandRank.DOUDIZHU_SINGLE, HandRank.DOUDIZHU_PAIR,
                HandRank.DOUDIZHU_TRIPLE, HandRank.DOUDIZHU_STRAIGHT,
                HandRank.DOUDIZHU_BOMB, HandRank.DOUDIZHU_ROCKET
        };
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i] == rank) return i;
        }
        return -1;
    }
}
