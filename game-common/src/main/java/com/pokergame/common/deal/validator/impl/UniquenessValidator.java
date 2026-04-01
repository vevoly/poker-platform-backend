package com.pokergame.common.deal.validator.impl;

import com.pokergame.common.card.Card;
import com.pokergame.common.deal.validator.DealValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 牌唯一性验证器
 * 验证所有手牌中没有重复的牌
 *
 * @author poker-platform
 */
public class UniquenessValidator implements DealValidator {

    @Override
    public void validate(List<List<Card>> hands) {
        Set<Integer> cardIds = new HashSet<>();

        for (List<Card> hand : hands) {
            for (Card card : hand) {
                int id = card.getId();
                if (cardIds.contains(id)) {
                    throw new IllegalStateException("发现重复牌: " + card);
                }
                cardIds.add(id);
            }
        }
    }

    @Override
    public String getName() {
        return "UniquenessValidator";
    }
}
