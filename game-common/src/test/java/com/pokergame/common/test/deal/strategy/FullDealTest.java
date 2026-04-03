package com.pokergame.common.test.deal.strategy;

import com.pokergame.common.card.Card;
import com.pokergame.common.deal.*;
import com.pokergame.common.deal.dealer.*;
import com.pokergame.common.deal.dealer.impl.BullDealer;
import com.pokergame.common.deal.dealer.impl.DoudizhuDealer;
import com.pokergame.common.deal.dealer.impl.TexasDealer;
import com.pokergame.common.event.ActiveEvent;
import com.pokergame.common.item.ActiveItem;
import com.pokergame.common.game.GameType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 完整发牌测试
 *
 * 测试目标：验证一局游戏中所有玩家的发牌情况
 *
 * @author poker-platform
 */
@DisplayName("完整发牌测试")
public class FullDealTest {

    // ==================== 斗地主发牌测试 ====================

    @Test
    @DisplayName("斗地主 - 3人局发牌测试")
    void testDoudizhu3PlayersDeal() {
        System.out.println("\n========== 斗地主3人局发牌测试 ==========");

        // 创建发牌器
        DoudizhuDealer dealer = new DoudizhuDealer(3);

        // 构建玩家列表
        List<Long> playerIds = List.of(1001L, 1002L, 1003L);

        // 构建多玩家上下文
        MultiPlayerDealContext context = buildDoudizhuContext(playerIds);

        // 执行发牌
        List<List<Card>> hands = dealer.deal(context);

        // 验证结果
        validateDoudizhuDeal(hands, playerIds);

        // 打印手牌
        printHands("斗地主3人局", playerIds, hands);
    }

    @Test
    @DisplayName("斗地主 - 2人局基础发牌测试（无策略）")
    void testDoudizhu2PlayersBasicDeal() {
        System.out.println("\n========== 斗地主2人局基础发牌测试 ==========");

        DoudizhuDealer dealer = new DoudizhuDealer(2);
        List<Long> playerIds = List.of(1001L, 1002L);

        // 使用无策略的上下文（所有加成关闭）
        MultiPlayerDealContext context = MultiPlayerDealContext.builder()
                .gameType(GameType.DOUDIZHU)
                .playerCount(2)
                .playerIds(playerIds)
                .landlordIndex(0)
                .vipLevels(List.of(0, 0))
                .consecutiveLosses(List.of(0, 0))
                .consecutiveWins(List.of(0, 0))
                .rookieFlags(List.of(false, false))
                .aiFlags(List.of(false, false))
                .activeItemsList(List.of(List.of(), List.of()))
                .activeEventsList(List.of(List.of(), List.of()))
                .build();

        List<List<Card>> hands = dealer.deal(context);

        validateDoudizhuDeal(hands, playerIds);
        printHands("斗地主2人局", playerIds, hands);
    }

    @Test
    @DisplayName("斗地主 - 2人局发牌测试")
    void testDoudizhu2PlayersDeal() {
        System.out.println("\n========== 斗地主2人局发牌测试 ==========");

        DoudizhuDealer dealer = new DoudizhuDealer(2);
        List<Long> playerIds = List.of(1001L, 1002L);
        MultiPlayerDealContext context = buildDoudizhuContext(playerIds);

        List<List<Card>> hands = dealer.deal(context);

        validateDoudizhuDeal(hands, playerIds);
        printHands("斗地主2人局", playerIds, hands);
    }

    @Test
    @DisplayName("斗地主 - 带策略发牌测试")
    void testDoudizhuWithStrategies() {
        System.out.println("\n========== 斗地主带策略发牌测试 ==========");

        DoudizhuDealer dealer = new DoudizhuDealer(3);
        List<Long> playerIds = List.of(1001L, 1002L, 1003L);

        // 构建带策略的上下文
        MultiPlayerDealContext context = buildDoudizhuContextWithStrategies(playerIds);

        List<List<Card>> hands = dealer.deal(context);

//        validateDoudizhuDeal(hands, playerIds);
        printHands("斗地主带策略", playerIds, hands);

        // 打印策略触发情况
        printStrategyInfo(context);
    }

