package com.pokergame.gateway.hall.handler;

import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.pokergame.common.exception.GameCode;
import com.pokergame.common.exception.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * <p>统一处理以下异常：
 * <ul>
 *     <li>业务异常（MsgException）</li>
 *     <li>参数校验异常（@Valid / @Validated）</li>
 *     <li>缺少请求参数异常</li>
 *     <li>系统异常（兜底）</li>
 * </ul>
 *
 * @author poker-platform
 */
@Slf4j
@Order(1)
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 ioGame 业务异常（MsgException）
     * <p>包括 GameCode 断言抛出的异常
     */
    @ExceptionHandler(MsgException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleMsgException(MsgException e) {
        GameCode gameCode = GameCode.fromCode(e.getMsgCode()) == null ? GameCode.SYSTEM_ERROR : GameCode.fromCode(e.getMsgCode());
        log.debug("业务异常: code={}, msg={}", gameCode.getCode(), gameCode.getMsg());
        return Result.error(gameCode.getCode(), gameCode.getMsg());
    }

    /**
     * 处理 @Valid 参数校验异常（RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse(GameCode.PARAM_ERROR.getMsg());
        log.debug("参数校验失败: {}", message);
        return Result.error(GameCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理 @Validated 参数校验异常（RequestParam、PathVariable）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse(GameCode.PARAM_ERROR.getMsg());
        log.debug("参数校验失败: {}", message);
        return Result.error(GameCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse(GameCode.PARAM_ERROR.getMsg());
        log.debug("参数绑定失败: {}", message);
        return Result.error(GameCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        String message = "缺少参数: " + e.getParameterName();
        log.debug(message);
        return Result.error(GameCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理系统异常（兜底）
     * <p>此异常会返回 HTTP 500 状态码
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(GameCode.SYSTEM_ERROR);
    }
}
