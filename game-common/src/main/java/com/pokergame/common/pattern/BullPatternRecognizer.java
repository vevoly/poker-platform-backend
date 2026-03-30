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
 * 牛牛牌型识别器
 *
 * 牌型规则（从高到低）：
 * 1. 五小牛：5张牌点数总和 < 10
 * 2. 炸弹：4张相同点数的牌
 * 3. 五花牛：5张牌全是花牌（J、Q、K）
 * 4. 四花牛：4张花牌 + 1张10点
 * 5. 牛牛：3张牌之和为10的倍数，另2张之和也为10的倍数
 * 6. 牛X：3张牌之和为10的倍数，另2张之和除以10的余数为X
 * 7. 无牛：无法组成3张牌和为10的倍数
 *
 * @author poker-platform
 */
@Slf4j
public class BullPatternRecognizer implements PatternRecognizer {

    private static final Set<CardPattern> SUPPORTED_PATTERNS = EnumSet.of(
            CardPattern.NO_BULL, CardPattern.BULL_1, CardPattern.BULL_2,
            CardPattern.BULL_3, CardPattern.BULL_4, CardPattern.BULL_5,
            CardPattern.BULL_6, CardPattern.BULL_7, CardPattern.BULL_8,
            CardPattern.BULL_9, CardPattern.BULL_BULL,
            CardPattern.FOUR_BOMB, CardPattern.FIVE_SMALL
    );

    @Override
    public PatternResult recognize(List<Card> cards) {
        if (cards == null || cards.size() != 5) {
            return new PatternResult(CardPattern.PASS, 0, cards);
        }

        List<Integer> points = cards.stream()
                .map(this::getCardPoint)
                .collect(Collectors.toList());

        // 1. 五小牛
        if (isFiveSmall(points)) {
            log.debug("识别为五小牛");
            return new PatternResult(CardPattern.FIVE_SMALL, 10, cards);
        }

        // 2. 炸弹
        if (isFourBomb(cards)) {
            log.debug("识别为炸弹");
            return new PatternResult(CardPattern.FOUR_BOMB, 10, cards);
        }

        // 3. 五花牛 / 四花牛
        int flowerCount = (int) cards.stream()
                .filter(c -> isFlowerCard(c))
                .count();
        if (flowerCount == 5) {
            log.debug("识别为五花牛");
            return new PatternResult(CardPattern.BULL_BULL, 10, cards);
        }
        if (flowerCount == 4 && hasTenPoint(cards)) {
            log.debug("识别为四花牛");
            return new PatternResult(CardPattern.BULL_BULL, 9, cards);
        }

        // 4. 查找牛的组合
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                for (int k = j + 1; k < 5; k++) {
                    int sum3 = points.get(i) + points.get(j) + points.get(k);
                    if (sum3 % 10 == 0) {
                        // 找到牛，计算牛几
                        List<Integer> remaining = new ArrayList<>();
                        for (int m = 0; m < 5; m++) {
                            if (m != i && m != j && m != k) {
                                remaining.add(points.get(m));
                            }
                        }
                        int bullValue = (remaining.get(0) + remaining.get(1)) % 10;

                        CardPattern pattern = getBullPattern(bullValue);
                        log.debug("识别为{}，牛值={}", pattern.getName(), bullValue);
                        return new PatternResult(pattern, bullValue == 0 ? 10 : bullValue, cards);
                    }
                }
            }
        }

        log.debug("识别为无牛");
        return new PatternResult(CardPattern.NO_BULL, 0, cards);
    }

    @Override
    public boolean canBeat(PatternResult last, PatternResult current) {
        if (current == null || current.getPattern() == CardPattern.PASS) return false;
        if (last == null || last.getPattern() == CardPattern.PASS) return true;

        // 按牌型优先级比较
        int currentPriority = getPatternPriority(current.getPattern());
        int lastPriority = getPatternPriority(last.getPattern());

        if (currentPriority != lastPriority) {
            return currentPriority > lastPriority;
        }

        // 同牌型比较牛值
        return current.getMainRank() > last.getMainRank();
    }

    @Override
    public Set<CardPattern> getSupportedPatterns() {
        return SUPPORTED_PATTERNS;
    }

    @Override
    public GameType getGameType() {
        return GameType.BULL;
    }

    @Override
    public List<Card> sortCards(List<Card> cards) {
        return cards.stream()
                .sorted((a, b) -> {
                    int pointA = getCardPoint(a);
                    int pointB = getCardPoint(b);
                    if (pointA != pointB) return Integer.compare(pointB, pointA);
                    return Integer.compare(b.getRank().getValue(), a.getRank().getValue());
                })
                .collect(Collectors.toList());
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取牌的点数（牛牛计点规则）
     * A=1, 2-9=对应点数, 10/J/Q/K=10
     */
    private int getCardPoint(Card card) {
        CardRank rank = card.getRank();
        int value = rank.getValue();
        if (value >= 10) return 10;
        if (value >= 2 && value <= 9) return value;
        if (value == 14) return 1; // A
        return value;
    }

    private boolean isFlowerCard(Card card) {
        int value = card.getRank().getValue();
        return value == 11 || value == 12 || value == 13; // J、Q、K
    }

    private boolean hasTenPoint(List<Card> cards) {
        return cards.stream().anyMatch(c -> getCardPoint(c) == 10);
    }

    private boolean isFiveSmall(List<Integer> points) {
        return points.stream().mapToInt(Integer::intValue).sum() < 10;
    }

    private boolean isFourBomb(List<Card> cards) {
        Map<Integer, List<Card>> rankMap = CardUtils.groupByRank(cards);
        return rankMap.values().stream().anyMatch(list -> list.size() == 4);
    }

    private CardPattern getBullPattern(int bullValue) {
        switch (bullValue) {
            case 0: return CardPattern.BULL_BULL;
            case 1: return CardPattern.BULL_1;
            case 2: return CardPattern.BULL_2;
            case 3: return CardPattern.BULL_3;
            case 4: return CardPattern.BULL_4;
            case 5: return CardPattern.BULL_5;
            case 6: return CardPattern.BULL_6;
            case 7: return CardPattern.BULL_7;
            case 8: return CardPattern.BULL_8;
            case 9: return CardPattern.BULL_9;
            default: return CardPattern.NO_BULL;
        }
    }

    private int getPatternPriority(CardPattern pattern) {
        switch (pattern) {
            case FIVE_SMALL: return 10;
            case FOUR_BOMB: return 9;
            case BULL_BULL: return 8;
            case BULL_9: return 7;
            case BULL_8: return 6;
            case BULL_7: return 5;
            case BULL_6: return 4;
            case BULL_5: return 3;
            case BULL_4: return 2;
            case BULL_3: return 1;
            case BULL_2: return 0;
            case BULL_1: return -1;
            default: return -2;
        }
    }
}
