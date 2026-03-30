package com.pokergame.common.card;

/**
 * 花色枚举
 */
public enum CardSuit {

    SPADE(4, "♠", "黑桃"),
    HEART(3, "♥", "红桃"),
    CLUB(2, "♣", "梅花"),
    DIAMOND(1, "♦", "方块"),
    JOKER(0, "🃏", "王");

    private final int value;      // 用于比较大小
    private final String symbol;   // 显示符号
    private final String name;     // 中文名

    CardSuit(int value, String symbol, String name) {
        this.value = value;
        this.symbol = symbol;
        this.name = name;
    }

    public int getValue() { return value; }
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
}