package com.pokergame.common.exception.result;

import com.pokergame.common.exception.GameCode;
import lombok.Data;

/**
 * 统一响应结果
 * <p>所有 HTTP 接口统一返回此格式
 *
 * @author poker-platform
 */
@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应（有数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(GameCode.SUCCESS.getCode());
        result.setMessage(GameCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    /**
     * 业务错误响应（使用 GameCode）
     */
    public static <T> Result<T> error(GameCode gameCode) {
        Result<T> result = new Result<>();
        result.setCode(gameCode.getCode());
        result.setMessage(gameCode.getMsg());
        return result;
    }

    /**
     * 业务错误响应（使用 GameCode + 自定义消息）
     */
    public static <T> Result<T> error(GameCode gameCode, String customMessage) {
        Result<T> result = new Result<>();
        result.setCode(gameCode.getCode());
        result.setMessage(customMessage);
        return result;
    }

    /**
     * 业务错误响应（使用错误码 + 消息）
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == GameCode.SUCCESS.getCode();
    }
}
