package com.pokergame.common.deal.validator.impl;

import com.pokergame.common.card.Card;
import com.pokergame.common.deal.validator.DealValidator;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 牌唯一性验证器
 * 验证所有手牌中没有重复的牌
 *
 * @author poker-platform
 */
@Slf4j
public class UniquenessValidator implements DealValidator {

    @Override
    public void validate(List<List<Card>> hands) {
        Set<Integer> cardIds = new HashSet<>();
        for (int i = 0; i < hands.size(); i++) {
            List<Card> hand = hands.get(i);
            log.info("玩家{}手牌({}张): {}", i, hand.size(),
                    hand.stream().map(c -> c.toString() + "(" + c.getId() + ")").collect(Collectors.joining(",")));

            for (Card card : hand) {
                int id = card.getId();
                if (cardIds.contains(id)) {
                    log.error("重复牌 {} (ID={}) 出现在玩家 {} 的手牌中", card, id, i);
                    log.error("该ID首次出现在玩家: {}", findPlayerIndex(hands, id));
                    log.error("已存在的牌ID: {}", cardIds);
                    throw new IllegalStateException("发现重复牌: " + card + "(ID=" + id + ")");
                }
                cardIds.add(id);
            }
        }
    }

    private int findPlayerIndex(List<List<Card>> hands, int targetId) {
        for (int i = 0; i < hands.size(); i++) {
            for (Card card : hands.get(i)) {
                if (card.getId() == targetId) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public String getName() {
        return "UniquenessValidator";
    }
}
