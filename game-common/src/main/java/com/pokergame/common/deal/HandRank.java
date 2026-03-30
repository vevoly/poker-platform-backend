package com.pokergame.common.deal;

import com.pokergame.common.game.GameType;
import lombok.Getter;

/**
 * 手牌强度等级
 * 用于量化一手牌的强弱，便于发牌概率控制
 *
 * @author poker-platform
 */
@Getter
public enum HandRank {

    // ========== 斗地主等级 ==========
    DOUDIZHU_JUNK(10, "垃圾牌", GameType.DOUDIZHU, 0),
    DOUDIZHU_SINGLE(20, "单张", GameType.DOUDIZHU, 10),
    DOUDIZHU_PAIR(30, "对子", GameType.DOUDIZHU, 20),
    DOUDIZHU_TRIPLE(40, "三张", GameType.DOUDIZHU, 30),
    DOUDIZHU_STRAIGHT(50, "顺子", GameType.DOUDIZHU, 40),
    DOUDIZHU_BOMB(80, "炸弹", GameType.DOUDIZHU, 80),
    DOUDIZHU_ROCKET(100, "王炸", GameType.DOUDIZHU, 100),

    // ========== 德州扑克等级 ==========
    TEXAS_HIGH_CARD(1, "高牌", GameType.TEXAS, 0),
    TEXAS_ONE_PAIR(10, "一对", GameType.TEXAS, 20),
    TEXAS_TWO_PAIR(20, "两对", GameType.TEXAS, 40),
    TEXAS_THREE_OF_KIND(30, "三条", GameType.TEXAS, 60),
    TEXAS_STRAIGHT(40, "顺子", GameType.TEXAS, 80),
    TEXAS_FLUSH(50, "同花", GameType.TEXAS, 90),
    TEXAS_FULL_HOUSE(60, "葫芦", GameType.TEXAS, 95),
    TEXAS_FOUR_OF_KIND(80, "四条", GameType.TEXAS, 98),
    TEXAS_STRAIGHT_FLUSH(95, "同花顺", GameType.TEXAS, 99),
    TEXAS_ROYAL_FLUSH(100, "皇家同花顺", GameType.TEXAS, 100),

    // ========== 牛牛等级 ==========
    BULL_NO(10, "无牛", GameType.BULL, 0),
    BULL_1(20, "牛一", GameType.BULL, 10),
    BULL_2(25, "牛二", GameType.BULL, 15),
    BULL_3(30, "牛三", GameType.BULL, 20),
    BULL_4(35, "牛四", GameType.BULL, 25),
    BULL_5(40, "牛五", GameType.BULL, 30),
    BULL_6(45, "牛六", GameType.BULL, 35),
    BULL_7(50, "牛七", GameType.BULL, 40),
    BULL_8(55, "牛八", GameType.BULL, 45),
    BULL_9(60, "牛九", GameType.BULL, 50),
    BULL_BULL(70, "牛牛", GameType.BULL, 70),
    BULL_FOUR_BOMB(90, "四炸", GameType.BULL, 90),
    BULL_FIVE_SMALL(100, "五小牛", GameType.BULL, 100);

    private final int score;           // 强度分数
    private final String name;         // 名称
    private final GameType gameType;   // 所属游戏
    private final int winRate;         // 预估胜率（0-100）

    HandRank(int score, String name, GameType gameType, int winRate) {
        this.score = score;
        this.name = name;
        this.gameType = gameType;
        this.winRate = winRate;
    }

    public static HandRank fromScore(GameType gameType, int score) {
        for (HandRank rank : values()) {
            if (rank.gameType == gameType && rank.score == score) {
                return rank;
            }
        }
        return null;
    }
}
