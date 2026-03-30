package com.pokergame.common.pattern;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import lombok.Data;

import java.util.List;

/**
 * 牌型识别结果
 * 包含牌型、主牌值、所有牌、辅助信息等
 */
@Data
public class PatternResult {

    /** 牌型 */
    private final CardPattern pattern;

    /** 主牌值（用于比较大小） */
    private final int mainRank;

    /** 完整牌列表 */
    private final List<Card> cards;

    /** 辅助值（用于复杂牌型比较，如顺子长度、连对数量等） */
    private int subRank;

    /** 额外数据（用于存储比较所需的其他信息） */
    private Object extra;

    public PatternResult(CardPattern pattern, int mainRank, List<Card> cards) {
        this.pattern = pattern;
        this.mainRank = mainRank;
        this.cards = cards;
        this.subRank = 0;
    }

    public PatternResult(CardPattern pattern, int mainRank, List<Card> cards, int subRank) {
        this.pattern = pattern;
        this.mainRank = mainRank;
        this.cards = cards;
        this.subRank = subRank;
    }

    /**
     * 判断是否为有效牌型
     */
    public boolean isValid() {
        return pattern != null && pattern != CardPattern.PASS;
    }

    /**
     * 判断是否能压过上家
     */
    public boolean canBeat(PatternResult last) {
        if (last == null || !last.isValid()) return true;
        if (!this.isValid()) return false;

        // 火箭最大
        if (this.pattern == CardPattern.ROCKET) return true;
        if (last.pattern == CardPattern.ROCKET) return false;

        // 炸弹可以压非炸弹
        if (this.pattern == CardPattern.BOMB) {
            if (last.pattern != CardPattern.BOMB) return true;
            return this.mainRank > last.mainRank;
        }

        // 同牌型比较
        if (this.pattern == last.pattern) {
            if (this.mainRank != last.mainRank) {
                return this.mainRank > last.mainRank;
            }
            return this.subRank > last.subRank;
        }

        return false;
    }

    @Override
    public String toString() {
        if (pattern == CardPattern.PASS) return "过牌";
        return pattern.getName() + " [主牌=" + mainRank + ", 牌数=" + cards.size() + "]";
    }
}
