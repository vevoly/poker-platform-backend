package com.pokergame.common.test.deal;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.DealStrategyManager;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.item.ActiveItem;
import com.pokergame.common.activity.ActiveActivity;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 策略叠加测试
 *
 * 测试目标：验证多个策略可以叠加生效
 *
 * @author poker-platform
 */
@DisplayName("策略叠加测试")
class StrategyStackTest {

    private DealStrategyManager strategyManager;

    @BeforeEach
    void setUp() {
        strategyManager = new DealStrategyManager(GameType.DOUDIZHU);
    }

    // ==================== 单策略测试 ====================

    @Test
    @DisplayName("VIP策略单独生效")
    void testVipAlone() {
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(5)
                .consecutiveLosses(0)
                .consecutiveWins(0)
                .build();

        int triggerCount = 0;
        int totalAttempts = 5000;

        for (int i = 0; i < totalAttempts; i++) {
            HandRank rank = strategyManager.getTargetRank(context);
            if (rank == HandRank.DOUDIZHU_BOMB || rank == HandRank.DOUDIZHU_ROCKET) {
                triggerCount++;
            }
        }

        double rate = triggerCount / (double) totalAttempts;
        System.out.println("VIP5好牌概率: " + rate);

        assertThat(rate).isBetween(0.05, 0.25);
    }

    @Test
    @DisplayName("连败补偿策略单独生效")
    void testCompensationAlone() {
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(0)
                .consecutiveLosses(5)
                .consecutiveWins(0)
                .build();

        int triggerCount = 0;
        int totalAttempts = 500;

        for (int i = 0; i < totalAttempts; i++) {
            HandRank rank = strategyManager.getTargetRank(context);
            if (rank != null) {
                triggerCount++;
            }
        }

        double rate = triggerCount / (double) totalAttempts;
        System.out.println("5连败补偿触发概率: " + rate);

        // 5连败应该大概率触发补偿（50%-90%）
        assertThat(rate).isBetween(0.50, 0.95);
    }

    // ==================== 策略叠加测试 ====================

    @Test
    @DisplayName("VIP + 活动加成 叠加测试")
    void testVipPlusEvent() {
        // 场景1：只有VIP
        DealContext vipOnly = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(5)
                .consecutiveLosses(0)
                .activeEvents(List.of())
                .build();

        // 场景2：VIP + 活动
        DealContext vipPlusEvent = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(5)
                .consecutiveLosses(0)
                .activeEvents(List.of(
                        createEvent("spring_festival", 0.2)
                ))
                .build();

        int vipOnlyCount = 0;
        int vipPlusEventCount = 0;
        int totalAttempts = 5000;

        for (int i = 0; i < totalAttempts; i++) {
            HandRank rank1 = strategyManager.getTargetRank(vipOnly);
            if (rank1 == HandRank.DOUDIZHU_BOMB || rank1 == HandRank.DOUDIZHU_ROCKET) {
                vipOnlyCount++;
            }

            HandRank rank2 = strategyManager.getTargetRank(vipPlusEvent);
            if (rank2 == HandRank.DOUDIZHU_BOMB || rank2 == HandRank.DOUDIZHU_ROCKET) {
                vipPlusEventCount++;
            }
        }

        double rateVipOnly = vipOnlyCount / (double) totalAttempts;
        double rateVipPlusEvent = vipPlusEventCount / (double) totalAttempts;

        System.out.println("VIP5单独好牌概率: " + rateVipOnly);
        System.out.println("VIP5+活动 好牌概率: " + rateVipPlusEvent);

        // 叠加后概率应该更高
        assertThat(rateVipPlusEvent).isGreaterThan(rateVipOnly);
    }

    @Test
    @DisplayName("连败补偿 + 活动加成 叠加测试")
    void testCompensationPlusEvent() {
        // 场景1：只有连败补偿
        DealContext compensationOnly = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .consecutiveLosses(5)
                .activeEvents(List.of())
                .build();

        // 场景2：连败补偿 + 活动
        DealContext compensationPlusEvent = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .consecutiveLosses(5)
                .activeEvents(List.of(
                        createEvent("summer_event", 0.25)
                ))
                .build();

        int compensationOnlyCount = 0;
        int compensationPlusEventCount = 0;
        int totalAttempts = 500;

        for (int i = 0; i < totalAttempts; i++) {
            HandRank rank1 = strategyManager.getTargetRank(compensationOnly);
            if (rank1 != null) {
                compensationOnlyCount++;
            }

            HandRank rank2 = strategyManager.getTargetRank(compensationPlusEvent);
            if (rank2 != null) {
                compensationPlusEventCount++;
            }
        }

        double rateCompensationOnly = compensationOnlyCount / (double) totalAttempts;
        double rateCompensationPlusEvent = compensationPlusEventCount / (double) totalAttempts;

        System.out.println("连败补偿单独触发概率: " + rateCompensationOnly);
        System.out.println("连败补偿+活动触发概率: " + rateCompensationPlusEvent);

        // 叠加后概率应该更高
        assertThat(rateCompensationPlusEvent).isGreaterThan(rateCompensationOnly);
    }

