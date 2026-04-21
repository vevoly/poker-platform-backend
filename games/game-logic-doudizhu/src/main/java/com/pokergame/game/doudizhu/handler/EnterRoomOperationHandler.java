package com.pokergame.game.doudizhu.handler;


import com.iohao.game.widget.light.room.operation.OperationHandler;
import com.iohao.game.widget.light.room.operation.PlayerOperationContext;
import com.pokergame.common.exception.GameCode;
import com.pokergame.game.doudizhu.broadcast.DoudizhuBroadcastKit;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import lombok.extern.slf4j.Slf4j;

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
//        DoudizhuRoom room = context.getRoom();
//        long userId = context.getUserId();
//
//        // 检查玩家是否已在房间中（如果已在，说明是重复触发，应当拒绝）
//        DoudizhuPlayer existingPlayer = room.getDoudizhuPlayer(userId);
//        GameCode.PLAYER_ALREADY_IN_ROOM.assertTrueThrows(existingPlayer != null);

        return true;
    }

    @Override
    public void process(PlayerOperationContext context) {
        long userId = context.getUserId();
        DoudizhuRoom room = context.getRoom();

        DoudizhuPlayer player = room.getDoudizhuPlayer(userId);
        // 广播玩家进入房间
        DoudizhuBroadcastKit.broadcastEnterRoom(player, room);
    }
}
