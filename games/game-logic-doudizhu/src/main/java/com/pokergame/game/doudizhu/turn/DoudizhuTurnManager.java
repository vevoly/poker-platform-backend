package com.pokergame.game.doudizhu.turn;

import com.pokergame.core.base.BaseTurnManager;
import com.pokergame.game.doudizhu.bidding.BiddingManager;
import com.pokergame.game.doudizhu.enums.InternalOperation;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.state.DoudizhuGameStateManager;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 斗地主回合管理器
 *
 * 继承 BaseTurnManager，实现斗地主特有的超时处理逻辑
 *
 * @author poker-platform
 */
@Slf4j
public class DoudizhuTurnManager extends BaseTurnManager<DoudizhuRoom> {

    /** 叫地主管理器引用（用于超时回调） */
    @Setter
    private BiddingManager biddingManager;

    public DoudizhuTurnManager(DoudizhuRoom room) {
        super(room);
    }

    @Override
    protected void onTimeout() {
        log.info("房间 {} 玩家 {} 出牌超时", room.getRoomId(), room.getCurrentPlayer());
        // 超时自动过牌
        room.operation(InternalOperation.PASS);
    }

    @Override
    protected String generateTaskId() {
        return "doudizhu_timeout_" + room.getRoomId() + "_" + System.currentTimeMillis();
    }
}
