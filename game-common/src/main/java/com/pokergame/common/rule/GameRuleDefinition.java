package com.pokergame.common.rule;

import com.pokergame.common.game.GameType;
import lombok.Data;

import java.util.EnumSet;
import java.util.Set;

/**
 * 游戏规则定义 - 无状态
 * 定义每个游戏支持哪些规则类型
 *
 * @author poker-platform
 */
@Data
public class GameRuleDefinition {

    private final GameType gameType;
    private final Set<RuleType> supportedRules;
    private final RuleConfig defaultConfig;

    private GameRuleDefinition(GameType gameType, Set<RuleType> supportedRules, RuleConfig defaultConfig) {
        this.gameType = gameType;
        this.supportedRules = supportedRules;
        this.defaultConfig = defaultConfig;
    }

    /**
     * 创建斗地主规则定义
     */
    public static GameRuleDefinition doudizhu() {
        return new GameRuleDefinition(
                GameType.DOUDIZHU,
                EnumSet.of(
                        RuleType.FIRST_PLAY_LIMIT,
                        RuleType.BOMB_MULTIPLIER,
                        RuleType.SPRING_RULE,
                        RuleType.COUNTER_SPRING_RULE,
                        RuleType.SHOW_CARD_RULE,
                        RuleType.AUTO_PLAY_RULE
                ),
                RuleConfig.defaultDoudizhuConfig()
        );
    }

    /**
     * 创建德州扑克规则定义
     */
    public static GameRuleDefinition texas() {
        return new GameRuleDefinition(
                GameType.TEXAS,
                EnumSet.of(
                        RuleType.AUTO_PLAY_RULE
                ),
                RuleConfig.defaultTexasConfig()
        );
    }

    /**
     * 创建牛牛规则定义
     */
    public static GameRuleDefinition bull() {
        return new GameRuleDefinition(
                GameType.BULL,
                EnumSet.of(
                        RuleType.AUTO_PLAY_RULE
                ),
                RuleConfig.defaultTexasConfig()
        );
    }

    /**
     * 检查是否支持某规则
     */
    public boolean supports(RuleType ruleType) {
        return supportedRules.contains(ruleType);
    }
}
