package com.pokergame.common.pattern;

import com.pokergame.common.card.CardPattern;
import com.pokergame.common.game.GameType;

import java.util.Comparator;

/**
 * 牌型比较器 - 用于比较两手牌的大小
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
}
