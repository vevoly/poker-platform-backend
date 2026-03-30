package com.pokergame.common.card;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 牌的定义
 * 不可变对象，使用工厂方法创建
 *
 * | 点数 | 黑桃ID | 红桃ID | 梅花ID | 方块ID |     ID 组合示例 (4张)     |
 * | :--- | :----: | :----: | :----: | :----: | :----------------------- |
 * |  3   |   0    |   13   |   26   |   39   | { 0, 13, 26, 39 }        |
 * |  4   |   1    |   14   |   27   |   40   | { 1, 14, 27, 40 }        |
 * |  5   |   2    |   15   |   28   |   41   | { 2, 15, 28, 41 }        |
 * |  6   |   3    |   16   |   29   |   42   | { 3, 16, 29, 42 }        |
 * |  7   |   4    |   17   |   30   |   43   | { 4, 17, 30, 43 }        |
 * |  8   |   5    |   18   |   31   |   44   | { 5, 18, 31, 44 }        |
 * |  9   |   6    |   19   |   32   |   45   | { 6, 19, 32, 45 }        |
 * |  10  |   7    |   20   |   33   |   46   | { 7, 20, 33, 46 }        |
 * |  J   |   8    |   21   |   34   |   47   | { 8, 21, 34, 47 }        |
 * |  Q   |   9    |   22   |   35   |   48   | { 9, 22, 35, 48 }        |
 * |  K   |   10   |   23   |   36   |   49   | { 10, 23, 36, 49 }       |
 * |  A   |   11   |   24   |   37   |   50   | { 11, 24, 37, 50 }       |
 * |  2   |   12   |   25   |   38   |   51   | { 12, 25, 38, 51 }       |
 */
@Getter
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

    /**
     * 获取标准扑克牌
     */
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

    /**
     * 根据花色和牌值生成牌ID
     * 每种花色有 13 张常规牌（A 到 K）
     * suit.getValue() 返回该花色的编号（例如：0=黑桃，1=红心 等）
     * suitOffset 表示当前花色在整副牌中的起始位置（如黑桃从 0 开始，红心从 13 开始
     */
    private static int buildId(CardSuit suit, CardRank rank) {
        if (suit == CardSuit.JOKER) {
            return rank == CardRank.JOKER_SMALL ? 52 : 53;
        }


        int suitOffset = suit.getValue() * 13;
        // 牌值偏移，减去 3 是为了使最小点数（3）的偏移为 0，这样整副牌 ID 可以从 0 开始连续排列
        int rankOffset = rank.getValue() - 3;
        return suitOffset + rankOffset;
    }

    /**
     * 获取牌排序值
     * 根据牌的点数（rank）和花色（suit）生成一个整数，用于排序
     * @param rank
     * @param suit
     * @return
     */
    private static int buildSortValue(CardRank rank, CardSuit suit) {
        // 斗地主排序：先按牌值，再按花色，大小王为固定值
        if (rank == CardRank.JOKER_SMALL) return 100;
        if (rank == CardRank.JOKER_BIG) return 101;
        // 获取当前牌点数的数值（如 A=14，2=15，3=3，…，K=13）
        int rankValue = rank.getValue();

        /*
        对于点数小于等于 15 的牌（即常规牌），生成一个排序值：
            - 排序值 = 点数 × 10 + 花色值
            - 乘以 10 是为了给花色留出空间（0~9），这样同一张点数下，不同花色的牌可以排序。
            - 例如：
                黑桃 3（rank=3, suit=4）→ 3 * 10 + 4 = 34
                方块 3（rank=3, suit=1）→ 3 * 10 + 1 = 31
                所以：3♠ > 3♣ > 3♦ > 3♥
         */
        if (rankValue <= 15) {
            return rankValue * 10 + suit.getValue();
        }
        return rankValue;
    }

    /**
     * 根据牌ID获取牌
     * @param id
     * @return
     */
    public static Card of(int id) {
        return ID_TO_CARD.get(id);
    }

    /**
     * 根据花色和牌值获取牌
     * @param suit
     * @param rank
     * @return
     */
    public static Card of(CardSuit suit, CardRank rank) {
        return of(buildId(suit, rank));
    }

    /**
     * 获取牌组
     * @return
     */
    public static Card[] getDeck() {
        return DECK.clone();
    }

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
