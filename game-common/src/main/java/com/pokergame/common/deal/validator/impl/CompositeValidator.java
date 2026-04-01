package com.pokergame.common.deal.validator.impl;

import com.pokergame.common.card.Card;
import com.pokergame.common.deal.validator.DealValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * 组合验证器
 * 组合多个验证器依次执行
 *
 * @author poker-platform
 */
public class CompositeValidator implements DealValidator {

    private final List<DealValidator> validators = new ArrayList<>();

    public CompositeValidator addValidator(DealValidator validator) {
        validators.add(validator);
        return this;
    }

    @Override
    public void validate(List<List<Card>> hands) {
        for (DealValidator validator : validators) {
            validator.validate(hands);
        }
    }

    @Override
    public String getName() {
        return "CompositeValidator";
    }

    /**
     * 创建斗地主默认验证器
     */
    public static CompositeValidator createDoudizhuValidator() {
        return new CompositeValidator()
                .addValidator(new UniquenessValidator())
                .addValidator(new RankLimitValidator())
                .addValidator(new CardCountValidator(54));  // 斗地主54张
    }

    /**
     * 创建德州扑克默认验证器
     */
    public static CompositeValidator createTexasValidator(int playerCount) {
        int totalCards = playerCount * 2 + 5;  // 手牌 + 公共牌
        return new CompositeValidator()
                .addValidator(new UniquenessValidator())
                .addValidator(new RankLimitValidator())
                .addValidator(new CardCountValidator(totalCards));
    }

    /**
     * 创建牛牛默认验证器
     */
    public static CompositeValidator createBullValidator(int playerCount) {
        return new CompositeValidator()
                .addValidator(new UniquenessValidator())
                .addValidator(new RankLimitValidator())
                .addValidator(new CardCountValidator(playerCount * 5));
    }
}
