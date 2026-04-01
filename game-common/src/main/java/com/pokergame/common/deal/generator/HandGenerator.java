package com.pokergame.common.deal.generator;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardDeck;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;

import java.util.List;

/**
 * 手牌生成器接口
 * 职责：根据目标牌型生成符合要求的手牌
 *
 * @author poker-platform
 */
public interface HandGenerator {

    /**
     * 生成手牌
     * @param deck 牌堆
     * @param targetRank 目标牌型（可为null）
     * @param handSize 手牌大小
     * @param maxAttempts 最大尝试次数
     * @return 生成的手牌，失败返回null
     */
    List<Card> generate(CardDeck deck, HandRank targetRank, int handSize, int maxAttempts);

    /**
     * 是否支持该游戏类型
     */
    boolean supports(GameType gameType);

    /**
     * 获取生成器名称
     */
    String getName();
}
