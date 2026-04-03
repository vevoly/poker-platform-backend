package com.pokergame.common.rule;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 规则配置 - 纯数据DTO，无状态
 *
 * @author poker-platform
 */
@Data
@Builder
public class RuleConfig {

    /** 是否允许首出炸弹 */
    @Builder.Default
    private boolean allowFirstPlayBomb = false;

    /** 炸弹倍率基数 */
    @Builder.Default
    private int bombMultiplierBase = 2;

    /** 是否启用春天规则 */
    @Builder.Default
    private boolean enableSpringRule = true;

    /** 是否启用反春规则 */
    @Builder.Default
    private boolean enableCounterSpring = true;

    /** 是否允许明牌 */
    @Builder.Default
    private boolean allowShowCard = true;

    /** 托管延迟（秒） */
    @Builder.Default
    private int autoPlayDelay = 15;

    /** 扩展配置 */
    @Builder.Default
    private Map<String, Object> extra = new HashMap<>();

    /**
     * 创建斗地主默认配置
     */
    public static RuleConfig defaultDoudizhuConfig() {
        return RuleConfig.builder()
                .allowFirstPlayBomb(false)
                .bombMultiplierBase(2)
                .enableSpringRule(true)
                .enableCounterSpring(true)
                .allowShowCard(true)
                .autoPlayDelay(15)
                .build();
    }

    /**
     * 创建德州扑克默认配置
     */
    public static RuleConfig defaultTexasConfig() {
        return RuleConfig.builder()
                .allowFirstPlayBomb(true)  // 德州没有首出限制
                .bombMultiplierBase(1)     // 德州没有炸弹倍率
                .enableSpringRule(false)
                .enableCounterSpring(false)
                .allowShowCard(false)
                .autoPlayDelay(20)
                .build();
    }
}
