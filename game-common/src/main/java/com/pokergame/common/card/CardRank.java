package com.pokergame.common.card;

import lombok.Getter;

/**
 * 牌值枚举（支持斗地主、德州扑克、麻将）
 */
public enum CardRank {
    // 斗地主和德州共用
    THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"), SIX(6, "6"),
    SEVEN(7, "7"), EIGHT(8, "8"), NINE(9, "9"), TEN(10, "10"),
    JACK(11, "J"), QUEEN(12, "Q"), KING(13, "K"),
    ACE(14, "A"),

    // 斗地主专用
    TWO(15, "2"),
    // 王牌
    JOKER_SMALL(16, "🃏", "小王"),
    JOKER_BIG(17, "🃏", "大王");

    @Getter
    private final int value;      // 比较大小用
    @Getter
    private final String symbol;   // 显示符号
    @Getter
    private final String name;     // 中文名

    CardRank(int value, String symbol) {
        this(value, symbol, symbol);
    }

    CardRank(int value, String symbol, String name) {
        this.value = value;
        this.symbol = symbol;
        this.name = name;
    }

}
