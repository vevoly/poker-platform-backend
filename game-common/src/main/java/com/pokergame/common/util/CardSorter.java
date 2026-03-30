package com.pokergame.common.util;

import com.pokergame.common.card.Card;
import com.pokergame.common.game.GameType;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 手牌排序器 - 按不同游戏规则排序
 *
 * @author poker-platform
 */
public class CardSorter {

    /**
     * 斗地主排序规则：先按牌值降序，再按花色降序
     */
    public static List<Card> sortForDoudizhu(List<Card> cards) {
        return cards.stream()
                .sorted(Comparator
                        .comparingInt((Card c) -> getDoudizhuRankValue(c))
                        .reversed()
                        .thenComparingInt(c -> c.getSuit().getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 德州扑克排序规则：按牌值降序，A最大
     */
    public static List<Card> sortForTexas(List<Card> cards) {
        return cards.stream()
                .sorted(Comparator
                        .comparingInt((Card c) -> c.getRank().getValue())
                        .reversed())
                .collect(Collectors.toList());
    }

    /**
     * 牛牛排序规则：按牌值降序（10/J/Q/K 都算10点）
     */
    public static List<Card> sortForBull(List<Card> cards) {
        return cards.stream()
                .sorted(Comparator
                        .comparingInt((Card c) -> getBullPoint(c))
                        .reversed()
                        .thenComparingInt(c -> c.getRank().getValue()))
                .collect(Collectors.toList());
    }

    private static int getDoudizhuRankValue(Card card) {
        int rank = card.getRank().getValue();
        // 斗地主中 2 比 A 大
        if (rank == 14) return 15;  // A 转为 15
        if (rank == 15) return 14;  // 2 转为 14
        return rank;
    }

    private static int getBullPoint(Card card) {
        int rank = card.getRank().getValue();
        if (rank >= 10) return 10;
        if (rank == 14) return 1;  // A = 1点
        return rank;
    }
}