    // ==================== 牛牛发牌测试 ====================

    @Test
    @DisplayName("牛牛 - 4人局发牌测试")
    void testBull4PlayersDeal() {
        System.out.println("\n========== 牛牛4人局发牌测试 ==========");

        BullDealer dealer = new BullDealer(4);
        List<Long> playerIds = List.of(2001L, 2002L, 2003L, 2004L);
        MultiPlayerDealContext context = buildBullContext(playerIds);

        List<List<Card>> hands = dealer.deal(context);

        validateBullDeal(hands, playerIds);
        printHands("牛牛4人局", playerIds, hands);
    }

    @Test
    @DisplayName("牛牛 - 6人局发牌测试")
    void testBull6PlayersDeal() {
        System.out.println("\n========== 牛牛6人局发牌测试 ==========");

        BullDealer dealer = new BullDealer(6);
        List<Long> playerIds = List.of(2001L, 2002L, 2003L, 2004L, 2005L, 2006L);
        MultiPlayerDealContext context = buildBullContext(playerIds);

        List<List<Card>> hands = dealer.deal(context);

        validateBullDeal(hands, playerIds);
        printHands("牛牛6人局", playerIds, hands);
    }

    // ==================== 德州扑克发牌测试 ====================

    @Test
    @DisplayName("德州扑克 - 6人局发牌测试")
    void testTexas6PlayersDeal() {
        System.out.println("\n========== 德州扑克6人局发牌测试 ==========");

        TexasDealer dealer = new TexasDealer(6);
        List<Long> playerIds = List.of(3001L, 3002L, 3003L, 3004L, 3005L, 3006L);
        MultiPlayerDealContext context = buildTexasContext(playerIds);

        List<List<Card>> hands = dealer.deal(context);

        validateTexasDeal(hands, playerIds);
        printHands("德州扑克6人局", playerIds, hands);
    }

