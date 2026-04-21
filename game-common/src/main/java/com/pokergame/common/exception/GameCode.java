package com.pokergame.common.exception;

import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.iohao.game.action.skeleton.core.exception.MsgExceptionInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 游戏错误码枚举
 *
 * 错误码格式：XYYZZZ（5-6位数字）
 * - X: 大类（1-9）
 * - YY: 子类（01-99）
 * - ZZZ: 具体错误（001-999）
 *
 * 大类分配：
 * - 1: 系统错误
 * - 2: 业务错误（用户、房间、联盟、道具等）
 * - 3: 游戏错误
 * - 4-9: 预留扩展
 *
 * 子类分配（大类=2）：
 * - 01: 用户/认证
 * - 02: 房间
 * - 03: 联盟
 * - 04: 道具
 * - 05: 支付
 *
 * 子类分配（大类=3）：
 * - 00: 游戏通用
 * - 01: 斗地主
 * - 02: 德州
 * - 03: 牛牛
 * - 04: 麻将
 * - 05: 炸金花
 *
 * @author poker-platform
 */
@AllArgsConstructor
public enum GameCode implements MsgExceptionInfo {

    // ========== 成功 ==========
    SUCCESS(200, "成功"),

    // ========== 系统错误 (1xxxx) ==========
    SYSTEM_ERROR(100001, "系统繁忙，请稍后重试"),
    DB_ERROR(100002, "数据库错误"),
    NETWORK_ERROR(100003, "网络错误"),
    PARAM_ERROR(100004, "参数错误"),
    ILLEGAL_STATE(100005, "非法状态"),
    ILLEGAL_OPERATION(100006, "非法操作"),
    OPERATION_TOO_FAST(100007, "操作过快，请稍后再试"),

    // ========== 用户/认证错误 (201xxx) ==========
    USER_NOT_FOUND(201001, "用户名或密码错误"), // 用户不存在
    USER_DISABLED(201002, "用户已被禁用"),
    USERNAME_EXISTS(201003, "用户名已存在"),
    PASSWORD_ERROR(201004, "用户名或密码错误"), // 密码错误
    LOGIN_FAILED(201005, "登录失败"),
    NOT_LOGGED_IN(201006, "未登录，请先登录"),
    ACCOUNT_ONLINE(201007, "用户已登录，请勿重复登录"),

    TOKEN_INVALID(201010, "Token无效"),
    TOKEN_EXPIRED(201011, "Token已过期"),
    TOKEN_MISSING(201012, "缺少Token"),
    TOKEN_VERIFY_FAILED(201013, "Token验证失败"),
    REFRESH_TOKEN_FAILED(201014, "刷新Token失败"),

    USER_REGISTER_FAILED(201021, "注册失败，请稍后重试"),
    USER_UPDATE_FAILED(201022, "更新用户信息失败"),
    USERNAME_INVALID(201023, "用户名格式不正确（4-20位字母数字）"),
    PASSWORD_INVALID(201024, "密码格式不正确（6-20位）"),
    NICKNAME_INVALID(201025, "昵称格式不正确（1-20位）"),
    // 手机号/邮箱相关
    MOBILE_INVALID(201031, "手机号格式不正确"),
    EMAIL_INVALID(201032, "邮箱格式不正确"),
    MOBILE_EXISTS(201033, "手机号已被注册"),
    EMAIL_EXISTS(201034, "邮箱已被注册"),
    MOBILE_NOT_BOUND(201035, "手机号未绑定"),
    EMAIL_NOT_BOUND(201036, "邮箱未绑定"),
    USER_CODE_INVALID(201037, "用户编码无效"),

    // ========== 房间错误 (202xxx) ==========
    ROOM_NOT_FOUND(202001, "房间不存在"),
    ROOM_FULL(202002, "房间已满"),
    ROOM_NOT_EXIST(202003, "房间不存在或已销毁"),
    ROOM_ALREADY_STARTED(202004, "游戏已开始，无法加入"),
    ROOM_NOT_ENOUGH_PLAYERS(202005, "房间人数不足，无法开始游戏"),
    ROOM_PLAYER_FULL(202006, "房间人数已满"),
    NOT_ROOM_OWNER(202007, "只有房主可以开始游戏"),
    PLAYER_ALREADY_IN_ROOM(202008, "玩家已在房间中"),
    PLAYER_NOT_IN_ROOM(202009, "玩家不在房间中"),
    PLAYER_NOT_READY(202010, "玩家未准备"),
    NOT_ALL_READY(202011, "有玩家未准备"),

    // ========== 联盟错误 (203xxx) ==========
    ALLIANCE_NOT_FOUND(203001, "联盟不存在"),
    ALLIANCE_ALREADY_JOINED(203002, "已加入联盟"),
    ALLIANCE_NOT_MEMBER(203003, "不是联盟成员"),
    ALLIANCE_FULL(203004, "联盟已满"),
    ALLIANCE_NO_PERMISSION(203005, "无权限操作"),

