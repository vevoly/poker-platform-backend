package com.pokergame.game.doudizhu.room;

import com.pokergame.core.base.BasePlayer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 斗地主玩家
 *
 * 继承 BasePlayer，扩展斗地主特有属性
 *
 * @author poker-platform
 */
@Slf4j
@Getter
@Setter
public class DoudizhuPlayer extends BasePlayer {

    /** 叫地主倍数 */
    private int bidMultiple = 0;

    public DoudizhuPlayer() {
        super();
    }

    public DoudizhuPlayer(long userId) {
        super(userId);
    }

    public DoudizhuPlayer(long userId, String nickname) {
        super(userId, nickname);
    }

    @Override
    public void reset() {
        super.reset();
        this.bidMultiple = 0;
        log.debug("斗地主玩家 {} 状态已重置", getUserId());
    }

    @Override
    public String toString() {
        return String.format("DoudizhuPlayer{userId=%d, nickname=%s, cardCount=%d, ready=%s, landlord=%s, bidMultiple=%d}",
                getUserId(), getNickname(), getCardCount(), isReady(), isLandlord(), bidMultiple);
    }
}