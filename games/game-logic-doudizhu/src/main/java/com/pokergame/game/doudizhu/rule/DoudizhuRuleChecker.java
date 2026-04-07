package com.pokergame.game.doudizhu.rule;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.game.GameType;
import com.pokergame.common.pattern.PatternComparator;
import com.pokergame.common.pattern.PatternRecognizer;
import com.pokergame.common.pattern.PatternRecognizerFactory;
import com.pokergame.common.pattern.PatternResult;
import com.pokergame.common.rule.ValidationResult;
import com.pokergame.core.base.BaseRuleChecker;
import com.pokergame.core.exception.GameCode;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 斗地主规则执行器
 *
 * 继承 BaseRuleChecker，只需实现斗地主特有的规则
 *
 * @author poker-platform
 */
@Slf4j
public class DoudizhuRuleChecker extends BaseRuleChecker<DoudizhuRoom, DoudizhuPlayer> {

    public DoudizhuRuleChecker(DoudizhuRoom room) {
        super(GameType.DOUDIZHU, room);
    }

    // ==================== 实现抽象方法 ====================

    @Override
    protected DoudizhuPlayer getPlayer(long playerId) {
        return room.getDoudizhuPlayer(playerId);
    }

    @Override
    protected boolean isCurrentPlayer(long playerId) {
        return room.isCurrentPlayer(playerId);
    }

    @Override
    protected List<Card> getLastPlayCards() {
        return room.getLastPlayCards();
    }

    @Override
    protected List<Card> getHandCards(DoudizhuPlayer player) {
        return player.getHandCards();
    }

    // ==================== 实现钩子方法 ====================

    /**
     * 首出校验：斗地主首出不能出炸弹
     */
    @Override
    protected ValidationResult validateFirstPlay(PatternResult current, List<Card> cards) {
        if (current.getPattern() == CardPattern.BOMB ||
                current.getPattern() == CardPattern.ROCKET) {
            return ValidationResult.failure(GameCode.FIRST_PLAY_NO_BOMB.getCode(), GameCode.FIRST_PLAY_NO_BOMB.getMsg());
        }
        return ValidationResult.success(current.getPattern(), current.getMainRank(), current.getSubRank());
    }

    /**
     * 校验后处理：记录炸弹倍率
     */
    @Override
    protected ValidationResult afterValidate(PatternResult current, List<Card> cards) {
        if (current.getPattern() == CardPattern.BOMB ||
                current.getPattern() == CardPattern.ROCKET) {
            room.addBomb();
            log.debug("炸弹触发，当前倍率: {}", room.getMultiplier());
        }
        return ValidationResult.success(current.getPattern(), current.getMainRank(), current.getSubRank());
    }
}
