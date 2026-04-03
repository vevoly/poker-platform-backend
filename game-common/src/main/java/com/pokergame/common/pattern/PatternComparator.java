package com.pokergame.common.pattern;

import com.pokergame.common.card.CardPattern;
import com.pokergame.common.game.GameType;

import java.util.Comparator;

/**
 * 牌型比较器
 *
 * 规则：
 * - 火箭最大
 * - 炸弹可以压非炸弹
 * - 同牌型比较主牌值
 *
 * @author poker-platform
 */
public class PatternComparator implements Comparator<PatternResult> {

    private final GameType gameType;

    public PatternComparator(GameType gameType) {
        this.gameType = gameType;
    }

    @Override
    public int compare(PatternResult o1, PatternResult o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;

        // 过牌最小
        if (o1.getPattern() == CardPattern.PASS) return -1;
        if (o2.getPattern() == CardPattern.PASS) return 1;

        // 火箭最大
        if (o1.getPattern() == CardPattern.ROCKET) return 1;
        if (o2.getPattern() == CardPattern.ROCKET) return -1;

        // 炸弹可以压非炸弹
        if (o1.getPattern() == CardPattern.BOMB && o2.getPattern() != CardPattern.BOMB) {
            return 1;
        }
        if (o2.getPattern() == CardPattern.BOMB && o1.getPattern() != CardPattern.BOMB) {
            return -1;
        }

        // 不同牌型比较牌型优先级
        if (o1.getPattern().getCode() != o2.getPattern().getCode()) {
            return Integer.compare(o1.getPattern().getCode(), o2.getPattern().getCode());
        }

        // 同牌型比较主牌值
        if (o1.getMainRank() != o2.getMainRank()) {
            return Integer.compare(o1.getMainRank(), o2.getMainRank());
        }

        // 比较副牌值
        return Integer.compare(o1.getSubRank(), o2.getSubRank());
    }

    /**
     * 静态方法：判断当前牌是否能压过上家
     * 用于测试用例
     */
    public static boolean canBeat(CardPattern lastPattern, int lastMainRank,
                                  CardPattern currentPattern, int currentMainRank) {
        // 过牌
        if (currentPattern == null || currentPattern == CardPattern.PASS) {
            return false;
        }

        // 上家过牌
        if (lastPattern == null || lastPattern == CardPattern.PASS) {
            return true;
        }

        // 火箭最大
        if (currentPattern == CardPattern.ROCKET) {
            return true;
        }
        if (lastPattern == CardPattern.ROCKET) {
            return false;
        }

        // 炸弹可以压非炸弹
        if (currentPattern == CardPattern.BOMB) {
            if (lastPattern != CardPattern.BOMB) {
                return true;
            }
            return currentMainRank > lastMainRank;
        }

        // 同牌型比较
        if (currentPattern == lastPattern) {
            return currentMainRank > lastMainRank;
        }

        return false;
    }
}
