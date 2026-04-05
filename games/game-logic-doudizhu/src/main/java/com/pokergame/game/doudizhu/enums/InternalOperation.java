package com.pokergame.game.doudizhu.enums;

import com.iohao.game.common.kit.OperationCode;
import lombok.Getter;

/**
 * 斗地主内部操作枚举
 *
 * 定义房间模块内部支持的操作类型
 * 用于 OperationFactory 注册 Handler
 *
 * @author poker-platform
 */
@Getter
public enum InternalOperation implements OperationCode {

    /** 玩家准备 */
    READY,

    /** 开始游戏 */
    START_GAME,

    /** 进入房间 */
    ENTER_ROOM,

    /** 离开房间 */
    QUIT_ROOM,

    /** 出牌 */
    PLAY_CARD,

    /** 过牌 */
    PASS,

    /** 抢地主 */
    GRAB_LANDLORD,

    /** 不抢地主 */
    NOT_GRAB;

    final int operationCode;

    InternalOperation() {
        this.operationCode = OperationCode.getAndIncrementCode();
    }

}
