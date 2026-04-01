package com.pokergame.common.deal.generator.impl;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardDeck;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.generator.HandGenerator;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 组合手牌生成器
 * 先尝试按牌型生成，失败后降级为随机生成
 *
 * @author poker-platform
 */
@Slf4j
public class CompositeHandGenerator implements HandGenerator {

    private final RankHandGenerator rankGenerator;
    private final RandomHandGenerator randomGenerator;

    public CompositeHandGenerator(GameType gameType) {
        this.rankGenerator = new RankHandGenerator(gameType);
        this.randomGenerator = new RandomHandGenerator();
    }

    @Override
    public List<Card> generate(CardDeck deck, HandRank targetRank, int handSize, int maxAttempts) {
        if (targetRank != null) {
            List<Card> hand = rankGenerator.generate(deck, targetRank, handSize, maxAttempts);
            if (hand != null) {
                log.debug("使用按牌型生成器成功");
                return hand;
            }
            log.warn("按牌型生成失败，降级为随机生成");
        }

        return randomGenerator.generate(deck, null, handSize, maxAttempts);
    }

    @Override
    public boolean supports(GameType gameType) {
        return true;
    }

    @Override
    public String getName() {
        return "CompositeHandGenerator";
    }
}
