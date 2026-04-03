package com.pokergame.common.pattern;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.card.CardRank;
import com.pokergame.common.game.GameType;
import com.pokergame.common.util.CardUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 斗地主牌型识别器
 *
 * 牌型规则：
 * - 单张、对子、三张、三带一、三带二
 * - 顺子（5张起）、连对（3对起）、飞机（2连起）
 * - 炸弹（4张相同）、王炸（大小王）
 * - 四带二（四带两张单或两对）
 *
 * @author poker-platform
 */
@Slf4j
public class DoudizhuPatternRecognizer implements PatternRecognizer {

    private static final Set<CardPattern> SUPPORTED_PATTERNS = EnumSet.of(
            CardPattern.SINGLE, CardPattern.PAIR, CardPattern.THREE,
            CardPattern.THREE_WITH_SINGLE, CardPattern.THREE_WITH_PAIR,
            CardPattern.STRAIGHT, CardPattern.STRAIGHT_PAIR,
            CardPattern.PLANE, CardPattern.PLANE_WITH_SINGLE, CardPattern.PLANE_WITH_PAIR,
            CardPattern.FOUR_WITH_SINGLE, CardPattern.FOUR_WITH_PAIR,
            CardPattern.BOMB, CardPattern.ROCKET
    );

    @Override
    public PatternResult recognize(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new PatternResult(CardPattern.PASS, 0, Collections.emptyList());
        }

        int size = cards.size();
        Map<Integer, List<Card>> rankMap = CardUtils.groupByRank(cards);
        List<Integer> ranks = new ArrayList<>(rankMap.keySet());
        Collections.sort(ranks);

        // 1. 王炸（火箭）
        if (size == 2 && isRocket(cards)) {
            log.debug("识别为火箭");
            return new PatternResult(CardPattern.ROCKET, CardRank.JOKER_BIG.getValue(), cards);
        }

        // 2. 炸弹
        if (size == 4 && isBomb(rankMap)) {
            int mainRank = getMainRank(rankMap);
            log.debug("识别为炸弹，主牌值={}", mainRank);
            return new PatternResult(CardPattern.BOMB, mainRank, cards);
        }

        // 3. 单张
        if (size == 1) {
            int mainRank = cards.get(0).getRank().getValue();
            log.debug("识别为单张，主牌值={}", mainRank);
            return new PatternResult(CardPattern.SINGLE, mainRank, cards);
        }

        // 4. 对子
        if (size == 2 && isPair(rankMap)) {
            int mainRank = ranks.get(0);
            log.debug("识别为对子，主牌值={}", mainRank);
            return new PatternResult(CardPattern.PAIR, mainRank, cards);
        }

        // 5. 三张
        if (size == 3 && isTriplet(rankMap)) {
            int mainRank = ranks.get(0);
            log.debug("识别为三张，主牌值={}", mainRank);
            return new PatternResult(CardPattern.THREE, mainRank, cards);
        }

        // 6. 三带一
        if (size == 4 && isTripletWithSingle(rankMap)) {
            int mainRank = findMainRank(rankMap, 3);
            log.debug("识别为三带一，主牌值={}", mainRank);
            return new PatternResult(CardPattern.THREE_WITH_SINGLE, mainRank, cards);
        }

        // 7. 三带二
        if (size == 5 && isTripletWithPair(rankMap)) {
            int mainRank = findMainRank(rankMap, 3);
            log.debug("识别为三带二，主牌值={}", mainRank);
            return new PatternResult(CardPattern.THREE_WITH_PAIR, mainRank, cards);
        }

        // 8. 顺子
        if (size >= 5 && isStraight(ranks)) {
            log.debug("识别为顺子，起始牌值={}", ranks.get(0));
            return new PatternResult(CardPattern.STRAIGHT, ranks.get(0), cards, size);
        }

