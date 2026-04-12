package com.pokergame.common.util;

import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.pokergame.common.exception.GameCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 参数校验工具类（基于 Bean Validation）
 *
 * <p>统一校验入口，校验失败抛出 MsgException（错误码 PARAM_ERROR）
 *
 * @author poker-platform
 */
@Slf4j
public final class ValidationUtils {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private ValidationUtils() {}

    /**
     * 校验对象，失败抛出第一个校验错误信息
     *
     * @param obj 待校验对象
     * @throws MsgException 参数错误异常
     */
    public static void validate(Object obj) throws MsgException {
        Set<ConstraintViolation<Object>> violations = validator.validate(obj);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            log.warn("参数校验失败: {}", message);
            throw new MsgException(GameCode.PARAM_ERROR.getCode(), message);
        }
    }

    /**
     * 校验对象（支持分组），返回所有错误信息拼接
     *
     * @param obj    待校验对象
     * @param groups 分组类型
     * @throws MsgException 参数错误异常（包含所有错误详情）
     */
    public static void validate(Object obj, Class<?>... groups) throws MsgException {
        Set<ConstraintViolation<Object>> violations = validator.validate(obj, groups);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            log.warn("参数校验失败: {}", message);
            throw new MsgException(GameCode.PARAM_ERROR.getCode(), message);
        }
    }
}