    // ========== 道具错误 (204xxx) ==========
    ITEM_NOT_FOUND(204001, "道具不存在"),
    ITEM_NOT_ENOUGH(204002, "道具不足"),
    ITEM_CANNOT_USE(204003, "道具无法使用"),

    // ========== 支付错误 (205xxx) ==========
    PAY_ORDER_NOT_FOUND(205001, "订单不存在"),
    PAY_AMOUNT_ERROR(205002, "金额错误"),
    PAY_ALREADY_PROCESSED(205003, "订单已处理"),

    // ========== 货币错误 (206xxx) ==========
    CURRENCY_NOT_FOUND(206001, "货币类型不存在"),
    CURRENCY_NOT_ENOUGH(206002, "货币不足"),
    CURRENCY_INCREASE_FAILED(206003, "增加货币失败"),
    CURRENCY_DECREASE_FAILED(206004, "减少货币失败"),
    CURRENCY_OPERATION_CONFLICT(206005, "货币操作冲突，请重试"),

    // ========== 统计错误 (207xxx) ==========
    USER_STATS_NOT_FOUND(207001, "用户统计信息不存在"),
    USER_STATS_UPDATE_FAILED(207002, "更新统计信息失败"),

    // ========== 游戏通用错误 (300xxx) ==========
    GAME_NOT_STARTED(300001, "游戏未开始"),
    GAME_ALREADY_FINISHED(300002, "游戏已结束"),
    INVALID_GAME_STATE(300003, "游戏状态错误"),
    GAME_NOT_FOUND(300004, "游戏不存在"),
    NOT_YOUR_TURN(300005, "不是你的回合"),
    INVALID_PATTERN(300006, "无效的牌型"),
    CANNOT_BEAT(300007, "不能压过上家的牌"),
    CARDS_NOT_IN_HAND(300008, "手牌中没有这些牌"),
    NO_CARDS_TO_PLAY(300009, "没有能出的牌"),
    INVALID_CARD_COUNT(300010, "出牌数量不正确"),
    FIRST_PLAY_NO_BOMB(300011, "首出不能出炸弹"),
    INVALID_PREVIOUS_PATTERN(300012, "上家牌型无效"),
    DEAL_CARDS_FAILED(300013, "发牌失败"),
    OPERATION_TIMEOUT(300014, "操作超时"),

    // ========== 斗地主错误 (301xxx) ==========
    DOUDIZHU_BIDDING_NOT_INITIALIZED(301001, "叫地主流程未初始化"),
    DOUDIZHU_BIDDING_STATE_ERROR(301002, "叫地主状态错误"),
    DOUDIZHU_NOT_BIDDING_TURN(301003, "不是你的叫地主回合"),
    DOUDIZHU_BIDDING_ALREADY_COMPLETED(301004, "叫地主已结束"),
    DOUDIZHU_INVALID_PLANE(301005, "无效的飞机牌型"),
    DOUDIZHU_INVALID_STRAIGHT_PAIR(301006, "无效的连对牌型"),
    DOUDIZHU_INVALID_FOUR_WITH_WINGS(301007, "无效的四带二牌型"),

    // ========== 德州错误 (302xxx) ==========
    TEXAS_INVALID_BET(302001, "无效的下注"),
    TEXAS_INSUFFICIENT_CHIPS(302002, "筹码不足"),
    TEXAS_ALREADY_FOLDED(302003, "已弃牌"),
    TEXAS_NOT_YOUR_TURN(302004, "不是你的回合"),
    TEXAS_RAISE_TOO_LOW(302005, "加注金额过低"),

    // ========== 牛牛错误 (303xxx) ==========
    NIUNIU_INVALID_BET(303001, "无效的下注"),
    NIUNIU_NO_BULL(303002, "无牛"),

    // ========== 麻将错误 (304xxx) ==========
    MAHJONG_INVALID_DISCARD(304001, "无效的出牌"),
    MAHJONG_CANNOT_CHI(304002, "不能吃"),
    MAHJONG_CANNOT_PENG(304003, "不能碰"),
    MAHJONG_CANNOT_GANG(304004, "不能杠"),
    MAHJONG_CANNOT_HU(304005, "不能胡"),

    // ========== 炸金花错误 (305xxx) ==========
    ZHAJINHUA_INVALID_COMPARE(305001, "无效的比牌"),
    ZHAJINHUA_INSUFFICIENT_CHIPS(305002, "筹码不足");

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

    public static GameCode fromCode(int code) {
        for (GameCode gameCode : values()) {
            if (gameCode.code == code) {
                return gameCode;
            }
        }
        return null;
    }

    public void assertTrueThrows(boolean condition) {
        if (condition) {
            throw new MsgException(this);
        }
    }

    public void assertTrueThrows(boolean condition, String customMessage) {
        if (condition) {
            throw new MsgException(this.getCode(), customMessage);
        }
    }
}

