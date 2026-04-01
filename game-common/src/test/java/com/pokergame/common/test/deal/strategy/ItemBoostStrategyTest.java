package com.pokergame.common.test.deal.strategy;

import com.pokergame.common.item.ActiveItem;
import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.strategy.ItemBoostStrategy;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 道具加成策略测试
 */
@DisplayName("道具加成策略测试")
class ItemBoostStrategyTest {

    private final ItemBoostStrategy strategy = new ItemBoostStrategy(GameType.DOUDIZHU);

    @Test
    @DisplayName("无道具返回null")
    void testNoItemReturnsNull() {
        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .activeItems(List.of())
                .build();

        HandRank rank = strategy.getTargetRank(context);

        assertThat(rank).isNull();
    }

    @Test
    @DisplayName("好运卡道具提高好牌概率")
    void testLuckyCardBoost() {
        ActiveItem luckyCard = ActiveItem.builder()
                .itemId("lucky_card")
                .name("好运卡")
                .remainingGames(5)
                .effects(Map.of("boostRate", 0.2))
                .build();

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .activeItems(List.of(luckyCard))
                .build();

        int goodRankCount = 0;

        for (int i = 0; i < 100; i++) {
            HandRank rank = strategy.getTargetRank(context);
            if (rank == HandRank.DOUDIZHU_ROCKET || rank == HandRank.DOUDIZHU_BOMB) {
                goodRankCount++;
            }
        }

        // 有好运卡应该有一定概率获得好牌
        assertThat(goodRankCount).isGreaterThan(5);
    }

    @Test
    @DisplayName("保底道具直接返回保底牌型")
    void testGuaranteeCardReturnsGuaranteeRank() {
        ActiveItem guaranteeCard = ActiveItem.builder()
                .itemId("guarantee_card")
                .name("保底卡")
                .remainingGames(1)
                .effects(Map.of("guaranteeTarget", "BOMB"))
                .build();

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .activeItems(List.of(guaranteeCard))
                .build();

        HandRank rank = strategy.getTargetRank(context);

        assertThat(rank).isEqualTo(HandRank.DOUDIZHU_BOMB);
    }

    @Test
    @DisplayName("多个道具加成叠加")
    void testMultipleItemsBoostStack() {
        ActiveItem luckyCard = ActiveItem.builder()
                .itemId("lucky_card")
                .name("好运卡")
                .remainingGames(5)
                .effects(Map.of("boostRate", 0.2))
                .build();

        ActiveItem doubleCard = ActiveItem.builder()
                .itemId("double_card")
                .name("双倍卡")
                .remainingGames(3)
                .effects(Map.of("boostRate", 0.3))
                .build();

        DealContext context = DealContext.builder()
                .playerId(1001L)
                .gameType(GameType.DOUDIZHU)
                .activeItems(List.of(luckyCard, doubleCard))
                .build();

        // 计算总加成
        double totalBoost = context.getTotalItemBoost();
        assertThat(totalBoost).isEqualTo(0.5);

        // 多次测试，应该能触发加成
        int triggerCount = 0;
        for (int i = 0; i < 50; i++) {
            if (strategy.getTargetRank(context) != null) {
                triggerCount++;
            }
        }

        assertThat(triggerCount).isGreaterThan(0);
    }
}
