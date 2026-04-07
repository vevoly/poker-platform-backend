package com.pokergame.game.doudizhu.bidding;

import lombok.Data;
import lombok.Getter;

/**
 * 叫地主记录
 *
 * @author poker-platform
 */
@Data
public class BidRecord {

    /** 玩家ID */
    private final long userId;

    /** 是否抢地主 */
    @Getter
    private final boolean grab;

    /** 倍数（抢地主时有效） */
    private final int multiple;

    /** 时间戳 */
    private final long timestamp;

    public static BidRecord grab(long userId, int multiple) {
        return new BidRecord(userId, true, multiple, System.currentTimeMillis());
    }

    public static BidRecord notGrab(long userId) {
        return new BidRecord(userId, false, 0, System.currentTimeMillis());
    }

}
