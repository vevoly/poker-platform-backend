package com.pokergame.common.rule;

import com.pokergame.common.card.CardPattern;
import lombok.Data;

/**
 * 出牌校验结果 - 纯数据DTO，无状态
 *
 * @author poker-platform
 */
@Data
public class ValidationResult {

    /** 是否合法 */
    private boolean valid;

    /** 牌型（如果合法） */
    private CardPattern pattern;

    /** 主牌值 */
    private int mainRank;

    /** 副牌值 */
    private int subRank;

    /** 错误码 */
    private int errorCode;

    /** 错误信息 */
    private String errorMessage;

    /** 是否炸弹（用于倍率计算） */
    private boolean isBomb;

    /** 是否火箭 */
    private boolean isRocket;

    private ValidationResult() {}

    public static ValidationResult success(CardPattern pattern, int mainRank, int subRank) {
        ValidationResult result = new ValidationResult();
        result.valid = true;
        result.pattern = pattern;
        result.mainRank = mainRank;
        result.subRank = subRank;
        result.isBomb = (pattern == CardPattern.BOMB);
        result.isRocket = (pattern == CardPattern.ROCKET);
        return result;
    }

    public static ValidationResult failure(int errorCode, String errorMessage) {
        ValidationResult result = new ValidationResult();
        result.valid = false;
        result.errorCode = errorCode;
        result.errorMessage = errorMessage;
        return result;
    }

    /**
     * 判断是否为炸弹或火箭（用于倍率计算）
     */
    public boolean isBombOrRocket() {
        return isBomb || isRocket;
    }
}
