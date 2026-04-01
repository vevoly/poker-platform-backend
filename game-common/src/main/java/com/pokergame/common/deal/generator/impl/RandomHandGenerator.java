package com.pokergame.common.deal.generator.impl;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardDeck;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.generator.HandGenerator;
import com.pokergame.common.game.GameType;

import java.util.ArrayList;
import java.util.List;

/**
 * 随机手牌生成器
 * 无目标牌型时的兜底方案
 *
 * @author poker-platform
 */
public class RandomHandGenerator implements HandGenerator {

    @Override
    public List<Card> generate(CardDeck deck, HandRank targetRank, int handSize, int maxAttempts) {
        // 随机生成器不关心目标牌型
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < handSize; i++) {
            hand.add(deck.draw());
        }
        return hand;
    }

    @Override
    public boolean supports(GameType gameType) {
        return true;  // 支持所有游戏类型
    }

    @Override
    public String getName() {
        return "RandomHandGenerator";
    }
}
