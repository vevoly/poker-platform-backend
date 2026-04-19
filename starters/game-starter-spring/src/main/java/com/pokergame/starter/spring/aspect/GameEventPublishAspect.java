package com.pokergame.starter.spring.aspect;

import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.pokergame.common.card.CardConverter;
import com.pokergame.common.enums.GameActionType;
import com.pokergame.common.game.GameType;
import com.pokergame.common.card.CardDTO;
import com.pokergame.starter.spring.annotation.PublishGameEvent;
import com.pokergame.starter.spring.spel.SpelExpressionEvaluator;
import lombok.extern.slf4j.Slf4j;

import com.pokergame.common.enums.GameEventType;
import com.pokergame.common.event.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 游戏事件发布切面
 * <p>
 * 拦截带有 {@link PublishGameEvent} 注解的方法，在方法成功执行后，
 * 根据注解配置的 SpEL 表达式从方法参数中提取必要信息，构造事件并通过 FlowContext 发布。
 * </p>
 *
 * @author poker-platform
 */
@Slf4j
@Aspect
@Component
@Order(1) // 确保在业务事务提交后执行（如果有事务）
public class GameEventPublishAspect {

    @Around("@annotation(publishEvent)")
    public Object publishEvent(ProceedingJoinPoint joinPoint, PublishGameEvent publishEvent) throws Throwable {
        // 1. 执行原方法
        Object result = joinPoint.proceed();

        // 2. 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        EvaluationContext spelContext = SpelExpressionEvaluator.buildContext(signature.getMethod(), args);

        // 3. 提取 FlowContext
        FlowContext flowContext = extractFlowContext(args);
        if (flowContext == null) {
            log.warn("方法 {} 的参数中未找到 FlowContext，无法发布事件", signature.getMethod().getName());
            return result;
        }

        // 4. 提取公共字段
        String roomId = SpelExpressionEvaluator.eval(publishEvent.roomIdSpel(), spelContext, String.class);
        Long userId = SpelExpressionEvaluator.eval(publishEvent.userIdSpel(), spelContext, Long.class);
        if (roomId == null || userId == null) {
            log.warn("无法解析 roomId 或 userId，roomIdSpel={}, userIdSpel={}", publishEvent.roomIdSpel(), publishEvent.userIdSpel());
            return result;
        }

        // 5. 根据事件类型构造事件对象
        BaseGameEvent event = buildEvent(publishEvent, spelContext, roomId, userId);
        if (event == null) {
            log.warn("不支持的事件类型: {}", publishEvent.eventType());
            return result;
        }

        // 6. 发布事件（支持同步/异步）
        if (publishEvent.async()) {
            // 异步发布（不阻塞业务线程）
            flowContext.fire(event);
            log.debug("异步发布事件: {}", event);
        } else {
            flowContext.fireSync(event);
            log.debug("同步发布事件: {}", event);
        }

        return result;
    }

    private FlowContext extractFlowContext(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof FlowContext) {
                return (FlowContext) arg;
            }
        }
        return null;
    }

    private BaseGameEvent buildEvent(PublishGameEvent anno, EvaluationContext ctx, String roomId, Long userId) {

        // 解析游戏类型
        GameEventType eventType = anno.eventType();
        GameType gameType = anno.gameType();
        GameActionType action = anno.actionType();

        switch (eventType) {
            case DOUDIZHU_GRAB_LANDLORD:
                Integer multiple = SpelExpressionEvaluator.eval(anno.multipleSpel(), ctx, Integer.class);
                return new BiddingEvent(gameType, roomId, userId, multiple != null ? multiple : 0);

            case DOUDIZHU_PLAY_CARD:
                List<?> rawCards = SpelExpressionEvaluator.eval(anno.cardsSpel(), ctx, List.class);
                List<CardDTO> cards = CardConverter.toCardDTOList(rawCards);
                return new GameActionEvent(eventType, gameType, roomId, userId, action, cards, null);

            case PASS:
                return new PassEvent(gameType, roomId, userId);

            default:
                // 其他事件...
        }
        return null;
    }

}