    @Test
    @DisplayName("德州扑克 - 2人局发牌测试")
    void testTexas2PlayersDeal() {
        System.out.println("\n========== 德州扑克2人局发牌测试 ==========");

        TexasDealer dealer = new TexasDealer(2);
        List<Long> playerIds = List.of(3001L, 3002L);
        MultiPlayerDealContext context = buildTexasContext(playerIds);

        List<List<Card>> hands = dealer.deal(context);

        validateTexasDeal(hands, playerIds);
        printHands("德州扑克2人局", playerIds, hands);
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("斗地主 - 非法人数应抛异常")
    void testDoudizhuInvalidPlayerCount() {
        System.out.println("\n========== 斗地主非法人数测试 ==========");

        try {
            DoudizhuDealer dealer = new DoudizhuDealer(4);
            // 不应该执行到这里
            assertThat(true).isFalse();
        } catch (IllegalArgumentException e) {
            System.out.println("正确抛出异常: " + e.getMessage());
            assertThat(e.getMessage()).contains("斗地主");
        }
    }

    @Test
    @DisplayName("牛牛 - 非法人数应抛异常")
    void testBullInvalidPlayerCount() {
        System.out.println("\n========== 牛牛非法人数测试 ==========");

        try {
            BullDealer dealer = new BullDealer(7);
            assertThat(true).isFalse();
        } catch (IllegalArgumentException e) {
            System.out.println("正确抛出异常: " + e.getMessage());
            assertThat(e.getMessage()).contains("牛牛");
        }
    }

    @Test
    @DisplayName("德州扑克 - 非法人数应抛异常")
    void testTexasInvalidPlayerCount() {
        System.out.println("\n========== 德州扑克非法人数测试 ==========");

        try {
            TexasDealer dealer = new TexasDealer(10);
            assertThat(true).isFalse();
        } catch (IllegalArgumentException e) {
            System.out.println("正确抛出异常: " + e.getMessage());
            assertThat(e.getMessage()).contains("德州扑克");
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建斗地主上下文
     */
    private MultiPlayerDealContext buildDoudizhuContext(List<Long> playerIds) {
        return MultiPlayerDealContext.builder()
                .gameType(GameType.DOUDIZHU)
                .playerCount(playerIds.size())
                .playerIds(playerIds)
                .landlordIndex(0)  // 第一个玩家是地主
                .vipLevels(List.of(5, 0, 0))
                .consecutiveLosses(List.of(0, 3, 0))
                .consecutiveWins(List.of(0, 0, 2))
                .rookieFlags(List.of(false, true, false))
                .aiFlags(List.of(false, false, true))
                .aiDifficulties(List.of(0, 0, 5))
                .build();
    }

    /**
     * 构建带策略的斗地主上下文
     */
    private MultiPlayerDealContext buildDoudizhuContextWithStrategies(List<Long> playerIds) {
        // 创建道具
        ActiveItem luckyCard = ActiveItem.builder()
                .itemId("lucky_card")
                .name("好运卡")
                .remainingGames(5)
                .effects(Map.of("type", "BOOST", "boostRate", 0.2))
                .build();

        ActiveItem guaranteeCard = ActiveItem.builder()
                .itemId("guarantee_card")
                .name("保底卡")
                .remainingGames(1)
                .effects(Map.of("type", "GUARANTEE", "guaranteeTarget", "BOMB"))
                .build();

        // 创建活动
        ActiveEvent springEvent = ActiveEvent.builder()
                .eventId("spring_festival")
                .eventName("春节活动")
                .isActive(true)
                .startTime(System.currentTimeMillis() - 10000)
                .endTime(System.currentTimeMillis() + 100000)
                .boostRate(0.2)
                .build();

        return MultiPlayerDealContext.builder()
                .gameType(GameType.DOUDIZHU)
                .playerCount(playerIds.size())
                .playerIds(playerIds)
                .landlordIndex(1)  // 第二个玩家是地主
                .vipLevels(List.of(3, 7, 0))
                .consecutiveLosses(List.of(0, 0, 5))
                .consecutiveWins(List.of(2, 0, 0))
                .rookieFlags(List.of(false, false, true))
                .aiFlags(List.of(false, false, true))
                .aiDifficulties(List.of(0, 0, 3))
                .activeItemsList(List.of(
                        List.of(luckyCard),      // 玩家1有好运卡
                        List.of(guaranteeCard),  // 玩家2有保底卡
                        List.of()                // 玩家3无道具
                ))
                .activeEventsList(List.of(
                        List.of(springEvent),    // 玩家1有活动
                        List.of(),               // 玩家2无活动
                        List.of(springEvent)     // 玩家3有活动
                ))
                .build();
    }

    /**
     * 构建牛牛上下文
     */
    private MultiPlayerDealContext buildBullContext(List<Long> playerIds) {
        return MultiPlayerDealContext.builder()
                .gameType(GameType.BULL)
                .playerCount(playerIds.size())
                .playerIds(playerIds)
                .vipLevels(playerIds.stream().map(id -> 0).collect(Collectors.toList()))
                .consecutiveLosses(playerIds.stream().map(id -> 0).collect(Collectors.toList()))
                .consecutiveWins(playerIds.stream().map(id -> 0).collect(Collectors.toList()))
                .rookieFlags(playerIds.stream().map(id -> false).collect(Collectors.toList()))
                .aiFlags(playerIds.stream().map(id -> false).collect(Collectors.toList()))
                .build();
    }

    /**
     * 构建德州扑克上下文
     */
    private MultiPlayerDealContext buildTexasContext(List<Long> playerIds) {
        return MultiPlayerDealContext.builder()
                .gameType(GameType.TEXAS)
                .playerCount(playerIds.size())
                .playerIds(playerIds)
                .vipLevels(List.of(5, 0, 0, 0, 0, 0))
                .consecutiveLosses(List.of(0, 0, 0, 0, 0, 0))
                .consecutiveWins(List.of(0, 0, 0, 0, 0, 0))
                .rookieFlags(List.of(false, true, false, false, false, false))
                .aiFlags(List.of(false, false, true, false, false, false))
                .aiDifficulties(List.of(0, 0, 7, 0, 0, 0))
                .build();
    }

    /**
     * 验证斗地主发牌结果
     */
    private void validateDoudizhuDeal(List<List<Card>> hands, List<Long> playerIds) {
        // 验证玩家数量
        assertThat(hands).hasSize(playerIds.size());

        // 验证总牌数（3人局：17+17+20=54，2人局：17+20=37？实际2人斗地主特殊处理）
        int totalCards = hands.stream().mapToInt(List::size).sum();
        if (playerIds.size() == 3) {
            assertThat(totalCards).isEqualTo(54);
        }

        // 验证牌不重复
        Set<Integer> allCardIds = new HashSet<>();
        for (List<Card> hand : hands) {
            for (Card card : hand) {
                assertThat(allCardIds.contains(card.getId())).isFalse();
                allCardIds.add(card.getId());
            }
        }

        // 验证地主牌数
        int landlordIndex = 0;  // 假设第一个玩家是地主
        assertThat(hands.get(landlordIndex)).hasSize(20);
        for (int i = 0; i < hands.size(); i++) {
            if (i != landlordIndex) {
                assertThat(hands.get(i)).hasSize(17);
            }
        }

        System.out.println("斗地主发牌验证通过: 总牌数=" + totalCards + ", 不重复牌=" + allCardIds.size());
    }

    /**
     * 验证牛牛发牌结果
     */
    private void validateBullDeal(List<List<Card>> hands, List<Long> playerIds) {
        assertThat(hands).hasSize(playerIds.size());

        // 牛牛每人5张牌
        for (List<Card> hand : hands) {
            assertThat(hand).hasSize(5);
        }

        // 验证牌不重复
        Set<Integer> allCardIds = new HashSet<>();
        for (List<Card> hand : hands) {
            for (Card card : hand) {
                assertThat(allCardIds.contains(card.getId())).isFalse();
                allCardIds.add(card.getId());
            }
        }

        int totalCards = hands.size() * 5;
        assertThat(allCardIds).hasSize(totalCards);

        System.out.println("牛牛发牌验证通过: 玩家数=" + playerIds.size() + ", 总牌数=" + totalCards);
    }

    /**
     * 验证德州扑克发牌结果
     */
    private void validateTexasDeal(List<List<Card>> hands, List<Long> playerIds) {
        assertThat(hands).hasSize(playerIds.size());

        // 德州每人2张手牌
        for (List<Card> hand : hands) {
            assertThat(hand).hasSize(2);
        }

        // 验证牌不重复
        Set<Integer> allCardIds = new HashSet<>();
        for (List<Card> hand : hands) {
            for (Card card : hand) {
                assertThat(allCardIds.contains(card.getId())).isFalse();
                allCardIds.add(card.getId());
            }
        }

        int totalCards = hands.size() * 2;
        assertThat(allCardIds).hasSize(totalCards);

        System.out.println("德州扑克发牌验证通过: 玩家数=" + playerIds.size() + ", 手牌数=" + totalCards);
    }

    /**
     * 打印手牌
     */
    private void printHands(String title, List<Long> playerIds, List<List<Card>> hands) {
        System.out.println("\n--- " + title + " ---");
        for (int i = 0; i < playerIds.size(); i++) {
            String cardsStr = hands.get(i).stream()
                    .map(Card::toString)
                    .collect(Collectors.joining(" "));
            System.out.printf("玩家%d (%d): %s (%d张)%n",
                    i + 1, playerIds.get(i), cardsStr, hands.get(i).size());
        }
    }

    /**
     * 打印策略信息
     */
    private void printStrategyInfo(MultiPlayerDealContext context) {
        System.out.println("\n--- 策略信息 ---");
        System.out.println("VIP等级: " + context.getVipLevels());
        System.out.println("连败次数: " + context.getConsecutiveLosses());
        System.out.println("连胜次数: " + context.getConsecutiveWins());
        System.out.println("新手标志: " + context.getRookieFlags());
        System.out.println("AI标志: " + context.getAiFlags());

        if (context.getActiveItemsList() != null) {
            System.out.println("道具列表: " + context.getActiveItemsList());
        }
        if (context.getActiveEventsList() != null) {
            System.out.println("活动列表: " + context.getActiveEventsList());
        }
    }
}
