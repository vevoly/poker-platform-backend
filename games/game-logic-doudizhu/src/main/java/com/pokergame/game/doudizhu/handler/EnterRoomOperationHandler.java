package com.pokergame.game.doudizhu.handler;


import com.iohao.game.widget.light.room.operation.OperationHandler;
import com.iohao.game.widget.light.room.operation.PlayerOperationContext;
import com.pokergame.core.exception.GameCode;
import com.pokergame.game.doudizhu.broadcast.DoudizhuBroadcastKit;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 进入房间操作处理器
 *
 * @author poker-platform
 */
@Slf4j
public final class EnterRoomOperationHandler implements OperationHandler {

    private final DoudizhuRoomService roomService = DoudizhuRoomService.me();

    @Override
    public boolean processVerify(PlayerOperationContext context) {
        DoudizhuRoom room = context.getRoom();

        // 检查房间是否有空位
        GameCode.ROOM_FULL.assertTrueThrows(room.isFull());

        return true;
    }

    @Override
    public void process(PlayerOperationContext context) {
        long userId = context.getUserId();
        DoudizhuRoom room = context.getRoom();

        log.info("玩家 {} 进入房间 {}", userId, room.getRoomId());

        // 检查玩家是否已在房间中
        DoudizhuPlayer player = room.getDoudizhuPlayer(userId);
        if (player == null) {
            player = new DoudizhuPlayer();
            player.setUserId(userId);
            player.setNickname("玩家" + userId); // TODO: 从用户服获取昵称

            room.addDoudizhuPlayer(player);
            roomService.addPlayer(room, player);
        }

        // 广播玩家进入房间
        DoudizhuBroadcastKit.broadcastEnterRoom(player, room);
    }
}
