package com.pokergame.core.exception;

import com.iohao.game.action.skeleton.core.exception.MsgExceptionInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 游戏错误码枚举
 *
 * 实现 MsgExceptionInfo 接口，支持 ioGame 的断言式异常
 *
 * 错误码分配规则：
 * - 0: 成功
 * - 10000-19999: 房间错误
 * - 20000-29999: 玩家错误
 * - 30000-39999: 出牌错误
 * - 40000-49999: 游戏错误
 * - 50000-59999: 系统错误
 *
 * @author poker-platform
 */
@AllArgsConstructor
public enum GameCode implements MsgExceptionInfo {

    // ========== 成功 ==========
    SUCCESS(0, "成功"),

    // ========== 房间错误 (10000-19999) ==========

    /** 房间不存在 */
    ROOM_NOT_FOUND(10001, "房间不存在"),

    /** 房间已满 */
    ROOM_FULL(10002, "房间已满"),

    /** 游戏已开始 */
    ROOM_ALREADY_STARTED(10003, "游戏已开始，无法加入"),

    /** 房间人数不足 */
    ROOM_NOT_ENOUGH_PLAYERS(10004, "房间人数不足，无法开始游戏"),

    /** 房间人数已满 */
    ROOM_PLAYER_FULL(10005, "房间人数已满"),

    /** 房间不存在或已销毁 */
    ROOM_NOT_EXIST(10006, "房间不存在或已销毁"),

    // ========== 玩家错误 (20000-29999) ==========

    /** 玩家不在房间中 */
    PLAYER_NOT_IN_ROOM(20001, "玩家不在房间中"),

    /** 不是你的回合 */
    NOT_YOUR_TURN(20002, "不是你的回合"),

    /** 玩家未准备 */
    PLAYER_NOT_READY(20003, "玩家未准备"),

    /** 不是房主 */
    NOT_ROOM_OWNER(20004, "只有房主可以开始游戏"),

    /** 非法操作 */
    ILLEGAL_OPERATION(20005, "当前状态下不能执行此操作"),

    /** 玩家已在房间中 */
    PLAYER_ALREADY_IN_ROOM(20006, "玩家已在房间中"),

    // ========== 出牌错误 (30000-39999) ==========

    /** 无效的牌型 */
    INVALID_PATTERN(30001, "无效的牌型"),

    /** 不能压过上家的牌 */
    CANNOT_BEAT(30002, "不能压过上家的牌"),

    /** 手牌中没有这些牌 */
    CARDS_NOT_IN_HAND(30003, "手牌中没有这些牌"),

    /** 没有能出的牌 */
    NO_CARDS_TO_PLAY(30004, "没有能出的牌"),

    /** 出牌数量不正确 */
    INVALID_CARD_COUNT(30005, "出牌数量不正确"),

    /** 首出不能出炸弹 */
    FIRST_PLAY_NO_BOMB(30006, "首出不能出炸弹"),

    // ========== 游戏错误 (40000-49999) ==========

    /** 游戏未开始 */
    GAME_NOT_STARTED(40001, "游戏未开始"),

    /** 游戏已结束 */
    GAME_ALREADY_FINISHED(40002, "游戏已结束"),

    /** 游戏状态错误 */
    INVALID_GAME_STATE(40003, "游戏状态错误"),

    /** 发牌失败 */
    DEAL_CARDS_FAILED(40004, "发牌失败"),

    // ========== 系统错误 (50000-59999) ==========

    /** 系统错误 */
    SYSTEM_ERROR(50001, "系统错误，请稍后重试");

    private final int code;
    private final String message;

    @Override
    public String getMsg() {
        return this.message;
    }

    @Override
    public int getCode() {
        return this.code;
    }

}

