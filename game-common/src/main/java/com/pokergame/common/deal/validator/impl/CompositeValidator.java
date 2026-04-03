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

    // ==================== 静态工厂方法 ====================

    /**
     * 创建斗地主默认验证器
     * @param playerCount 玩家数量（2或3）
     * @param landlordIndex 地主索引
     */
    public static CompositeValidator createDoudizhuValidator(int playerCount, int landlordIndex) {
        // 计算斗地主总牌数：农民17张 × (玩家数-1) + 地主20张
        int totalCards = 17 * (playerCount - 1) + 20;

        // 构建手牌大小列表用于验证
        List<Integer> expectedHandSizes = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            if (i == landlordIndex) {
                expectedHandSizes.add(20);
            } else {
                expectedHandSizes.add(17);
            }
        }

        return new CompositeValidator()
                .addValidator(new UniquenessValidator())
                .addValidator(new RankLimitValidator())
                .addValidator(new CardCountValidator(totalCards))
                .addValidator(new HandSizeValidator(expectedHandSizes));
    }

    /**
     * 创建德州扑克默认验证器
     * @param playerCount 玩家数量（2-9）
     */
    public static CompositeValidator createTexasValidator(int playerCount) {
        int totalCards = playerCount * 2;  // 只验证手牌，公共牌单独验证
        return new CompositeValidator()
                .addValidator(new UniquenessValidator())
                .addValidator(new RankLimitValidator())
                .addValidator(new CardCountValidator(totalCards));
    }

    /**
     * 创建牛牛默认验证器
     * @param playerCount 玩家数量（2-6）
     */
    public static CompositeValidator createBullValidator(int playerCount) {
        return new CompositeValidator()
                .addValidator(new UniquenessValidator())
                .addValidator(new RankLimitValidator())
                .addValidator(new CardCountValidator(playerCount * 5));
    }

    /**
     * 创建基础验证器（只验证牌唯一性和牌值限制）
     */
    public static CompositeValidator createBasicValidator() {
        return new CompositeValidator()
                .addValidator(new UniquenessValidator())
                .addValidator(new RankLimitValidator());
    }
}
