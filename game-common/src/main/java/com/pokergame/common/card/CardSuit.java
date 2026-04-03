package com.pokergame.common.card;

import lombok.Getter;

/**
 * 花色枚举
 */
@Getter
public enum CardSuit {

    SPADE(4, 0, "♠", "黑桃"),     // ID偏移 0
    HEART(3, 13, "♥", "红桃"),    // ID偏移 13
    CLUB(2, 26, "♣", "梅花"),     // ID偏移 26
    DIAMOND(1, 39, "♦", "方块"),  // ID偏移 39
    JOKER(0, 52, "🃏", "王牌");    // ID偏移 52

    private final int value;       // 用于比较大小
    private final int idOffset;    // 用于计算牌ID
    private final String symbol;
    private final String name;

    CardSuit(int value, int idOffset, String symbol, String name) {
        this.value = value;
        this.idOffset = idOffset;
        this.symbol = symbol;
        this.name = name;
    }
}