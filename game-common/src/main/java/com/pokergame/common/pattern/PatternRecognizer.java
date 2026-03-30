package com.pokergame.common.pattern;


import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.game.GameType;

import java.util.List;
import java.util.Set;

/**
 * 牌型识别器接口
 * 每种游戏实现自己的牌型识别逻辑
 */
public interface PatternRecognizer {

    /**
     * 识别牌型
     * @param cards 待识别的牌（已排序）
     * @return 牌型识别结果
     */
    PatternResult recognize(List<Card> cards);

    /**
     * 判断当前牌型是否能压过上家
     * @param last 上家出的牌
     * @param current 当前出的牌
     * @return true表示能压过
     */
    boolean canBeat(PatternResult last, PatternResult current);

    /**
     * 获取该游戏支持的所有牌型
     */
    Set<CardPattern> getSupportedPatterns();

    /**
     * 获取游戏类型
     */
    GameType getGameType();

    /**
     * 对牌进行排序（按游戏规则）
     * @param cards 原始牌列表
     * @return 排序后的牌列表
     */
    List<Card> sortCards(List<Card> cards);
}
