package com.pokergame.game.doudizhu.enums;

import lombok.Getter;

/**
 * 叫地主状态枚举
 *
 * @author poker-platform
 */
@Getter
public enum BiddingState {

    /** 等待当前玩家叫地主 */
    WAITING(0, "等待叫地主"),

    /** 已记录，等待下一个玩家 */
    RECORDED(1, "已记录"),

    /** 一轮结束，准备下一轮 */
    ROUND_END(2, "一轮结束"),

    /** 所有玩家都不抢 */
    ALL_PASS(3, "全都不抢"),

    /** 地主已确定 */
    LANDLORD_DETERMINED(4, "地主已确定");

    private final int code;
    private final String desc;

    BiddingState(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
