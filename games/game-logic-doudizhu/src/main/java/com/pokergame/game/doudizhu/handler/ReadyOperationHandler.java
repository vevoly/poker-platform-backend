package com.pokergame.game.doudizhu.handler;


import com.iohao.game.widget.light.room.operation.OperationHandler;
import com.iohao.game.widget.light.room.operation.PlayerOperationContext;
import com.pokergame.common.exception.GameCode;
import com.pokergame.game.doudizhu.broadcast.DoudizhuBroadcastKit;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.enums.InternalOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 准备操作处理器
 *
 * 处理玩家准备/取消准备操作
 *
 * 流程：
 * 1. 校验：游戏必须未开始
 * 2. 更新玩家准备状态
 * 3. 广播准备状态
 * 4. 如果所有玩家都准备，自动开始游戏
 *
 * @author poker-platform
 */
@Slf4j
public final class ReadyOperationHandler implements OperationHandler {

    @Override
    public boolean processVerify(PlayerOperationContext context) {
        DoudizhuRoom room = context.getRoom();
        DoudizhuGameStatus gameStatus = room.getGameStatus();

        // 游戏未开始时才能准备/取消准备
        boolean canReady = gameStatus == DoudizhuGameStatus.WAITING || gameStatus == DoudizhuGameStatus.READY;
        GameCode.ILLEGAL_OPERATION.assertTrueThrows(!canReady);
        return true;
    }

    @Override
    public void process(PlayerOperationContext context) {
        DoudizhuRoom room = context.getRoom();
        DoudizhuPlayer player = context.getPlayer();

        // 获取准备状态（通过 getCommand() 获取 boolean 值）
        boolean ready = context.getCommand();
        player.setReady(ready);

        log.info("玩家 {} 准备状态: {}", player.getUserId(), ready);

        // 广播准备状态
        DoudizhuBroadcastKit.broadcastReady(player, ready, room);

        // 如果所有玩家都准备好了，自动开始游戏
        if (ready && room.isAllReady() && room.getGameStatus() == DoudizhuGameStatus.READY) {
            log.info("房间 {} 所有玩家已准备，自动开始游戏", room.getRoomId());
            room.operation(InternalOperation.START_GAME);
        }
    }
}