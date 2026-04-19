package com.pokergame.common.event;

import com.pokergame.common.enums.GameEventType;
import com.pokergame.common.game.GameType;
import lombok.Getter;

/**
 * 回合切换事件
 * 发布时机：轮到下一个玩家行动时
 * 机器人服务收到此事件后，若当前玩家是机器人，应触发决策引擎
 */
@Getter
public class TurnChangedEvent extends BaseGameEvent {
    /** 当前需要行动的玩家ID */
    private final long currentPlayerId;
    /** 回合超时时间（秒），0表示无超时 */
    private final int timeoutSeconds;

    public TurnChangedEvent(GameType gameType, String roomId, long currentPlayerId, int timeoutSeconds) {
        super(GameEventType.TURN_CHANGE, gameType, roomId);
        this.currentPlayerId = currentPlayerId;
        this.timeoutSeconds = timeoutSeconds;
    }
}
