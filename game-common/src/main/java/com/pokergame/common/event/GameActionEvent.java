package com.pokergame.common.event;

import com.pokergame.common.card.CardPattern;
import com.pokergame.common.enums.GameActionType;
import com.pokergame.common.enums.GameEventType;
import com.pokergame.common.game.GameType;
import com.pokergame.common.card.CardDTO;
import lombok.Getter;

import java.util.List;

/**
 * 通用动作事件
 * 适用于出牌、下注、跟注等动作
 * 对于不同游戏，通过 eventType 区分具体含义
 */
@Getter
public class GameActionEvent extends BaseGameEvent {

    /** 执行动作的玩家ID */
    private final long playerId;
    /** 动作类型字符串（如 "PLAY", "BET", "FOLD", "RAISE"） */
    private final GameActionType action;        // PLAY, PASS, BET, FOLD等
    /** 涉及的牌（如果是出牌） */
    private final List<CardDTO> cards;      // 涉及的牌
    /** 牌型（如果是出牌） */
    private final CardPattern pattern;   // 牌型（如果是出牌）
    /** 下注金额（如果是下注动作） */
    private final int betAmount;         // 下注金额（如果是下注）

    /**
     * 构造简单动作（如过牌、弃牌）
     */
    public GameActionEvent(GameEventType eventType, GameType gameType,
                           String roomId, long playerId, GameActionType action) {
        super(eventType, gameType, roomId);
        this.playerId = playerId;
        this.action = action;
        this.cards = null;
        this.pattern = null;
        this.betAmount = 0;
    }

    /**
     * 构造出牌动作
     */
    public GameActionEvent(GameEventType eventType, GameType gameType,
                           String roomId, long playerId, GameActionType action,
                           List<CardDTO> cards, CardPattern pattern) {
        super(eventType, gameType, roomId);
        this.playerId = playerId;
        this.action = action;
        this.cards = cards;
        this.pattern = pattern;
        this.betAmount = 0;
    }

    /**
     * 构造下注动作
     */
    public GameActionEvent(GameEventType eventType, GameType gameType,
                           String roomId, long playerId, GameActionType action, int betAmount) {
        super(eventType, gameType, roomId);
        this.playerId = playerId;
        this.action = action;
        this.cards = null;
        this.pattern = null;
        this.betAmount = betAmount;
    }
}