    @Test
    @DisplayName("道具加成 + VIP + 活动 多重叠加测试")
    void testMultipleStack() {
        // 普通玩家
        DealContext normal = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(0)
                .activeItems(List.of())
                .activeEvents(List.of())
                .build();

        // 多重加成玩家：VIP5 + 好运卡 + 春节活动
        DealContext stacked = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(5)
                .activeItems(List.of(
                        createItem("lucky_card", 0.2)
                ))
                .activeEvents(List.of(
                        createEvent("spring_festival", 0.2)
                ))
                .build();

        int normalCount = 0;
        int stackedCount = 0;
        int totalAttempts = 5000;

        for (int i = 0; i < totalAttempts; i++) {
            HandRank rank1 = strategyManager.getTargetRank(normal);
            if (rank1 == HandRank.DOUDIZHU_BOMB || rank1 == HandRank.DOUDIZHU_ROCKET) {
                normalCount++;
            }

            HandRank rank2 = strategyManager.getTargetRank(stacked);
            if (rank2 == HandRank.DOUDIZHU_BOMB || rank2 == HandRank.DOUDIZHU_ROCKET) {
                stackedCount++;
            }
        }

        double rateNormal = normalCount / (double) totalAttempts;
        double rateStacked = stackedCount / (double) totalAttempts;

        System.out.println("普通玩家好牌概率: " + rateNormal);
        System.out.println("多重加成玩家好牌概率: " + rateStacked);

        // 多重加成玩家应该有更高概率
        assertThat(rateStacked).isGreaterThan(rateNormal);
        assertThat(rateStacked).isGreaterThan(0.1);
    }

    // ==================== 保底策略优先级测试 ====================

    @Test
    @DisplayName("保底策略优先级最高 - 道具保底")
    void testGuaranteePriorityWithItem() {
        // 有保底道具的玩家
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(9)           // 高VIP
                .consecutiveLosses(10) // 高连败
                .activeItems(List.of(
                        createGuaranteeItem("guarantee_card", "BOMB")
                ))
                .build();

        for (int i = 0; i < 50; i++) {
            HandRank rank = strategyManager.getTargetRank(context);
            // 保底道具应该触发保底牌型
            assertThat(rank).isEqualTo(HandRank.DOUDIZHU_BOMB);
        }
    }

    @Test
    @DisplayName("保底策略优先级最高 - 连败保底")
    void testGuaranteePriorityWithLoss() {
        // 10连败玩家，有活动加成
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .consecutiveLosses(10)
                .activeEvents(List.of(
                        createEvent("summer_event", 0.3)
                ))
                .build();

        int rocketCount = 0;
        int bombCount = 0;
        int totalAttempts = 100;

        for (int i = 0; i < totalAttempts; i++) {
            HandRank rank = strategyManager.getTargetRank(context);
            if (rank == HandRank.DOUDIZHU_ROCKET) {
                rocketCount++;
            } else if (rank == HandRank.DOUDIZHU_BOMB) {
                bombCount++;
            }
        }

        System.out.println("10连败触发王炸次数: " + rocketCount);
        System.out.println("10连败触发炸弹次数: " + bombCount);

        // 10连败应该触发王炸（顶级补偿）
        assertThat(rocketCount).isGreaterThan(0);
    }

    // ==================== 辅助方法 ====================

    private ActiveItem createItem(String itemId, double boostRate) {
        return ActiveItem.builder()
                .itemId(itemId)
                .name(itemId)
                .remainingGames(5)
                .effects(Map.of(
                        "type", "BOOST",
                        "boostRate", boostRate
                ))
                .build();
    }

    private ActiveItem createGuaranteeItem(String itemId, String targetRank) {
        return ActiveItem.builder()
                .itemId(itemId)
                .name(itemId)
                .remainingGames(1)
                .effects(Map.of(
                        "type", "GUARANTEE",
                        "guaranteeTarget", targetRank
                ))
                .build();
    }

    private ActiveActivity createEvent(String eventId, double boostRate) {
        return ActiveActivity.builder()
                .activityId(eventId)
                .eventName(eventId)
                .isActive(true)
                .startTime(System.currentTimeMillis() - 10000)
                .endTime(System.currentTimeMillis() + 100000)
                .boostRate(boostRate)
                .build();
    }
}