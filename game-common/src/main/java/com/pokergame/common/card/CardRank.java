package com.pokergame.common.card;

import lombok.Getter;

/**
 * 牌值枚举（支持斗地主、德州扑克、麻将）
 */
@Getter
public enum CardRank {
    THREE(3, 0, "3"),
    FOUR(4, 1, "4"),
    FIVE(5, 2, "5"),
    SIX(6, 3, "6"),
    SEVEN(7, 4, "7"),
    EIGHT(8, 5, "8"),
    NINE(9, 6, "9"),
    TEN(10, 7, "10"),
    JACK(11, 8, "J"),
    QUEEN(12, 9, "Q"),
    KING(13, 10, "K"),
    ACE(14, 11, "A"),
    TWO(15, 12, "2"),
    JOKER_SMALL(16, 13, "🃏", "小王"),
    JOKER_BIG(17, 14, "🃏", "大王");

    private final int value;       // 比较大小用
    private final int rankOffset;  // 用于计算牌ID
    private final String symbol;
    private final String name;

    CardRank(int value, int rankOffset, String symbol) {
        this(value, rankOffset, symbol, symbol);
    }

    CardRank(int value, int rankOffset, String symbol, String name) {
        this.value = value;
        this.rankOffset = rankOffset;
        this.symbol = symbol;
        this.name = name;
    }

}
