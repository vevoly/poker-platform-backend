package com.pokergame.core.exception;

import com.iohao.game.action.skeleton.core.exception.MsgExceptionInfo;

/**
 * 定义业务错误码枚举
 *
 */
public enum GameCodeEnum implements MsgExceptionInfo {

    SUCCESS(0, "成功"),

    // 房间错误码 10000-19999
    ROOM_NOT_FOUND(10001, "房间不存在"),
    ROOM_FULL(10002, "房间已满"),
    ROOM_ALREADY_STARTED(10003, "游戏已开始"),

    // 玩家错误码 20000-29999
    PLAYER_NOT_IN_ROOM(20001, "玩家不在房间中"),
    NOT_YOUR_TURN(20002, "不是你的回合"),

    // 出牌错误码 30000-39999
    INVALID_PATTERN(30001, "无效的牌型"),
    CANNOT_BEAT(30002, "不能压过上家的牌");

    private final int code;
    private final String message;

    GameCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMsg() {
        return message;
    }

    @Override
    public int getCode() {
        return code;
    }

}

