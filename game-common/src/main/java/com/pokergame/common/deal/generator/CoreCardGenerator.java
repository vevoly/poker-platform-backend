package com.pokergame.common.deal.generator;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardDeck;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 核心牌生成器
 *
 * 职责：根据目标牌型生成"核心牌"
 * - 只负责生成牌型的核心部分（如炸弹的4张相同牌）
 * - 不负责生成完整手牌
 * - 德州扑克特殊牌型需要同时处理公共牌
 *
 * 设计原则：
 * 1. 只处理"可保证"的牌型
 * 2. 返回核心牌，补牌由调用方负责
 * 3. 德州扑克需要公共牌配合的牌型，返回空，由完整手牌生成器处理
 *
 * @author poker-platform
 */
@Slf4j
public class CoreCardGenerator {

    private final GameType gameType;

    public CoreCardGenerator(GameType gameType) {
        this.gameType = gameType;
    }

    // ==================== 主入口 ====================

    /**
     * 生成核心牌
     * @param deck 牌堆
     * @param targetRank 目标牌型
     * @return 核心牌列表，如果不需要核心牌返回 null
     */
    public List<Card> generate(CardDeck deck, HandRank targetRank) {
        if (targetRank == null) {
            return null;
        }

        switch (gameType) {
            case DOUDIZHU:
                return generateDoudizhuCore(deck, targetRank);
            case TEXAS:
                return generateTexasCore(deck, targetRank);
            case BULL:
                return generateBullCore(deck, targetRank);
            default:
                return null;
        }
    }

    /**
     * 判断是否需要完整手牌生成（德州扑克专用）
     */
    public boolean needFullHandGeneration(HandRank targetRank) {
        if (gameType != GameType.TEXAS) {
            return false;
        }

        switch (targetRank) {
            case TEXAS_ROYAL_FLUSH:
            case TEXAS_STRAIGHT_FLUSH:
            case TEXAS_FOUR_OF_KIND:
            case TEXAS_FULL_HOUSE:
            case TEXAS_FLUSH:
            case TEXAS_STRAIGHT:
            case TEXAS_THREE_OF_KIND:
            case TEXAS_TWO_PAIR:
                return true;
            default:
                return false;
        }
    }

    // ==================== 斗地主核心生成 ====================

    private List<Card> generateDoudizhuCore(CardDeck deck, HandRank targetRank) {
        switch (targetRank) {
            // 需要特殊核心牌的牌型
            case DOUDIZHU_ROCKET:      // 王炸
                return generateRocket(deck);
            case DOUDIZHU_BOMB:         // 炸弹
                return generateBomb(deck);
            case DOUDIZHU_STRAIGHT:     // 顺子
                return generateStraight(deck, 5);
            case DOUDIZHU_TRIPLE:       // 三张
                return generateTriple(deck);
            case DOUDIZHU_PAIR:         // 对子
                return generatePair(deck);

            // 不需要核心牌的牌型
            case DOUDIZHU_SINGLE:       // 单张
            case DOUDIZHU_JUNK:         // 垃圾牌
            default:
                return null;
        }
    }

    // ==================== 德州核心生成 ====================

    private List<Card> generateTexasCore(CardDeck deck, HandRank targetRank) {
        switch (targetRank) {
            // 手牌本身就能决定的牌型
            case TEXAS_ONE_PAIR:        // 一对
                return generatePair(deck);
            case TEXAS_HIGH_CARD:       // 高牌
                return null;

            // 需要公共牌配合的牌型 - 返回 null，由完整手牌生成器处理
            case TEXAS_ROYAL_FLUSH:
            case TEXAS_STRAIGHT_FLUSH:
            case TEXAS_FOUR_OF_KIND:
            case TEXAS_FULL_HOUSE:
            case TEXAS_FLUSH:
            case TEXAS_STRAIGHT:
            case TEXAS_THREE_OF_KIND:
            case TEXAS_TWO_PAIR:
                return null;
            default:
                return null;
        }
    }

