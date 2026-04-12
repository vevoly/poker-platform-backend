package com.pokergame.common.exception;

import com.iohao.game.action.skeleton.core.exception.MsgException;

/**
 * 乐观锁异常
 * 此异常代表乐观锁异常，可以重试，其他异常不重试
 */
public class OptimisticLockException extends MsgException {
    public OptimisticLockException() {
        super(GameCode.CURRENCY_OPERATION_CONFLICT.getCode(), GameCode.CURRENCY_OPERATION_CONFLICT.getMsg());
    }
}
