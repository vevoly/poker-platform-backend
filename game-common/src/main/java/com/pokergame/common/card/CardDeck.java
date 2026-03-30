package com.pokergame.common.card;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * 牌堆管理器 - 支持多副牌
 */
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
    }

    public void discard(Card card) {
        discardPile.push(card);
    }
}
