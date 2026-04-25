package com.pokergame.game.doudizhu.handler;

import com.iohao.game.widget.light.room.operation.OperationHandler;
import com.iohao.game.widget.light.room.operation.PlayerOperationContext;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import lombok.extern.slf4j.Slf4j;

/**
 * 过牌处理器
 */
@Slf4j
public class PassOperationHandler implements OperationHandler {
    @Override
    public void process(PlayerOperationContext context) {
        DoudizhuRoom room = context.getRoom();
        long userId = room.getCurrentPlayer().getUserId();

        // 切换到下一个玩家
        room.nextTurn();

        // 重置超时定时器
        if (room.getTurnManager() != null) {
            room.getTurnManager().resetTimeout();
        }
        log.info("玩家 {} 超时过牌 {}", userId, room.getRoomId());
    }
}