        // 9. 连对
        if (size >= 6 && size % 2 == 0 && isStraightPair(ranks, rankMap)) {
            log.debug("识别为连对，起始牌值={}", ranks.get(0));
            return new PatternResult(CardPattern.STRAIGHT_PAIR, ranks.get(0), cards, size / 2);
        }

        // 10. 飞机（不带）
        if (size >= 6 && size % 3 == 0 && isPlane(ranks, rankMap)) {
            log.debug("识别为飞机，起始牌值={}", ranks.get(0));
            return new PatternResult(CardPattern.PLANE, ranks.get(0), cards, size / 3);
        }

        // 11. 飞机带单
        PatternResult planeWithSingle = recognizePlaneWithWings(rankMap, cards, 1);
        if (planeWithSingle != null) return planeWithSingle;

        // 12. 飞机带对
        PatternResult planeWithPair = recognizePlaneWithWings(rankMap, cards, 2);
        if (planeWithPair != null) return planeWithPair;

        // 13. 四带二单
        if (size == 6 && isFourWithSingles(rankMap)) {
            int mainRank = findMainRank(rankMap, 4);
            log.debug("识别为四带二单，主牌值={}", mainRank);
            return new PatternResult(CardPattern.FOUR_WITH_SINGLE, mainRank, cards);
        }

        // 14. 四带二对
        if (size == 8 && isFourWithPairs(rankMap)) {
            int mainRank = findMainRank(rankMap, 4);
            log.debug("识别为四带二对，主牌值={}", mainRank);
            return new PatternResult(CardPattern.FOUR_WITH_PAIR, mainRank, cards);
        }

