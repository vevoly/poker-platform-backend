package com.pokergame.common.deal.generator.impl;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardDeck;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.HandRankEvaluator;
import com.pokergame.common.deal.generator.CoreCardGenerator;
import com.pokergame.common.deal.generator.HandGenerator;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 按牌型生成器
 * 根据目标牌型生成对应的手牌
 *
 * @author poker-platform
 */
@Slf4j
public class RankHandGenerator implements HandGenerator {

    private final GameType gameType;
    private final HandRankEvaluator evaluator;
    private final CoreCardGenerator coreGenerator;

    public RankHandGenerator(GameType gameType) {
        this.gameType = gameType;
        this.evaluator = new HandRankEvaluator();
        this.coreGenerator = new CoreCardGenerator(gameType);
    }

    @Override
    public List<Card> generate(CardDeck deck, HandRank targetRank, int handSize, int maxAttempts) {
        if (targetRank == null) {
            return null;
        }

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            CardDeck backup = deck.copy();

            // 1. 生成核心牌
            List<Card> coreCards = coreGenerator.generate(deck, targetRank);
            if (coreCards == null || coreCards.isEmpty()) {
                deck.restore(backup);
                continue;
            }

            // 2. 检查核心牌数量是否超过手牌大小
            if (coreCards.size() > handSize) {
                deck.restore(backup);
                continue;
            }

            // 3. 补充剩余牌
            List<Card> hand = new ArrayList<>(coreCards);
            int remaining = handSize - coreCards.size();
            hand.addAll(drawCards(deck, remaining));

            // 4. 验证生成的牌型是否符合目标
            HandRank actualRank = evaluator.evaluate(gameType, hand);
            if (actualRank == targetRank) {
                log.debug("成功生成目标牌型: {}, 尝试次数: {}", targetRank.getName(), attempt + 1);
                return hand;
            }

            // 5. 失败，恢复牌堆
            deck.restore(backup);
        }

        log.warn("无法生成目标牌型: {}, 尝试次数: {}", targetRank.getName(), maxAttempts);
        return null;
    }

    private List<Card> drawCards(CardDeck deck, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(deck.draw());
        }
        return cards;
    }

    @Override
    public boolean supports(GameType gameType) {
        return this.gameType == gameType;
    }

    @Override
    public String getName() {
        return "RankHandGenerator[" + gameType + "]";
    }
}
