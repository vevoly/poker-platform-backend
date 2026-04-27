package com.pokergame.game.doudizhu.trustee;

import com.pokergame.core.base.BaseTrusteeshipManager;
import com.pokergame.game.doudizhu.broadcast.DoudizhuBroadcastKit;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import lombok.extern.slf4j.Slf4j;

/**
 * 斗地主托管管理器
 */
@Slf4j
public class DoudizhuTrusteeshipManager extends BaseTrusteeshipManager<DoudizhuRoom, DoudizhuPlayer> {

    public DoudizhuTrusteeshipManager(DoudizhuRoom room) {
        super(room, new DoudizhuTrusteeshipDecision());
    }

    @Override
    protected void onTrusteeStart(long userId) {
        log.info("玩家 {} 进入托管模式，房间 {}", userId, room.getRoomId());
        // 广播托管状态变化（需要实现广播工具类）
         DoudizhuBroadcastKit.broadcastTrusteeshipChange(userId, true, room);
    }

    @Override
    protected void onTrusteeEnd(long userId) {
        log.info("玩家 {} 退出托管模式，房间 {}", userId, room.getRoomId());
         DoudizhuBroadcastKit.broadcastTrusteeshipChange(userId, false, room);
    }

    @Override
    public void autoAct(long userId) {
        if (!isTrustee(userId)) return;
        // 确保当前是托管玩家的回合
        if (!room.isCurrentPlayer(userId)) return;
        // 决策并执行
        decision.act(room, getPlayer(userId));
    }

    @Override
    protected DoudizhuPlayer getPlayer(long userId) {
        return room.getDoudizhuPlayer(userId);
    }

    @Override
    protected boolean isCurrentTurn(DoudizhuPlayer player) {
        return room.isCurrentPlayer(player.getUserId());
    }
}

