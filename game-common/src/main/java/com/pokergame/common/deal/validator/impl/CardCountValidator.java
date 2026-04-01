package com.pokergame.common.deal.validator.impl;

import com.pokergame.common.card.Card;
import com.pokergame.common.deal.validator.DealValidator;

import java.util.List;

/**
 * 牌数验证器
 * 验证总牌数是否符合游戏规则
 *
 * @author poker-platform
 */
public class CardCountValidator implements DealValidator {

    private final int expectedTotalCards;

    public CardCountValidator(int expectedTotalCards) {
        this.expectedTotalCards = expectedTotalCards;
    }

    @Override
    public void validate(List<List<Card>> hands) {
        int total = hands.stream().mapToInt(List::size).sum();

        if (total != expectedTotalCards) {
            throw new IllegalStateException(
                    String.format("牌数错误: 实际%d张, 期望%d张", total, expectedTotalCards));
        }
    }

    @Override
    public String getName() {
        return "CardCountValidator";
    }
}
