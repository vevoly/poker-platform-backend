package com.pokergame.core.temp;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;

import java.util.List;
/**
 * 玩家出牌事件
 */
public class PlayerPlayEvent extends GameEvent {

    /** 玩家ID */
    private final long playerId;
    /** 玩家出牌 */
    private final List<Card> cards;
    /** 牌型 */
    private final CardPattern pattern;

    public PlayerPlayEvent(String roomId, long playerId, List<Card> cards, CardPattern pattern) {
        super(GameEventType.PLAYER_PLAY, roomId, cards);
        this.playerId = playerId;
        this.cards = cards;
        this.pattern = pattern;
    }
}
