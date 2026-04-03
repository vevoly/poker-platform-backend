package com.pokergame.common.pattern.texas;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.card.CardSuit;
import com.pokergame.common.game.GameType;
import com.pokergame.common.pattern.PatternRecognizer;
import com.pokergame.common.pattern.PatternResult;
import com.pokergame.common.util.CardUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 德州扑克牌型识别器
 *
 * 牌型规则（从高到低）：
 * 1. 皇家同花顺：同花色的 A-K-Q-J-10
 * 2. 同花顺：同花色的连续牌
 * 3. 四条：四张相同点数
 * 4. 葫芦：三条 + 一对
 * 5. 同花：五张同花色
 * 6. 顺子：五张连续牌
 * 7. 三条：三张相同点数
 * 8. 两对：两个不同点数的对子
 * 9. 一对：一个对子
 * 10. 高牌：无以上牌型
 *
 * @author poker-platform
 */
@Slf4j
public class TexasPatternRecognizer implements PatternRecognizer {

    private static final Set<CardPattern> SUPPORTED_PATTERNS = EnumSet.of(
            CardPattern.HIGH_CARD,
            CardPattern.ONE_PAIR,
            CardPattern.TWO_PAIR,
            CardPattern.THREE_OF_KIND,
            CardPattern.STRAIGHT_POKER,
            CardPattern.FLUSH,
            CardPattern.FULL_HOUSE,
            CardPattern.FOUR_OF_KIND,
            CardPattern.STRAIGHT_FLUSH,
            CardPattern.ROYAL_FLUSH
    );

    @Override
    public PatternResult recognize(List<Card> cards) {
        if (cards == null || cards.size() < 5) {
            return new PatternResult(CardPattern.PASS, 0, cards);
        }

        // 从7张牌中选出最好的5张组合
        List<List<Card>> combinations = CardUtils.combination(cards, 5);

        PatternResult best = null;
        for (List<Card> hand : combinations) {
            PatternResult result = evaluateFiveCards(hand);
            if (best == null || comparePatternResult(result, best) > 0) {
                best = result;
            }
        }

        return best;
    }

    @Override
    public boolean canBeat(PatternResult last, PatternResult current) {
        if (current == null || current.getPattern() == CardPattern.PASS) return false;
        if (last == null || last.getPattern() == CardPattern.PASS) return true;

        // 比较牌型等级
        if (current.getPattern().getCode() != last.getPattern().getCode()) {
            return current.getPattern().getCode() > last.getPattern().getCode();
        }

        // 同牌型比较主牌值
        if (current.getMainRank() != last.getMainRank()) {
            return current.getMainRank() > last.getMainRank();
        }

        // 比较副牌值（如两对中的第二对）
        return current.getSubRank() > last.getSubRank();
    }

    @Override
    public Set<CardPattern> getSupportedPatterns() {
        return SUPPORTED_PATTERNS;
    }

    @Override
    public GameType getGameType() {
        return GameType.TEXAS;
    }

    @Override
    public List<Card> sortCards(List<Card> cards) {
        return cards.stream()
                .sorted((a, b) -> {
                    int rankCompare = Integer.compare(b.getRank().getValue(), a.getRank().getValue());
                    if (rankCompare != 0) return rankCompare;
                    return Integer.compare(b.getSuit().getValue(), a.getSuit().getValue());
                })
                .collect(Collectors.toList());
    }

    /**
     * 评估5张牌的牌型
     */
    private PatternResult evaluateFiveCards(List<Card> hand) {
        Map<CardSuit, List<Card>> suitMap = CardUtils.groupBySuit(hand);
        Map<Integer, List<Card>> rankMap = CardUtils.groupByRank(hand);
        List<Integer> ranks = CardUtils.getSortedRanks(hand);

        boolean isFlush = suitMap.values().stream().anyMatch(list -> list.size() == 5);
        boolean isStraight = CardUtils.isStraight(ranks, true, true);

        // ========== 同花顺 ==========
        if (isFlush && isStraight) {
            int highRank = getStraightHighRank(ranks);
            // 皇家同花顺：10-J-Q-K-A
            if (highRank == 14 && ranks.contains(13) && ranks.contains(12) &&
                    ranks.contains(11) && ranks.contains(10)) {
                log.debug("识别为皇家同花顺");
                return new PatternResult(CardPattern.ROYAL_FLUSH, 14, hand);
            }
            log.debug("识别为同花顺，高牌={}", highRank);
            return new PatternResult(CardPattern.STRAIGHT_FLUSH, highRank, hand);
        }

        // ========== 四条 ==========
        for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
            if (entry.getValue().size() == 4) {
                int kicker = getKickerRank(rankMap, entry.getKey());
                log.debug("识别为四条，主牌={}, 踢脚={}", entry.getKey(), kicker);
                return new PatternResult(CardPattern.FOUR_OF_KIND, entry.getKey(), hand, kicker);
            }
        }

        // ========== 葫芦 ==========
        boolean hasThree = rankMap.values().stream().anyMatch(list -> list.size() == 3);
        boolean hasPair = rankMap.values().stream().anyMatch(list -> list.size() == 2);
        if (hasThree && hasPair) {
            int threeRank = findRankBySize(rankMap, 3);
            int pairRank = findRankBySize(rankMap, 2);
            log.debug("识别为葫芦，三条={}, 对子={}", threeRank, pairRank);
            return new PatternResult(CardPattern.FULL_HOUSE, threeRank, hand, pairRank);
        }

