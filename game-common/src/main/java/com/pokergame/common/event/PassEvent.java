package com.pokergame.common.event;

import com.pokergame.common.enums.GameEventType;
import com.pokergame.common.game.GameType;
import lombok.Getter;

/**
 * 过牌事件
 * 发布时机：玩家选择不出牌（pass）
 */
@Getter
public class PassEvent extends BaseGameEvent {
    /** 执行过牌的玩家ID */
    private final long playerId;

    public PassEvent(GameType gameType, String roomId, long playerId) {
        super(GameEventType.PASS, gameType, roomId);
        this.playerId = playerId;
    }
}
