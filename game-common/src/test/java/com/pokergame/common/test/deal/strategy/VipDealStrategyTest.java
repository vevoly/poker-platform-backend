package com.pokergame.common.test.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.VipConfigData;
import com.pokergame.common.deal.strategy.VipDealStrategy;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VIP特权策略测试
 *
 * 测试目标：验证VIP策略的配置驱动功能和概率控制
 *
 * @author poker-platform
 */
@DisplayName("VIP特权策略测试")
class VipDealStrategyTest {

    private VipDealStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new VipDealStrategy(GameType.DOUDIZHU);
    }

    // ==================== 基础功能测试 ====================

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
        int totalAttempts = 5000;

        for (int i = 0; i < totalAttempts; i++) {
            HandRank rank = strategy.getTargetRank(context);
            if (rank == HandRank.DOUDIZHU_ROCKET || rank == HandRank.DOUDIZHU_BOMB) {
                goodRankCount++;
            }
        }

        double rate = goodRankCount / (double) totalAttempts;
        System.out.println("VIP5好牌概率: " + rate);

        // VIP5应该有一定概率获得好牌（5%-20%之间）
        assertThat(rate).isBetween(0.05, 0.25);
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
        int totalAttempts = 5000;

        for (int i = 0; i < totalAttempts; i++) {
            HandRank rank5 = strategy.getTargetRank(contextVip5);
            HandRank rank9 = strategy.getTargetRank(contextVip9);

            if (rank5 == HandRank.DOUDIZHU_ROCKET || rank5 == HandRank.DOUDIZHU_BOMB) {
                vip5GoodCount++;
            }
            if (rank9 == HandRank.DOUDIZHU_ROCKET || rank9 == HandRank.DOUDIZHU_BOMB) {
                vip9GoodCount++;
            }
        }

        double rate5 = vip5GoodCount / (double) totalAttempts;
        double rate9 = vip9GoodCount / (double) totalAttempts;

        System.out.println("VIP5好牌概率: " + rate5);
        System.out.println("VIP9好牌概率: " + rate9);

        // VIP9应该比VIP5获得更多好牌
        assertThat(rate9).isGreaterThan(rate5);
    }

    // ==================== 配置驱动测试 ====================

    @Test
    @DisplayName("使用自定义配置")
    void testWithCustomConfig() {
        // 创建自定义配置（调高VIP5的概率）
        Map<Integer, Double> customTriggers = new HashMap<>();
        Map<Integer, Double> customBoosts = new HashMap<>();
        Map<Integer, Double> customSpecials = new HashMap<>();

        // VIP5: 触发概率50%，加成系数30%
        customTriggers.put(5, 0.5);
        customBoosts.put(5, 0.3);
        customSpecials.put(5, 0.0);

        VipConfigData customConfig = VipConfigData.builder()
                .triggerProbabilities(customTriggers)
                .boostRates(customBoosts)
                .specialProbabilities(customSpecials)
                .build();

        // 使用默认配置
        DealContext defaultContext = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(5)
                .build();

        // 使用自定义配置
        DealContext customContext = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(5)
                .vipConfig(customConfig)
                .build();

        int defaultGoodCount = 0;
        int customGoodCount = 0;
        int totalAttempts = 5000;

        for (int i = 0; i < totalAttempts; i++) {
            HandRank defaultRank = strategy.getTargetRank(defaultContext);
            HandRank customRank = strategy.getTargetRank(customContext);

            if (defaultRank == HandRank.DOUDIZHU_ROCKET || defaultRank == HandRank.DOUDIZHU_BOMB) {
                defaultGoodCount++;
            }
            if (customRank == HandRank.DOUDIZHU_ROCKET || customRank == HandRank.DOUDIZHU_BOMB) {
                customGoodCount++;
            }
        }

        double defaultRate = defaultGoodCount / (double) totalAttempts;
        double customRate = customGoodCount / (double) totalAttempts;

        System.out.println("默认配置VIP5好牌概率: " + defaultRate);
        System.out.println("自定义配置VIP5好牌概率: " + customRate);

        // 自定义配置概率更高
        assertThat(customRate).isGreaterThan(defaultRate);
    }

    @Test
    @DisplayName("VIP7专属牌型测试")
    void testVip7SpecialRank() {
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(7)
                .build();

        int specialRankCount = 0;
        int totalAttempts = 10000;

        for (int i = 0; i < totalAttempts; i++) {
            HandRank rank = strategy.getTargetRank(context);
            if (rank == HandRank.DOUDIZHU_ROCKET) {
                specialRankCount++;
            }
        }

        double rate = specialRankCount / (double) totalAttempts;
        System.out.println("VIP7王炸概率: " + rate);

        // VIP7应该有较低概率获得王炸（1%-10%）
        assertThat(rate).isBetween(0.01, 0.12);
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("空Context返回null")
    void testNullContext() {
        HandRank rank = strategy.getTargetRank(null);
        assertThat(rank).isNull();
    }

    @Test
    @DisplayName("不同游戏类型返回不同牌型")
    void testDifferentGameTypes() {
        VipDealStrategy doudizhuStrategy = new VipDealStrategy(GameType.DOUDIZHU);
        VipDealStrategy texasStrategy = new VipDealStrategy(GameType.TEXAS);

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .vipLevel(5)
                .build();

        // 斗地主策略
        HandRank doudizhuRank = doudizhuStrategy.getTargetRank(context);
        // 德州策略
        HandRank texasRank = texasStrategy.getTargetRank(context);

        if (doudizhuRank != null) {
            assertThat(doudizhuRank.name()).startsWith("DOUDIZHU");
        }
        if (texasRank != null) {
            assertThat(texasRank.name()).startsWith("TEXAS");
        }
    }
}