        // ========== 同花 ==========
        if (isFlush) {
            int highRank = ranks.get(ranks.size() - 1);
            log.debug("识别为同花，高牌={}", highRank);
            return new PatternResult(CardPattern.FLUSH, highRank, hand);
        }

        // ========== 顺子 ==========
        if (isStraight) {
            int highRank = getStraightHighRank(ranks);
            log.debug("识别为顺子，高牌={}", highRank);
            return new PatternResult(CardPattern.STRAIGHT_POKER, highRank, hand);
        }

        // ========== 三条 ==========
        if (hasThree) {
            int threeRank = findRankBySize(rankMap, 3);
            List<Integer> kickers = getKickerRanks(rankMap, threeRank);
            int kicker = kickers.isEmpty() ? 0 : kickers.get(0);
            log.debug("识别为三条，主牌={}", threeRank);
            return new PatternResult(CardPattern.THREE_OF_KIND, threeRank, hand, kicker);
        }

        // ========== 两对 ==========
        List<Integer> pairRanks = getPairRanks(rankMap);
        if (pairRanks.size() == 2) {
            // 降序排序，高对在前
            pairRanks.sort(Collections.reverseOrder());
            // 获取踢脚（不是对子的那张牌）
            Set<Integer> excludeRanks = new HashSet<>(pairRanks);
            int kicker = getKickerRank(rankMap, excludeRanks);
            log.debug("识别为两对，高对={}, 低对={}, 踢脚={}", pairRanks.get(0), pairRanks.get(1), kicker);
            return new PatternResult(CardPattern.TWO_PAIR, pairRanks.get(0), hand, pairRanks.get(1));
        }

        // ========== 一对 ==========
        if (pairRanks.size() == 1) {
            int pairRank = pairRanks.get(0);
            List<Integer> kickers = getKickerRanks(rankMap, pairRank);
            int kicker = kickers.isEmpty() ? 0 : kickers.get(0);
            log.debug("识别为一对，对子={}", pairRank);
            return new PatternResult(CardPattern.ONE_PAIR, pairRank, hand, kicker);
        }

        // ========== 高牌 ==========
        int highRank = ranks.get(ranks.size() - 1);
        log.debug("识别为高牌，高牌={}", highRank);
        return new PatternResult(CardPattern.HIGH_CARD, highRank, hand);
    }

    /**
     * 比较两个牌型结果（返回正数表示a大于b）
     */
    private int comparePatternResult(PatternResult a, PatternResult b) {
        if (a.getPattern().getCode() != b.getPattern().getCode()) {
            return a.getPattern().getCode() - b.getPattern().getCode();
        }
        if (a.getMainRank() != b.getMainRank()) {
            return a.getMainRank() - b.getMainRank();
        }
        return a.getSubRank() - b.getSubRank();
    }

    /**
     * 获取顺子的高牌值（处理A-2-3-4-5的特殊情况）
     */
    private int getStraightHighRank(List<Integer> ranks) {
        // 检查是否为 A-2-3-4-5 的特殊顺子
        if (ranks.contains(14) && ranks.contains(2) && ranks.contains(3) &&
                ranks.contains(4) && ranks.contains(5)) {
            return 5;  // 高牌是5
        }
        return ranks.get(ranks.size() - 1);
    }

    // ==================== 辅助方法 ====================

    /**
     * 根据牌值数量查找对应的牌值
     */
    private int findRankBySize(Map<Integer, List<Card>> rankMap, int targetSize) {
        for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
            if (entry.getValue().size() == targetSize) {
                return entry.getKey();
            }
        }
        return 0;
    }

    /**
     * 获取所有对子的牌值列表
     */
    private List<Integer> getPairRanks(Map<Integer, List<Card>> rankMap) {
        List<Integer> pairs = new ArrayList<>();
        for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
            if (entry.getValue().size() == 2) {
                pairs.add(entry.getKey());
            }
        }
        return pairs;
    }

    /**
     * 获取踢脚牌值（排除指定牌值）
     */
    private int getKickerRank(Map<Integer, List<Card>> rankMap, Set<Integer> excludeRanks) {
        for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
            if (!excludeRanks.contains(entry.getKey())) {
                return entry.getKey();
            }
        }
        return 0;
    }

    /**
     * 获取踢脚牌值（排除单个牌值）
     */
    private int getKickerRank(Map<Integer, List<Card>> rankMap, int excludeRank) {
        return getKickerRank(rankMap, Set.of(excludeRank));
    }

    /**
     * 获取踢脚牌值列表（排除指定牌值）
     */
    private List<Integer> getKickerRanks(Map<Integer, List<Card>> rankMap, int excludeRank) {
        List<Integer> kickers = new ArrayList<>();
        for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
            if (entry.getKey() != excludeRank) {
                // 每张牌值只记录一次
                kickers.add(entry.getKey());
            }
        }
        kickers.sort(Collections.reverseOrder());
        return kickers;
    }

    /**
     * 获取踢脚牌值列表（排除多个牌值）
     */
    private List<Integer> getKickerRanks(Map<Integer, List<Card>> rankMap, Set<Integer> excludeRanks) {
        List<Integer> kickers = new ArrayList<>();
        for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
            if (!excludeRanks.contains(entry.getKey())) {
                kickers.add(entry.getKey());
            }
        }
        kickers.sort(Collections.reverseOrder());
        return kickers;
    }
}
