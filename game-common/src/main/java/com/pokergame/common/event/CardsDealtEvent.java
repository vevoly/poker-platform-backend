package com.pokergame.common.event;

import com.pokergame.common.enums.GameEventType;
import com.pokergame.common.game.GameType;
import com.pokergame.common.card.CardDTO;
import lombok.Getter;

import java.util.List;

/**
 * 发牌事件
 * 发布时机：游戏开始发牌或补牌时
 * 注意：仅包含机器人自己的手牌（其他玩家手牌不可见）
 */
@Getter
public class CardsDealtEvent extends BaseGameEvent {
    /** 收到手牌的玩家ID */
    private final long playerId;
    /** 手牌列表 */
    private final List<CardDTO> handCards;

    public CardsDealtEvent(GameType gameType, String roomId, long playerId, List<CardDTO> handCards) {
        super(GameEventType.CARDS_DEALT, gameType, roomId);
        this.playerId = playerId;
        this.handCards = handCards;
    }
}
