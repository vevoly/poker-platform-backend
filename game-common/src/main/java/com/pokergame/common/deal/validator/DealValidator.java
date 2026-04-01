package com.pokergame.common.deal.validator;

import com.pokergame.common.card.Card;

import java.util.List;

/**
 * 发牌验证器接口
 * 职责：验证发牌结果的合法性
 *
 * @author poker-platform
 */
public interface DealValidator {

    /**
     * 验证发牌结果
     * @param hands 所有玩家的手牌
     * @throws IllegalStateException 验证失败时抛出异常
     */
    void validate(List<List<Card>> hands);

    /**
     * 获取验证器名称
     */
    String getName();
}
