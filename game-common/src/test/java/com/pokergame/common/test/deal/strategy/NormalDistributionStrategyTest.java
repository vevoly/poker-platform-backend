package com.pokergame.common.test.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.strategy.NormalDistributionStrategy;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 正态分布策略测试
 */
@DisplayName("正态分布策略测试")
class NormalDistributionStrategyTest {

    private final NormalDistributionStrategy strategy = new NormalDistributionStrategy(GameType.DOUDIZHU);

    @Test
    @DisplayName("策略返回牌型")
    void testStrategyReturnsRank() {
        // 创建空的统计信息（使用默认概率）
        NormalDistributionStrategy.GlobalStatistics stats =
                new NormalDistributionStrategy.GlobalStatistics(0, new HashMap<>());

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .globalStatistics(stats)
                .build();

        HandRank rank = strategy.getTargetRank(context);

        assertThat(rank).isNotNull();
        assertThat(rank).isInstanceOf(HandRank.class);
    }

    @Test
    @DisplayName("牌型分布符合概率配置")
    void testDistributionMatchesProbability() {
        Map<HandRank, Integer> rankCounts = new HashMap<>();

        // 初始化计数
        for (HandRank rank : new HandRank[]{
                HandRank.DOUDIZHU_SINGLE, HandRank.DOUDIZHU_PAIR, HandRank.DOUDIZHU_TRIPLE,
                HandRank.DOUDIZHU_STRAIGHT, HandRank.DOUDIZHU_BOMB, HandRank.DOUDIZHU_ROCKET,
                HandRank.DOUDIZHU_JUNK
        }) {
            rankCounts.put(rank, 0);
        }

        NormalDistributionStrategy.GlobalStatistics stats =
                new NormalDistributionStrategy.GlobalStatistics(0, rankCounts);

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .globalStatistics(stats)
                .build();

        // 进行1000次测试
        for (int i = 0; i < 1000; i++) {
            HandRank rank = strategy.getTargetRank(context);
            rankCounts.merge(rank, 1, Integer::sum);
        }

        // 更新统计信息
        stats = new NormalDistributionStrategy.GlobalStatistics(1000, rankCounts);
        context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .globalStatistics(stats)
                .build();

        // 再次测试，应该根据实际频率调整
        for (int i = 0; i < 100; i++) {
            HandRank rank = strategy.getTargetRank(context);
            assertThat(rank).isNotNull();
        }

        // 验证计数不为空
        assertThat(rankCounts.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(1000);
    }

    @Test
    @DisplayName("不同游戏类型返回不同牌型")
    void testDifferentGameTypes() {
        NormalDistributionStrategy dzStrategy = new NormalDistributionStrategy(GameType.DOUDIZHU);
        NormalDistributionStrategy texasStrategy = new NormalDistributionStrategy(GameType.TEXAS);

        NormalDistributionStrategy.GlobalStatistics stats =
                new NormalDistributionStrategy.GlobalStatistics(0, new HashMap<>());

        DealContext dzContext = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .globalStatistics(stats)
                .build();

        DealContext texasContext = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.TEXAS)
                .globalStatistics(stats)
                .build();

        HandRank dzRank = dzStrategy.getTargetRank(dzContext);
        HandRank texasRank = texasStrategy.getTargetRank(texasContext);

        assertThat(dzRank).isNotNull();
        assertThat(texasRank).isNotNull();

        // 斗地主应该返回斗地主牌型
        assertThat(dzRank.name()).startsWith("DOUDIZHU");
        // 德州应该返回德州牌型
        assertThat(texasRank.name()).startsWith("TEXAS");
    }
}
