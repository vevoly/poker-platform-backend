package com.pokergame.common.card;

import java.util.HashMap;
import java.util.Map;

/**
 * 牌的定义
 * 不可变对象，使用工厂方法创建
 */
public class Card implements Comparable<Card> {

    /** 牌花色 */
    private final CardSuit suit;

    /** 牌值 */
    private final CardRank rank;

    /** 牌ID 0-53 */
    private final int id;

    /** 牌排序值 */
    private final int sortValue;

    private Card(CardSuit suit, CardRank rank) {
        this.suit = suit;
        this.rank = rank;
        this.id = buildId(suit, rank);
        this.sortValue = buildSortValue(rank, suit);
    }

    // 标准扑克牌（54张）
    private static final Card[] DECK = new Card[54];
    private static final Map<Integer, Card> ID_TO_CARD = new HashMap<>();

    static {
        int index = 0;
        // 生成 4 种花色 x 13 张牌
        for (CardSuit suit : new CardSuit[]{CardSuit.SPADE, CardSuit.HEART, CardSuit.CLUB, CardSuit.DIAMOND}) {
            for (CardRank rank : CardRank.values()) {
                if (rank == CardRank.JOKER_SMALL || rank == CardRank.JOKER_BIG) continue;
                DECK[index] = new Card(suit, rank);
                ID_TO_CARD.put(index, DECK[index]);
                index++;
            }
        }
        // 添加大小王
        DECK[52] = new Card(CardSuit.JOKER, CardRank.JOKER_SMALL);
        DECK[53] = new Card(CardSuit.JOKER, CardRank.JOKER_BIG);
        ID_TO_CARD.put(52, DECK[52]);
        ID_TO_CARD.put(53, DECK[53]);
    }

    private static int buildId(CardSuit suit, CardRank rank) {
        if (suit == CardSuit.JOKER) {
            return rank == CardRank.JOKER_SMALL ? 52 : 53;
        }
        int suitOffset = suit.getValue() * 13;
        int rankOffset = rank.getValue() - 3;
        return suitOffset + rankOffset;
    }

    private static int buildSortValue(CardRank rank, CardSuit suit) {
        // 斗地主排序：先按牌值，再按花色
        if (rank == CardRank.JOKER_SMALL) return 100;
        if (rank == CardRank.JOKER_BIG) return 101;
        int rankValue = rank.getValue();
        if (rankValue <= 15) {
            return rankValue * 10 + suit.getValue();
        }
        return rankValue;
    }

    public static Card of(int id) {
        return ID_TO_CARD.get(id);
    }

    public static Card of(CardSuit suit, CardRank rank) {
        return of(buildId(suit, rank));
    }

    public static Card[] getDeck() {
        return DECK.clone();
    }

    public CardSuit getSuit() { return suit; }
    public CardRank getRank() { return rank; }
    public int getId() { return id; }
    public int getSortValue() { return sortValue; }

    @Override
    public int compareTo(Card o) {
        return Integer.compare(this.sortValue, o.sortValue);
    }

    @Override
    public String toString() {
        if (suit == CardSuit.JOKER) return rank.getName();
        return suit.getSymbol() + rank.getSymbol();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;
        return id == card.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
