package com.pokergame.common.deal;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.game.GameType;
import com.pokergame.common.pattern.PatternRecognizer;
import com.pokergame.common.pattern.PatternRecognizerFactory;
import com.pokergame.common.pattern.PatternResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 手牌强度评估器
 * 将牌型结果转换为强度等级
 *
 * @author poker-platform
 */
@Slf4j
public class HandRankEvaluator {

    /**
     * 评估手牌强度
     * @param gameType 游戏类型
     * @param cards 手牌
     * @return 强度等级
     */
    public static HandRank evaluate(GameType gameType, List<Card> cards) {
        PatternRecognizer recognizer = PatternRecognizerFactory.get(gameType);
        PatternResult result = recognizer.recognize(cards);

        return convertToHandRank(gameType, result);
    }

    /**
     * 将牌型结果转换为强度等级
     */
    private static HandRank convertToHandRank(GameType gameType, PatternResult result) {
        CardPattern pattern = result.getPattern();
        int mainRank = result.getMainRank();

        switch (gameType) {
            case DOUDIZHU:
                return convertDoudizhuPattern(pattern);
            case TEXAS:
                return convertTexasPattern(pattern, mainRank);
            case BULL:
                return convertBullPattern(pattern, mainRank);
            default:
                return HandRank.DOUDIZHU_JUNK;
        }
    }

    private static HandRank convertDoudizhuPattern(CardPattern pattern) {
        switch (pattern) {
            case ROCKET: return HandRank.DOUDIZHU_ROCKET;
            case BOMB: return HandRank.DOUDIZHU_BOMB;
            case STRAIGHT: return HandRank.DOUDIZHU_STRAIGHT;
            case THREE: return HandRank.DOUDIZHU_TRIPLE;
            case PAIR: return HandRank.DOUDIZHU_PAIR;
            case SINGLE: return HandRank.DOUDIZHU_SINGLE;
            default: return HandRank.DOUDIZHU_JUNK;
        }
    }

    private static HandRank convertTexasPattern(CardPattern pattern, int mainRank) {
        switch (pattern) {
            case ROYAL_FLUSH: return HandRank.TEXAS_ROYAL_FLUSH;
            case STRAIGHT_FLUSH: return HandRank.TEXAS_STRAIGHT_FLUSH;
            case FOUR_OF_KIND: return HandRank.TEXAS_FOUR_OF_KIND;
            case FULL_HOUSE: return HandRank.TEXAS_FULL_HOUSE;
            case FLUSH: return HandRank.TEXAS_FLUSH;
            case STRAIGHT_POKER: return HandRank.TEXAS_STRAIGHT;
            case THREE_OF_KIND: return HandRank.TEXAS_THREE_OF_KIND;
            case TWO_PAIR: return HandRank.TEXAS_TWO_PAIR;
            case ONE_PAIR: return HandRank.TEXAS_ONE_PAIR;
            default: return HandRank.TEXAS_HIGH_CARD;
        }
    }

    private static HandRank convertBullPattern(CardPattern pattern, int mainRank) {
        if (pattern == CardPattern.FIVE_SMALL) return HandRank.BULL_FIVE_SMALL;
        if (pattern == CardPattern.FOUR_BOMB) return HandRank.BULL_FOUR_BOMB;
        if (pattern == CardPattern.BULL_BULL) return HandRank.BULL_BULL;

        // 牛1-牛9
        if (mainRank >= 1 && mainRank <= 9) {
            return HandRank.values()[mainRank + 18]; // BULL_1 到 BULL_9 的索引
        }

        return HandRank.BULL_NO;
    }
}
