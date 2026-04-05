package com.pokergame.game.doudizhu.config;

import com.iohao.game.action.skeleton.core.BarSkeleton;
import com.iohao.game.action.skeleton.core.runner.Runner;
import com.pokergame.game.doudizhu.handler.*;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import com.pokergame.game.doudizhu.enums.InternalOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 斗地主操作配置 Runner
 *
 * 只注册简单状态变更的操作处理器
 * 出牌等复杂操作直接在 Action 中处理
 *
 * @author poker-platform
 */
@Slf4j
public final class DoudizhuOperationConfigRunner implements Runner {

    @Override
    public void onStart(BarSkeleton skeleton) {
        var factory = DoudizhuRoomService.me().getOperationFactory();

        // 注册房间操作处理器
        factory.mapping(InternalOperation.ENTER_ROOM, new EnterRoomOperationHandler());
        factory.mapping(InternalOperation.QUIT_ROOM, new QuitRoomOperationHandler());

        // 注册游戏操作处理器（简单状态变更）
        factory.mapping(InternalOperation.READY, new ReadyOperationHandler());
        factory.mapping(InternalOperation.START_GAME, new StartGameOperationHandler());

        // 注意：PLAY_CARD、PASS、GRAB_LANDLORD、NOT_GRAB 在 Action 层直接处理
        // 不需要注册 OperationHandler

        log.info("斗地主操作处理器注册完成");
    }
}
