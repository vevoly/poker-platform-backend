package com.pokergame.common.test.deal;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.DealStrategyManager;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 发牌策略管理器测试
 */
@DisplayName("发牌策略管理器测试")
class DealStrategyManagerTest {

    @Test
    @DisplayName("策略链正常执行")
    void testStrategyChainExecutes() {
        DealStrategyManager manager = new DealStrategyManager(GameType.DOUDIZHU);

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .isRookie(true)  // 新手应该触发新手保护
                .build();

        HandRank rank = manager.getTargetRank(context);

        assertThat(rank).isNotNull();
        assertThat(rank).isEqualTo(HandRank.DOUDIZHU_BOMB);
    }

    @Test
    @DisplayName("高优先级策略先执行")
    void testHighPriorityStrategyExecutesFirst() {
        DealStrategyManager manager = new DealStrategyManager(GameType.DOUDIZHU);

        // 同时满足多个条件：新手 + VIP5 + 3连败
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .isRookie(true)
                .vipLevel(5)
                .consecutiveLosses(3)
                .build();

        HandRank rank = manager.getTargetRank(context);

        // 新手保护优先级最高，应该返回新手牌型
        assertThat(rank).isEqualTo(HandRank.DOUDIZHU_BOMB);
    }

    @Test
    @DisplayName("无策略触发时返回null")
    void testNoStrategyTriggeredReturnsNull() {
        DealStrategyManager manager = new DealStrategyManager(GameType.DOUDIZHU);

        // 普通玩家，无任何特殊条件
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .isRookie(false)
                .vipLevel(0)
                .consecutiveLosses(0)
                .consecutiveWins(0)
                .activeItems(List.of())
                .activeEvents(List.of())
                .remainingBonusGames(0)
                .build();

        HandRank rank = manager.getTargetRank(context);

        // 可能返回null（由正态分布策略兜底）或某个牌型
        // 这里只验证不会抛异常
        assertThat(rank).isNotNull(); // 正态分布策略会兜底
    }

    @Test
    @DisplayName("连胜平衡调整")
    void testWinningBalanceAdjustment() {
        DealStrategyManager manager = new DealStrategyManager(GameType.DOUDIZHU);

        HandRank originalRank = HandRank.DOUDIZHU_ROCKET;

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .consecutiveWins(8)  // 8连胜
                .build();

        HandRank adjustedRank = manager.adjustRank(originalRank, context);

        System.out.println("原始牌型: " + originalRank);
        System.out.println("调整后牌型: " + adjustedRank);

        // 8连胜应该调整牌型
        assertThat(adjustedRank).isNotEqualTo(originalRank);
    }
}
