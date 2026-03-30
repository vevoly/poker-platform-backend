package com.pokergame.common.game;

import com.pokergame.common.card.Card;
import com.pokergame.common.pattern.PatternRecognizer;
import com.pokergame.common.pattern.PatternResult;

import java.util.List;

/**
 * 游戏逻辑接口
 * 每种游戏实现自己的核心逻辑
 *
 * @author poker-platform
 */
public interface GameLogic {

    /**
     * 获取游戏类型
     */
    GameType getGameType();

    /**
     * 获取牌型识别器
     */
    PatternRecognizer getPatternRecognizer();

    /**
     * 初始化游戏（发牌、设置庄家等）
     */
    GameContext initGame(GameContext context);

    /**
     * 验证出牌是否合法
     * @param context 游戏上下文
     * @param playerId 玩家ID
     * @param cards 出的牌
     * @param lastPattern 上一手牌型（用于压牌）
     * @return 是否合法
     */
    boolean isValidPlay(GameContext context, long playerId, List<Card> cards, PatternResult lastPattern);

    /**
     * 执行出牌
     */
    GameContext playCard(GameContext context, long playerId, List<Card> cards);

    /**
     * 检查游戏是否结束
     */
    boolean isGameOver(GameContext context);

    /**
     * 计算游戏结果（赢家、分数变化等）
     */
    GameResult calculateResult(GameContext context);

    /**
     * 获取AI出牌（托管/机器人）
     */
    List<Card> getAIPlay(GameContext context, long playerId);

    /**
     * 获取当前回合的玩家ID
     */
    long getCurrentPlayerId(GameContext context);

    /**
     * 跳过当前玩家（如不能出牌时）
     */
    GameContext skipPlayer(GameContext context, long playerId);
}
