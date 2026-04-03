package com.pokergame.common.event;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.game.GameType;
import lombok.Getter;

import java.util.List;

/**
 * 游戏动作事件 - 通用动作事件
 *
 * 适用于：出牌、下注等通用动作
 *
 * @author poker-platform
 */
@Getter
public class GameActionEvent extends BaseGameEvent {

    private final long playerId;
    private final String action;        // PLAY, PASS, BET, FOLD等
    private final List<Card> cards;      // 涉及的牌
    private final CardPattern pattern;   // 牌型（如果是出牌）
    private final int betAmount;         // 下注金额（如果是下注）

    public GameActionEvent(GameEventType eventType, GameType gameType,
                           String roomId, long playerId, String action) {
        super(eventType, gameType, roomId);
        this.playerId = playerId;
        this.action = action;
        this.cards = null;
        this.pattern = null;
        this.betAmount = 0;
    }

    public GameActionEvent(GameEventType eventType, GameType gameType,
                           String roomId, long playerId, String action,
                           List<Card> cards, CardPattern pattern) {
        super(eventType, gameType, roomId);
        this.playerId = playerId;
        this.action = action;
        this.cards = cards;
        this.pattern = pattern;
        this.betAmount = 0;
    }

    public GameActionEvent(GameEventType eventType, GameType gameType,
                           String roomId, long playerId, String action, int betAmount) {
        super(eventType, gameType, roomId);
        this.playerId = playerId;
        this.action = action;
        this.cards = null;
        this.pattern = null;
        this.betAmount = betAmount;
    }
}
