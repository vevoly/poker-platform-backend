package com.pokergame.game.doudizhu.handler;

import com.iohao.game.widget.light.room.operation.OperationHandler;
import com.iohao.game.widget.light.room.operation.PlayerOperationContext;
import com.pokergame.game.doudizhu.broadcast.DoudizhuBroadcastKit;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import lombok.extern.slf4j.Slf4j;

/**
 * 离开房间操作处理器
 *
 * @author poker-platform
 */
@Slf4j
public final class QuitRoomOperationHandler implements OperationHandler {

    private final DoudizhuRoomService roomService = DoudizhuRoomService.me();

    @Override
    public boolean processVerify(PlayerOperationContext context) {
        // 不需要额外校验
        return true;
    }

    @Override
    public void process(PlayerOperationContext context) {
        DoudizhuRoom room = context.getRoom();
        long userId = context.getUserId();

        log.info("玩家 {} 离开房间 {}", userId, room.getRoomId());

        // 移除玩家
        roomService.removePlayer(room, userId);

        // 如果房间空了，销毁房间
        if (room.countPlayer() == 0) {
            roomService.removeRoom(room);
            log.info("房间 {} 已销毁", room.getRoomId());
            return;
        }

        // 广播玩家离开房间
        DoudizhuBroadcastKit.broadcastQuitRoom(userId, room);
    }
}
