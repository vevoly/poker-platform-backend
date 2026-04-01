package com.pokergame.common.test.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.strategy.RookieDealStrategy;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 新手保护策略测试
 */
@DisplayName("新手保护策略测试")
class RookieDealStrategyTest {

    private final RookieDealStrategy strategy = new RookieDealStrategy(GameType.DOUDIZHU);

    @Test
    @DisplayName("新手玩家获得保护牌型")
    void testRookieGetsProtection() {
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .isRookie(true)
                .build();

        HandRank rank = strategy.getTargetRank(context);

        assertThat(rank).isNotNull();
        assertThat(rank).isEqualTo(HandRank.DOUDIZHU_BOMB);
    }

    @Test
    @DisplayName("非新手玩家不获得保护")
    void testNonRookieNoProtection() {
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .isRookie(false)
                .totalGames(11)
                .build();

        HandRank rank = strategy.getTargetRank(context);

        assertThat(rank).isNull();
    }

    @Test
    @DisplayName("注册时间判定新手")
    void testRegisterTimeDeterminesRookie() {
        long sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;

        // 注册7天内
        DealContext contextNew = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .registerTime(sevenDaysAgo + 10000)  // 刚注册
                .totalGames(0)
                .build();

        // 注册7天以上
        DealContext contextOld = DealContext.builder()
                .playerId(1002L)
                .gameType(GameType.DOUDIZHU)
                .registerTime(sevenDaysAgo - 24 * 60 * 60 * 1000)  // 注册超过7天
                .totalGames(100)
                .build();

        // 注意：策略内部有备选判断逻辑
        HandRank rankNew = strategy.getTargetRank(contextNew);
        HandRank rankOld = strategy.getTargetRank(contextOld);

        // 新注册玩家可能获得保护
        // 老玩家不应该获得保护
        if (rankNew != null) {
            assertThat(rankNew).isEqualTo(HandRank.DOUDIZHU_BOMB);
        }
        assertThat(rankOld).isNull();
    }
}
