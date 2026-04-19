package com.pokergame.starter.spring.spel;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * SpEL 表达式求值器
 * <p>
 * 用于在 AOP 切面中根据方法参数和表达式字符串提取值。
 * </p>
 *
 * @author poker-platform
 */
public class SpelExpressionEvaluator {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer PARAM_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * 在给定上下文和根对象下计算表达式
     *
     * @param expressionStr SpEL 表达式字符串
     * @param context       上下文（包含变量）
     * @param requiredType  期望返回的类型
     * @param <T>           泛型
     * @return 计算结果，若表达式为空或无效则返回 null
     */
    public static <T> T eval(String expressionStr, EvaluationContext context, Class<T> requiredType) {
        if (expressionStr == null || expressionStr.isEmpty()) {
            return null;
        }
        Expression expression = PARSER.parseExpression(expressionStr);
        return expression.getValue(context, requiredType);
    }

    /**
     * 根据方法参数构建 EvaluationContext
     *
     * @param method 目标方法
     * @param args   方法参数
     * @return EvaluationContext 实例
     */
    public static EvaluationContext buildContext(Method method, Object[] args) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = PARAM_NAME_DISCOVERER.getParameterNames(method);
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        return context;
    }
}