        log.debug("无法识别的牌型，牌数={}, 牌={}", size, CardUtils.toVisualString(cards));
        return new PatternResult(CardPattern.PASS, 0, cards);
    }

    @Override
    public boolean canBeat(PatternResult last, PatternResult current) {
        if (current == null || current.getPattern() == CardPattern.PASS) return false;
        if (last == null || last.getPattern() == CardPattern.PASS) return true;

        // 火箭最大
        if (current.getPattern() == CardPattern.ROCKET) return true;
        if (last.getPattern() == CardPattern.ROCKET) return false;

        // 炸弹可以压非炸弹
        if (current.getPattern() == CardPattern.BOMB) {
            if (last.getPattern() != CardPattern.BOMB) return true;
            return current.getMainRank() > last.getMainRank();
        }

        // 同牌型比较
        if (current.getPattern() == last.getPattern()) {
            // 比较主牌值
            if (current.getMainRank() != last.getMainRank()) {
                return current.getMainRank() > last.getMainRank();
            }
            // 比较副牌值（如顺子长度、连对数量）
            return current.getSubRank() > last.getSubRank();
        }

        return false;
    }

    @Override
    public Set<CardPattern> getSupportedPatterns() {
        return SUPPORTED_PATTERNS;
    }

    @Override
    public GameType getGameType() {
        return GameType.DOUDIZHU;
    }

    @Override
    public List<Card> sortCards(List<Card> cards) {
        return cards.stream()
                .sorted((a, b) -> {
                    // 先按牌值降序，再按花色降序
                    int rankCompare = Integer.compare(b.getRank().getValue(), a.getRank().getValue());
                    if (rankCompare != 0) return rankCompare;
                    return Integer.compare(b.getSuit().getValue(), a.getSuit().getValue());
                })
                .collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================

    private boolean isRocket(List<Card> cards) {
        return cards.size() == 2 &&
                cards.get(0).getRank() == CardRank.JOKER_SMALL &&
                cards.get(1).getRank() == CardRank.JOKER_BIG;
    }

    private boolean isBomb(Map<Integer, List<Card>> rankMap) {
        return rankMap.size() == 1 && rankMap.values().iterator().next().size() == 4;
    }

    private boolean isPair(Map<Integer, List<Card>> rankMap) {
        return rankMap.size() == 1 && rankMap.values().iterator().next().size() == 2;
    }

    private boolean isTriplet(Map<Integer, List<Card>> rankMap) {
        return rankMap.size() == 1 && rankMap.values().iterator().next().size() == 3;
    }

    private boolean isTripletWithSingle(Map<Integer, List<Card>> rankMap) {
        if (rankMap.size() != 2) return false;
        return rankMap.values().stream().anyMatch(l -> l.size() == 3) &&
                rankMap.values().stream().anyMatch(l -> l.size() == 1);
    }

    private boolean isTripletWithPair(Map<Integer, List<Card>> rankMap) {
        if (rankMap.size() != 2) return false;
        return rankMap.values().stream().anyMatch(l -> l.size() == 3) &&
                rankMap.values().stream().anyMatch(l -> l.size() == 2);
    }

    private boolean isStraight(List<Integer> ranks) {
        if (ranks.contains(15)) return false; // 2不能进顺子
        for (int i = 0; i < ranks.size() - 1; i++) {
            if (ranks.get(i + 1) - ranks.get(i) != 1) return false;
        }
        return true;
    }

    private boolean isStraightPair(List<Integer> ranks, Map<Integer, List<Card>> rankMap) {
        if (ranks.contains(15)) return false;
        for (Integer rank : ranks) {
            if (rankMap.get(rank).size() != 2) return false;
        }
        return isStraight(ranks);
    }

    private boolean isPlane(List<Integer> ranks, Map<Integer, List<Card>> rankMap) {
        if (ranks.contains(15)) return false;
        for (Integer rank : ranks) {
            if (rankMap.get(rank).size() != 3) return false;
        }
        return isStraight(ranks);
    }

    private PatternResult recognizePlaneWithWings(Map<Integer, List<Card>> rankMap,
                                                  List<Card> cards, int wingSize) {
        // 分离飞机主体和翅膀
        List<Integer> mainRanks = new ArrayList<>();
        List<Integer> wingRanks = new ArrayList<>();

        for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
            if (entry.getValue().size() == 3) {
                mainRanks.add(entry.getKey());
            } else if (entry.getValue().size() == wingSize) {
                wingRanks.add(entry.getKey());
            } else {
                return null;
            }
        }

        Collections.sort(mainRanks);
        int planeCount = mainRanks.size();

        if (planeCount < 2) return null;
        if (!isStraight(mainRanks)) return null;
        if (wingRanks.size() != planeCount) return null;

        CardPattern pattern = wingSize == 1 ? CardPattern.PLANE_WITH_SINGLE : CardPattern.PLANE_WITH_PAIR;
        log.debug("识别为{}，起始牌值={}", pattern.getName(), mainRanks.get(0));
        return new PatternResult(pattern, mainRanks.get(0), cards, planeCount);
    }

    private boolean isFourWithSingles(Map<Integer, List<Card>> rankMap) {
        if (rankMap.size() != 3) return false;
        int fourCount = 0, singleCount = 0;
        for (List<Card> list : rankMap.values()) {
            if (list.size() == 4) fourCount++;
            else if (list.size() == 1) singleCount++;
        }
        return fourCount == 1 && singleCount == 2;
    }

    private boolean isFourWithPairs(Map<Integer, List<Card>> rankMap) {
        if (rankMap.size() != 3) return false;
        int fourCount = 0, pairCount = 0;
        for (List<Card> list : rankMap.values()) {
            if (list.size() == 4) fourCount++;
            else if (list.size() == 2) pairCount++;
        }
        return fourCount == 1 && pairCount == 2;
    }

    private int getMainRank(Map<Integer, List<Card>> rankMap) {
        return rankMap.keySet().iterator().next();
    }

    private int findMainRank(Map<Integer, List<Card>> rankMap, int targetSize) {
        for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
            if (entry.getValue().size() == targetSize) {
                return entry.getKey();
            }
        }
        return 0;
    }
}