    /**
     * 德州扑克完整手牌生成（包含公共牌）
     * 在 needFullHandGeneration 返回 true 时调用
     */
    public List<Card> generateTexasFullHand(CardDeck deck, HandRank targetRank, int handSize) {
        log.debug("德州扑克完整手牌生成: targetRank={}", targetRank.getName());

        switch (targetRank) {
            case TEXAS_FOUR_OF_KIND:
                return generateTexasFourOfKindHand(deck, handSize);
            case TEXAS_THREE_OF_KIND:
                return generateTexasThreeOfKindHand(deck, handSize);
            case TEXAS_TWO_PAIR:
                return generateTexasTwoPairHand(deck, handSize);
            case TEXAS_FULL_HOUSE:
                return generateTexasFullHouseHand(deck, handSize);
            case TEXAS_FLUSH:
                return generateTexasFlushHand(deck, handSize);
            case TEXAS_STRAIGHT:
                return generateTexasStraightHand(deck, handSize);
            case TEXAS_STRAIGHT_FLUSH:
                return generateTexasStraightFlushHand(deck, handSize);
            case TEXAS_ROYAL_FLUSH:
                return generateTexasRoyalFlushHand(deck, handSize);
            default:
                return null;
        }
    }

    // ==================== 牛牛核心生成 ====================

    private List<Card> generateBullCore(CardDeck deck, HandRank targetRank) {
        switch (targetRank) {
            // 需要特殊核心牌的牌型
            case BULL_FIVE_SMALL:       // 五小牛
                return generateFiveSmall(deck);
            case BULL_FOUR_BOMB:         // 四炸
                return generateFourBomb(deck);
            case BULL_BULL:              // 牛牛
                return generateBullBull(deck);

            // 不需要核心牌的牌型
            case BULL_1:
            case BULL_2:
            case BULL_3:
            case BULL_4:
            case BULL_5:
            case BULL_6:
            case BULL_7:
            case BULL_8:
            case BULL_9:
            case BULL_NO:
            default:
                return null;
        }
    }

    // ==================== 基础牌型生成方法 ====================

    /**
     * 生成王炸（大小王）
     */
    private List<Card> generateRocket(CardDeck deck) {
        Card small = deck.findAndRemove(52);  // 小王
        Card big = deck.findAndRemove(53);    // 大王
        if (small != null && big != null) {
            return List.of(small, big);
        }
        return null;
    }

    /**
     * 生成炸弹（4张相同牌值）
     */
    private List<Card> generateBomb(CardDeck deck) {
        for (int rank = 3; rank <= 15; rank++) {
            List<Card> bomb = deck.findAndRemoveByRank(rank, 4);
            if (bomb != null && bomb.size() == 4) {
                return bomb;
            }
        }
        return null;
    }

    /**
     * 生成顺子
     */
    private List<Card> generateStraight(CardDeck deck, int length) {
        for (int start = 3; start <= 15 - length; start++) {
            List<Card> straight = deck.findAndRemoveStraight(start, length);
            if (straight != null && straight.size() == length) {
                return straight;
            }
        }
        return null;
    }

    /**
     * 生成三张（3张相同牌值）
     */
    private List<Card> generateTriple(CardDeck deck) {
        for (int rank = 3; rank <= 15; rank++) {
            List<Card> triple = deck.findAndRemoveByRank(rank, 3);
            if (triple != null && triple.size() == 3) {
                return triple;
            }
        }
        return null;
    }

    /**
     * 生成对子（2张相同牌值）
     */
    private List<Card> generatePair(CardDeck deck) {
        for (int rank = 3; rank <= 17; rank++) {
            List<Card> pair = deck.findAndRemoveByRank(rank, 2);
            if (pair != null && pair.size() == 2) {
                return pair;
            }
        }
        return null;
    }

    // ==================== 牛牛特殊牌型生成 ====================

    /**
     * 生成五小牛：A,A,2,2,3 (点数总和 < 10)
     */
    private List<Card> generateFiveSmall(CardDeck deck) {
        Card a1 = deck.findAndRemoveByRank(14);
        Card a2 = deck.findAndRemoveByRank(14);
        Card two1 = deck.findAndRemoveByRank(2);
        Card two2 = deck.findAndRemoveByRank(2);
        Card three = deck.findAndRemoveByRank(3);

        if (a1 != null && a2 != null && two1 != null && two2 != null && three != null) {
            return List.of(a1, a2, two1, two2, three);
        }
        return null;
    }

