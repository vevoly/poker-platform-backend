package com.pokergame.core.base;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.pattern.PatternComparator;
import com.pokergame.common.pattern.PatternRecognizer;
import com.pokergame.common.pattern.PatternRecognizerFactory;
import com.pokergame.common.pattern.PatternResult;
import com.pokergame.common.rule.ValidationResult;
import com.pokergame.common.game.GameType;
import com.pokergame.common.exception.GameCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 规则检查器基类
 *
 * 封装了通用的出牌校验逻辑：
 * 1. 回合校验
 * 2. 手牌完整性校验
 * 3. 牌型识别（调用 game-common）
 * 4. 大小比较（调用 game-common）
 *
 * 子类只需实现：
 * - getPlayer()：获取玩家
 * - isCurrentPlayer()：判断是否为当前玩家
 * - getLastPlayCards()：获取上一手出的牌
 * - getHandCards()：获取玩家手牌
 * - validateFirstPlay()：首出特殊规则
 * - afterValidate()：校验后处理（如炸弹倍率）
 *
 * @param <R> 房间类型
 * @param <P> 玩家类型
 * @author poker-platform
 */
@Slf4j
public abstract class BaseRuleChecker<R, P> {

    protected final GameType gameType;
    protected final R room;
    protected final PatternRecognizer recognizer;

    public BaseRuleChecker(GameType gameType, R room) {
        this.gameType = gameType;
        this.room = room;
        this.recognizer = PatternRecognizerFactory.get(gameType);
    }

    // ==================== 抽象方法（子类必须实现） ====================

    /**
     * 获取玩家
     */
    protected abstract P getPlayer(long playerId);

    /**
     * 判断是否为当前玩家
     */
    protected abstract boolean isCurrentPlayer(long playerId);

    /**
     * 获取上一手出的牌（可能为 null）
     */
    protected abstract List<Card> getLastPlayCards();

    /**
     * 获取玩家手牌
     */
    protected abstract List<Card> getHandCards(P player);

    /**
     * 首出校验（子类实现）
     *
     * @param current 当前牌型
     * @param cards 出的牌
     * @return 校验结果
     */
    protected abstract ValidationResult validateFirstPlay(PatternResult current, List<Card> cards);

    /**
     * 校验后处理（子类实现）
     * 用于处理炸弹倍率、特殊事件等
     *
     * @param current 当前牌型
     * @param cards 出的牌
     * @return 校验结果
     */
    protected abstract ValidationResult afterValidate(PatternResult current, List<Card> cards);

    // ==================== 模板方法 ====================

    /**
     * 校验出牌（模板方法）
     *
     * @param playerId 玩家ID
     * @param cards 出的牌
     * @return 校验结果
     */
    public final ValidationResult validatePlay(long playerId, List<Card> cards) {
        // 1. 回合校验
        if (!isCurrentPlayer(playerId)) {
            return ValidationResult.failure(GameCode.NOT_YOUR_TURN.getCode(), GameCode.NOT_YOUR_TURN.getMsg());
        }

        // 2. 手牌完整性校验
        P player = getPlayer(playerId);
        if (player == null) {
            return ValidationResult.failure(GameCode.PLAYER_NOT_IN_ROOM.getCode(), GameCode.PLAYER_NOT_IN_ROOM.getMsg());
        }
        if (!hasCardsInHand(player, cards)) {
            return ValidationResult.failure(GameCode.CARDS_NOT_IN_HAND.getCode(), GameCode.CARDS_NOT_IN_HAND.getMsg());
        }

        // 3. 牌型识别（无状态 - 调用 game-common）
        PatternResult current = recognizer.recognize(cards);
        if (current == null || current.getPattern() == CardPattern.PASS) {
            return ValidationResult.failure(GameCode.INVALID_PATTERN.getCode(), GameCode.INVALID_PATTERN.getMsg());
        }

        // 4. 首出校验
        if (isFirstPlay()) {
            return validateFirstPlay(current, cards);
        }

        // 5. 上家牌型识别（无状态 - 调用 game-common）
        List<Card> lastPlayCards = getLastPlayCards();
        if (lastPlayCards == null || lastPlayCards.isEmpty()) {
            // 没有上家牌，视为首出
            return validateFirstPlay(current, cards);
        }

        PatternResult last = recognizer.recognize(lastPlayCards);
        if (last == null) {
            return ValidationResult.failure(GameCode.INVALID_PREVIOUS_PATTERN.getCode(), GameCode.INVALID_PREVIOUS_PATTERN.getMsg());
        }

        // 6. 大小比较（无状态 - 调用 game-common）
        boolean canBeat = PatternComparator.canBeat(
                last.getPattern(), last.getMainRank(),
                current.getPattern(), current.getMainRank()
        );

        if (!canBeat) {
            return ValidationResult.failure(GameCode.CANNOT_BEAT.getCode(), GameCode.CANNOT_BEAT.getMsg());
        }

        // 7. 子类钩子方法
        return afterValidate(current, cards);
    }

    // ==================== 辅助方法（子类可重写） ====================

    /**
     * 判断是否为首出（子类可重写）
     */
    protected boolean isFirstPlay() {
        List<Card> lastPlayCards = getLastPlayCards();
        return lastPlayCards == null || lastPlayCards.isEmpty();
    }

    // ==================== 通用工具方法 ====================

    /**
     * 检查手牌是否包含要出的牌
     */
    protected boolean hasCardsInHand(P player, List<Card> cards) {
        Map<Integer, Long> handCount = getHandCardMap(player);
        Map<Integer, Long> playCount = cards.stream()
                .collect(Collectors.groupingBy(Card::getId, Collectors.counting()));

        for (Map.Entry<Integer, Long> entry : playCount.entrySet()) {
            int cardId = entry.getKey();
            long needCount = entry.getValue();
            long haveCount = handCount.getOrDefault(cardId, 0L);
            if (haveCount < needCount) {
                log.debug("手牌不足: cardId={}, need={}, have={}", cardId, needCount, haveCount);
                return false;
            }
        }
        return true;
    }

    /**
     * 获取手牌映射（子类可重写）
     */
    protected Map<Integer, Long> getHandCardMap(P player) {
        return getHandCards(player).stream()
                .collect(Collectors.groupingBy(Card::getId, Collectors.counting()));
    }
}