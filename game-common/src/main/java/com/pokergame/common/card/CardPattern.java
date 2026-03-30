package com.pokergame.common.card;

import com.pokergame.common.game.GameType;
import lombok.Getter;

/**
 * 牌型枚举
 */
@Getter
public enum CardPattern {
    // ========== 通用牌型 ==========
    PASS(0, "过牌", GameType.ALL, false),
    SINGLE(1, "单张", GameType.ALL, true),
    PAIR(2, "对子", GameType.ALL, true),

    // ========== 斗地主专用 ==========
    THREE(10, "三张", GameType.DOUDIZHU, true),
    THREE_WITH_SINGLE(11, "三带一", GameType.DOUDIZHU, true),
    THREE_WITH_PAIR(12, "三带二", GameType.DOUDIZHU, true),
    STRAIGHT(13, "顺子", GameType.DOUDIZHU, true),
    STRAIGHT_PAIR(14, "连对", GameType.DOUDIZHU, true),
    PLANE(15, "飞机", GameType.DOUDIZHU, true),
    PLANE_WITH_SINGLE(16, "飞机带单", GameType.DOUDIZHU, true),
    PLANE_WITH_PAIR(17, "飞机带对", GameType.DOUDIZHU, true),
    FOUR_WITH_SINGLE(18, "四带二单", GameType.DOUDIZHU, true),
    FOUR_WITH_PAIR(19, "四带二对", GameType.DOUDIZHU, true),
    BOMB(20, "炸弹", GameType.DOUDIZHU, true),
    ROCKET(21, "王炸", GameType.DOUDIZHU, true),

    // ========== 德州扑克专用 ==========
    HIGH_CARD(100, "高牌", GameType.TEXAS, true),
    ONE_PAIR(101, "一对", GameType.TEXAS, true),
    TWO_PAIR(102, "两对", GameType.TEXAS, true),
    THREE_OF_KIND(103, "三条", GameType.TEXAS, true),
    STRAIGHT_POKER(104, "顺子", GameType.TEXAS, true),
    FLUSH(105, "同花", GameType.TEXAS, true),
    FULL_HOUSE(106, "葫芦", GameType.TEXAS, true),
    FOUR_OF_KIND(107, "四条", GameType.TEXAS, true),
    STRAIGHT_FLUSH(108, "同花顺", GameType.TEXAS, true),
    ROYAL_FLUSH(109, "皇家同花顺", GameType.TEXAS, true),

    // ========== 牛牛专用 ==========
    NO_BULL(200, "无牛", GameType.BULL, true),
    BULL_1(201, "牛一", GameType.BULL, true),
    BULL_2(202, "牛二", GameType.BULL, true),
    BULL_3(203, "牛三", GameType.BULL, true),
    BULL_4(204, "牛四", GameType.BULL, true),
    BULL_5(205, "牛五", GameType.BULL, true),
    BULL_6(206, "牛六", GameType.BULL, true),
    BULL_7(207, "牛七", GameType.BULL, true),
    BULL_8(208, "牛八", GameType.BULL, true),
    BULL_9(209, "牛九", GameType.BULL, true),
    BULL_BULL(210, "牛牛", GameType.BULL, true),
    FOUR_BOMB(211, "四炸", GameType.BULL, true),
    FIVE_SMALL(212, "五小牛", GameType.BULL, true);

    /** 牌型编码 */
    private final int code;

    /** 牌型名称 */
    private final String name;

    /** 牌型对应的游戏类型 */
    private final GameType gameType;

    /** 是否可以压过上家 */
    private final boolean canBeat;

    CardPattern(int code, String name, GameType gameType, boolean canBeat) {
        this.code = code;
        this.name = name;
        this.gameType = gameType;
        this.canBeat = canBeat;
    }

    /**
     * 判断是否为该游戏支持的牌型
     */
    public boolean isSupportedBy(GameType gameType) {
        if (this.gameType == GameType.ALL) return true;
        return this.gameType == gameType;
    }
}