    /**
     * 生成四炸：4张相同牌值 + 1张随机牌
     */
    private List<Card> generateFourBomb(CardDeck deck) {
        for (int rank = 3; rank <= 13; rank++) {
            List<Card> bomb = deck.findAndRemoveByRank(rank, 4);
            if (bomb != null && bomb.size() == 4) {
                // 四炸需要5张牌，这里只返回4张核心牌
                // 第5张由调用方补充
                return bomb;
            }
        }
        return null;
    }

    /**
     * 生成牛牛：3,4,3,5,5 或类似组合
     */
    private List<Card> generateBullBull(CardDeck deck) {
        // 方案1: 3,4,3,5,5
        Card three1 = deck.findAndRemoveByRank(3);
        Card three2 = deck.findAndRemoveByRank(3);
        Card four = deck.findAndRemoveByRank(4);
        Card five1 = deck.findAndRemoveByRank(5);
        Card five2 = deck.findAndRemoveByRank(5);

        if (three1 != null && three2 != null && four != null && five1 != null && five2 != null) {
            return List.of(three1, three2, four, five1, five2);
        }

        // 方案2: 4,6,4,7,7
        Card four1 = deck.findAndRemoveByRank(4);
        Card four2 = deck.findAndRemoveByRank(4);
        Card six = deck.findAndRemoveByRank(6);
        Card seven1 = deck.findAndRemoveByRank(7);
        Card seven2 = deck.findAndRemoveByRank(7);

        if (four1 != null && four2 != null && six != null && seven1 != null && seven2 != null) {
            return List.of(four1, four2, six, seven1, seven2);
        }

        return null;
    }

    // ==================== 德州扑克完整手牌生成 ====================

    /**
     * 生成四条手牌（需要公共牌配合）
     * 策略：在公共牌中放4张相同牌值，手牌中放一张
     */
    private List<Card> generateTexasFourOfKindHand(CardDeck deck, int handSize) {
        // 寻找有4张的牌值
        for (int rank = 3; rank <= 14; rank++) {
            List<Card> fourCards = deck.findAndRemoveByRank(rank, 4);
            if (fourCards != null && fourCards.size() == 4) {
                // 这4张牌将作为公共牌，不放回手牌
                // 手牌只需要随机2张
                List<Card> hand = new ArrayList<>();
                for (int i = 0; i < handSize; i++) {
                    hand.add(deck.draw());
                }
                log.debug("生成四条手牌成功，公共牌包含{}点", rank);
                return hand;
            }
        }
        return null;
    }

    /**
     * 生成葫芦手牌（三条+一对）
     */
    private List<Card> generateTexasFullHouseHand(CardDeck deck, int handSize) {
        // 寻找三条
        for (int threeRank = 3; threeRank <= 14; threeRank++) {
            List<Card> three = deck.findAndRemoveByRank(threeRank, 3);
            if (three == null || three.size() != 3) continue;

            // 寻找对子
            for (int pairRank = 3; pairRank <= 14; pairRank++) {
                if (pairRank == threeRank) continue;
                List<Card> pair = deck.findAndRemoveByRank(pairRank, 2);
                if (pair != null && pair.size() == 2) {
                    // 将三条和对子放回牌堆（作为公共牌）
                    deck.addCards(three);
                    deck.addCards(pair);
                    // 手牌随机
                    List<Card> hand = new ArrayList<>();
                    for (int i = 0; i < handSize; i++) {
                        hand.add(deck.draw());
                    }
                    log.debug("生成葫芦手牌成功，三条{}点，对子{}点", threeRank, pairRank);
                    return hand;
                }
            }
            // 恢复三条
            deck.addCards(three);
        }
        return null;
    }

