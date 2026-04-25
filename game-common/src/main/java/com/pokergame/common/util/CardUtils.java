package com.pokergame.common.util;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardSuit;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 牌通用工具类
 */
@Slf4j
public class CardUtils {

    /**
     * 按牌值分组
     */
    public static Map<Integer, List<Card>> groupByRank(List<Card> cards) {
        return cards.stream()
                .filter(Objects::nonNull)  // 过滤掉 null
                .collect(Collectors.groupingBy(c -> c.getRank().getValue()));
    }

    /**
     * 按花色分组
     */
    public static Map<CardSuit, List<Card>> groupBySuit(List<Card> cards) {
        return cards.stream()
                .filter(Objects::nonNull)  // 过滤掉 null
                .collect(Collectors.groupingBy(Card::getSuit));
    }

    /**
     * 获取所有牌值（去重排序）
     */
    public static List<Integer> getSortedRanks(List<Card> cards) {
        return cards.stream()
                .map(c -> c.getRank().getValue())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 获取牌值频率统计
     */
    public static Map<Integer, Long> getRankFrequency(List<Card> cards) {
        return cards.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getRank().getValue(),
                        Collectors.counting()
                ));
    }

    /**
     * 排序牌（按牌值降序）
     */
    public static List<Card> sortByRankDesc(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return Collections.emptyList();
        }
        return cards.stream()
                .sorted((a, b) -> Integer.compare(b.getRank().getValue(), a.getRank().getValue()))
                .collect(Collectors.toList());
    }

    public static boolean isStraight(List<Integer> ranks, boolean includeAceAsLow) {
        return isStraight(ranks, includeAceAsLow, false);
    }

    /**
     * 判断是否为顺子
     * @param ranks 牌值列表
     * @param includeAceAsLow 是否将A视为1
     * @param isTexas 是否为德州扑克（2视为最小）
     */
    public static boolean isStraight(List<Integer> ranks, boolean includeAceAsLow, boolean isTexas) {
        List<Integer> sorted = ranks.stream()
                .map(r -> {
                    if (isTexas && r == 15) return 2;  // 德州：2映射为2
                    return r;
                })
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 处理 A 作为低牌的情况 (A-2-3-4-5)
        if (includeAceAsLow && sorted.contains(14) && sorted.contains(2) &&
                sorted.contains(3) && sorted.contains(4) && sorted.contains(5)) {
            return true;
        }

        // 普通顺子检查
        if (sorted.size() < 5) return false;

        for (int i = 0; i <= sorted.size() - 5; i++) {
            boolean straight = true;
            for (int j = i; j < i + 4; j++) {
                if (sorted.get(j + 1) - sorted.get(j) != 1) {
                    straight = false;
                    break;
                }
            }
            if (straight) return true;
        }
        return false;
    }

    /**
     * 获取组合（C(n, k)）
     */
    public static <T> List<List<T>> combination(List<T> list, int k) {
        List<List<T>> result = new ArrayList<>();
        combinationHelper(list, k, 0, new ArrayList<>(), result);
        return result;
    }

    private static <T> void combinationHelper(List<T> list, int k, int start,
                                              List<T> current, List<List<T>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            current.add(list.get(i));
            combinationHelper(list, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    /**
     * 获取牌的可视化字符串
     */
    public static String toVisualString(List<Card> cards) {
        return cards.stream()
                .map(Card::toString)
                .collect(Collectors.joining(" "));
    }

    /**
     * 获取牌值列表字符串
     */
    public static String toRankString(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return "[]";
        return cards.stream()
                .map(c -> String.valueOf(c.getRank().getValue()))
                .collect(Collectors.joining(","));
    }

}
