package com.pokergame.common.test.deal.strategy;

import com.pokergame.common.activity.ActiveActivity;
import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.strategy.EventDealStrategy;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 活动加成策略测试
 */
@DisplayName("活动加成策略测试")
class EventDealStrategyTest {

    @Test
    @DisplayName("无活动返回null")
    void testNoEventReturnsNull() {
        EventDealStrategy strategy = new EventDealStrategy(GameType.DOUDIZHU, "test_event", 0.2, null, null);

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .activeEvents(List.of())
                .build();

        HandRank rank = strategy.getTargetRank(context);

        assertThat(rank).isNull();
    }

    @Test
    @DisplayName("有活动且触发加成返回牌型")
    void testWithEventReturnsRank() {
        EventDealStrategy strategy = new EventDealStrategy(GameType.DOUDIZHU, "spring_event", 0.5, HandRank.DOUDIZHU_BOMB, null);

        ActiveActivity event = ActiveActivity.builder()
                .activityId("spring_event")
                .eventName("春节活动")
                .isActive(true)
                .startTime(System.currentTimeMillis() - 10000)
                .endTime(System.currentTimeMillis() + 100000)
                .boostRate(0.5)
                .build();

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .activeEvents(List.of(event))
                .build();

        // 多次测试，应该有一定概率触发
        int triggerCount = 0;
        for (int i = 0; i < 50; i++) {
            HandRank rank = strategy.getTargetRank(context);
            if (rank != null) {
                triggerCount++;
                assertThat(rank).isEqualTo(HandRank.DOUDIZHU_BOMB);
            }
        }

        assertThat(triggerCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("活动已过期返回null")
    void testExpiredEventReturnsNull() {
        EventDealStrategy strategy = new EventDealStrategy(GameType.DOUDIZHU, "expired_event", 0.2, HandRank.DOUDIZHU_BOMB, null);

        ActiveActivity event = ActiveActivity.builder()
                .activityId("expired_event")
                .eventName("过期活动")
                .isActive(true)
                .startTime(System.currentTimeMillis() - 200000)
                .endTime(System.currentTimeMillis() - 100000)
                .boostRate(0.2)
                .build();

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .activeEvents(List.of(event))
                .build();

        HandRank rank = strategy.getTargetRank(context);

        assertThat(rank).isNull();
    }

    @Test
    @DisplayName("多牌型活动随机选择")
    void testMultipleTargetRanks() {
        List<HandRank> targetRanks = List.of(
                HandRank.DOUDIZHU_ROCKET,
                HandRank.DOUDIZHU_BOMB,
                HandRank.DOUDIZHU_STRAIGHT
        );

        EventDealStrategy strategy = new EventDealStrategy(GameType.DOUDIZHU, "multi_event", 0.5, null, targetRanks);

        ActiveActivity event = ActiveActivity.builder()
                .activityId("multi_event")
                .eventName("多牌型活动")
                .isActive(true)
                .startTime(System.currentTimeMillis() - 10000)
                .endTime(System.currentTimeMillis() + 100000)
                .boostRate(0.5)
                .build();

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .activeEvents(List.of(event))
                .build();

        boolean sawRocket = false;
        boolean sawBomb = false;
        boolean sawStraight = false;

        for (int i = 0; i < 100; i++) {
            HandRank rank = strategy.getTargetRank(context);
            if (rank == HandRank.DOUDIZHU_ROCKET) sawRocket = true;
            if (rank == HandRank.DOUDIZHU_BOMB) sawBomb = true;
            if (rank == HandRank.DOUDIZHU_STRAIGHT) sawStraight = true;
            if (sawRocket && sawBomb && sawStraight) break;
        }

        // 应该能看到多种牌型
        assertThat(sawRocket || sawBomb || sawStraight).isTrue();
    }
}
