package com.pokergame.common.card;


import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 牌堆管理器 - 支持多副牌
 */
@Slf4j
public class CardDeck {
    private final int decksCount;        // 牌堆数量
    private final List<Card> cards;      // 所有牌
    private final Stack<Card> drawPile;  // 抽牌堆
    private final Stack<Card> discardPile; // 弃牌堆

    public CardDeck(int decksCount) {
        this.decksCount = decksCount;
        this.cards = new ArrayList<>();
        this.drawPile = new Stack<>();
        this.discardPile = new Stack<>();
        initCards();
        shuffle();
    }

    private void initCards() {
        for (int i = 0; i < decksCount; i++) {
            for (Card card : Card.getDeck()) {
                cards.add(card);
            }
        }
    }

    public void shuffle() {
        drawPile.clear();
        List<Card> shuffled = new ArrayList<>(cards);
        Collections.shuffle(shuffled);
        drawPile.addAll(shuffled);
    }

    // ==================== 抽牌 ====================

    public Card draw() {
        if (drawPile.isEmpty()) {
            reshuffle();
        }
        return drawPile.isEmpty() ? null : drawPile.pop();
    }

    private void reshuffle() {
        if (discardPile.isEmpty()) return;
        List<Card> reshuffleCards = new ArrayList<>(discardPile);
        Collections.shuffle(reshuffleCards);
        drawPile.addAll(reshuffleCards);
        discardPile.clear();
        log.debug("重新洗牌，共{}张", reshuffleCards.size());
    }

    // ==================== 弃牌 ====================

    public void discard(Card card) {
        discardPile.push(card);
    }

    public void discardAll(List<Card> cards) {
        discardPile.addAll(cards);
    }

    // ==================== 查找并移除（核心生成器需要） ====================

    /**
     * 查找并移除指定ID的牌
     */
    public Card findAndRemove(int cardId) {
        for (int i = 0; i < drawPile.size(); i++) {
            Card card = drawPile.get(i);
            if (card.getId() == cardId) {
                drawPile.remove(i);
                return card;
            }
        }
        return null;
    }

    /**
     * 查找并移除指定牌值的牌（任意花色）
     */
    public Card findAndRemoveByRank(int rankValue) {
        for (int i = 0; i < drawPile.size(); i++) {
            Card card = drawPile.get(i);
            if (card.getRank().getValue() == rankValue) {
                drawPile.remove(i);
                return card;
            }
        }
        return null;
    }

    /**
     * 查找并移除指定牌值的多张牌
     */
    public List<Card> findAndRemoveByRank(int rankValue, int count) {
        List<Card> result = new ArrayList<>();
        Iterator<Card> iterator = drawPile.iterator();

        while (iterator.hasNext() && result.size() < count) {
            Card card = iterator.next();
            if (card.getRank().getValue() == rankValue) {
                iterator.remove();
                result.add(card);
            }
        }

        return result.size() == count ? result : null;
    }

    /**
     * 查找并移除指定花色和牌值的牌
     */
    public Card findAndRemoveBySuitAndRank(String suitName, int rankValue) {
        for (int i = 0; i < drawPile.size(); i++) {
            Card card = drawPile.get(i);
            if (card.getSuit().name().equals(suitName) &&
                    card.getRank().getValue() == rankValue) {
                drawPile.remove(i);
                return card;
            }
        }
        return null;
    }

    /**
     * 查找并移除顺子
     */
    public List<Card> findAndRemoveStraight(int startRank, int length) {
        List<Card> straight = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            int targetRank = startRank + i;
            Card card = findAndRemoveByRank(targetRank);
            if (card == null) {
                // 失败，恢复已移除的牌
                for (Card c : straight) {
                    drawPile.push(c);
                }
                return null;
            }
            straight.add(card);
        }

        return straight;
    }

    /**
     * 获取当前牌堆大小
     */
    public int size() {
        return drawPile.size();
    }

    /**
     * 添加牌回牌堆（用于恢复）
     */
    public void addCard(Card card) {
        if (card != null) {
            drawPile.push(card);
        }
    }

    /**
     * 批量添加牌回牌堆
     */
    public void addCards(List<Card> cards) {
        drawPile.addAll(cards);
    }

    // ==================== 备份与恢复 ====================

    /**
     * 创建当前牌堆的副本
     */
    public CardDeck copy() {
        CardDeck copy = new CardDeck(1);
        copy.drawPile.clear();
        copy.drawPile.addAll(this.drawPile);
        copy.discardPile.clear();
        copy.discardPile.addAll(this.discardPile);
        return copy;
    }

    /**
     * 从另一个牌堆恢复状态
     */
    public void restore(CardDeck backup) {
        this.drawPile.clear();
        this.drawPile.addAll(backup.drawPile);
        this.discardPile.clear();
        this.discardPile.addAll(backup.discardPile);
    }
}