    /**
     * 生成同花手牌
     */
    private List<Card> generateTexasFlushHand(CardDeck deck, int handSize) {
        // 获取5张同花色的牌作为公共牌
        String[] suits = {"SPADE", "HEART", "CLUB", "DIAMOND"};

        for (String suit : suits) {
            List<Card> flush = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Card card = deck.findAndRemoveBySuitAndRank(suit, 0); // 任意牌值
                if (card == null) break;
                flush.add(card);
            }
            if (flush.size() == 5) {
                deck.addCards(flush);  // 放回作为公共牌
                List<Card> hand = new ArrayList<>();
                for (int i = 0; i < handSize; i++) {
                    hand.add(deck.draw());
                }
                log.debug("生成同花手牌成功，花色={}", suit);
                return hand;
            }
        }
        return null;
    }

    /**
     * 生成顺子手牌
     */
    private List<Card> generateTexasStraightHand(CardDeck deck, int handSize) {
        for (int start = 3; start <= 10; start++) {
            List<Card> straight = deck.findAndRemoveStraight(start, 5);
            if (straight != null && straight.size() == 5) {
                deck.addCards(straight);  // 放回作为公共牌
                List<Card> hand = new ArrayList<>();
                for (int i = 0; i < handSize; i++) {
                    hand.add(deck.draw());
                }
                log.debug("生成顺子手牌成功，起始点={}", start);
                return hand;
            }
        }
        return null;
    }

    /**
     * 生成同花顺手牌
     */
    private List<Card> generateTexasStraightFlushHand(CardDeck deck, int handSize) {
        // 先尝试生成同花顺
        String[] suits = {"SPADE", "HEART", "CLUB", "DIAMOND"};

        for (String suit : suits) {
            for (int start = 3; start <= 10; start++) {
                List<Card> straightFlush = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    Card card = deck.findAndRemoveBySuitAndRank(suit, start + i);
                    if (card == null) break;
                    straightFlush.add(card);
                }
                if (straightFlush.size() == 5) {
                    deck.addCards(straightFlush);
                    List<Card> hand = new ArrayList<>();
                    for (int i = 0; i < handSize; i++) {
                        hand.add(deck.draw());
                    }
                    log.debug("生成同花顺手牌成功，花色={}, 起始点={}", suit, start);
                    return hand;
                }
            }
        }
        return null;
    }

    /**
     * 生成皇家同花顺手牌
     */
    private List<Card> generateTexasRoyalFlushHand(CardDeck deck, int handSize) {
        String[] suits = {"SPADE", "HEART", "CLUB", "DIAMOND"};

        for (String suit : suits) {
            List<Card> royal = new ArrayList<>();
            for (int rank : List.of(10, 11, 12, 13, 14)) {
                Card card = deck.findAndRemoveBySuitAndRank(suit, rank);
                if (card == null) break;
                royal.add(card);
            }
            if (royal.size() == 5) {
                deck.addCards(royal);
                List<Card> hand = new ArrayList<>();
                for (int i = 0; i < handSize; i++) {
                    hand.add(deck.draw());
                }
                log.debug("生成皇家同花顺手牌成功，花色={}", suit);
                return hand;
            }
        }
        return null;
    }

    /**
     * 生成两对手牌
     */
    private List<Card> generateTexasTwoPairHand(CardDeck deck, int handSize) {
        List<Integer> usedRanks = new ArrayList<>();
        List<Card> pairs = new ArrayList<>();

        for (int rank = 3; rank <= 14 && pairs.size() < 4; rank++) {
            List<Card> pair = deck.findAndRemoveByRank(rank, 2);
            if (pair != null && pair.size() == 2) {
                pairs.addAll(pair);
                usedRanks.add(rank);
            }
        }

        if (pairs.size() >= 4) {
            // 取两对（4张）
            List<Card> twoPairs = pairs.subList(0, 4);
            deck.addCards(twoPairs);  // 放回作为公共牌
            List<Card> hand = new ArrayList<>();
            for (int i = 0; i < handSize; i++) {
                hand.add(deck.draw());
            }
            log.debug("生成两对手牌成功，对子点数={}", usedRanks.subList(0, 2));
            return hand;
        }

        // 恢复
        deck.addCards(pairs);
        return null;
    }

    /**
     * 生成三条手牌
     */
    private List<Card> generateTexasThreeOfKindHand(CardDeck deck, int handSize) {
        for (int rank = 3; rank <= 14; rank++) {
            List<Card> three = deck.findAndRemoveByRank(rank, 3);
            if (three != null && three.size() == 3) {
                deck.addCards(three);  // 放回作为公共牌
                List<Card> hand = new ArrayList<>();
                for (int i = 0; i < handSize; i++) {
                    hand.add(deck.draw());
                }
                log.debug("生成三条手牌成功，点数={}", rank);
                return hand;
            }
        }
        return null;
    }
}
