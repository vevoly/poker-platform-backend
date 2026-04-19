package com.pokergame.common.event;

import com.pokergame.common.enums.GameEventType;
import com.pokergame.common.game.GameType;
import lombok.Getter;

/**
 * 叫地主/抢地主事件
 * 发布时机：玩家进行叫分或抢地主时
 */
@Getter
public class BiddingEvent extends BaseGameEvent {
    /** 执行叫地主的玩家ID */
    private final long playerId;
    /** 叫分倍数（1/2/3，0表示不叫） */
    private final int multiple;   // 叫分倍数（1/2/3）

    public BiddingEvent(GameType gameType, String roomId, long playerId, int multiple) {
        super(GameEventType.DOUDIZHU_GRAB_LANDLORD, gameType, roomId);
        this.playerId = playerId;
        this.multiple = multiple;
    }
}
