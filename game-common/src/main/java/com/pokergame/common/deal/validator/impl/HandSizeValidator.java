package com.pokergame.common.deal.validator.impl;

import com.pokergame.common.card.Card;
import com.pokergame.common.deal.validator.DealValidator;

import java.util.List;

/**
 * 手牌大小验证器
 * 验证每个玩家的手牌数量是否正确
 *
 * @author poker-platform
 */
public class HandSizeValidator implements DealValidator {

    private final List<Integer> expectedHandSizes;

    public HandSizeValidator(List<Integer> expectedHandSizes) {
        this.expectedHandSizes = expectedHandSizes;
    }

    @Override
    public void validate(List<List<Card>> hands) {
        if (hands.size() != expectedHandSizes.size()) {
            throw new IllegalStateException(
                    String.format("玩家数量不匹配: 实际%d, 期望%d",
                            hands.size(), expectedHandSizes.size()));
        }

        for (int i = 0; i < hands.size(); i++) {
            int actual = hands.get(i).size();
            int expected = expectedHandSizes.get(i);
            if (actual != expected) {
                throw new IllegalStateException(
                        String.format("玩家%d手牌数量错误: 实际%d, 期望%d",
                                i, actual, expected));
            }
        }
    }

    @Override
    public String getName() {
        return "HandSizeValidator";
    }
}
