package com.pokergame.common.util;

import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.pokergame.common.event.BaseGameEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 通用事件发布工具类
 * 不依赖具体游戏类型，通过 Supplier 让调用方提供事件实例
 */
@Slf4j
public final class EventPublisher {

   private EventPublisher() {}

    /**
     * 发布事件（异步）
     * @param ctx FlowContext
     * @param eventSupplier 事件提供者（例如 () -> new GameStartEvent(...)）
     */
    public static void fire(FlowContext ctx, Supplier<? extends BaseGameEvent> eventSupplier) {
        BaseGameEvent event = eventSupplier.get();
        ctx.fire(event);
        log.debug("发布事件(异步): type={}, roomId={}", event.getEventType(), event.getRoomId());
    }

    /**
     * 发布事件（同步）
     */
    public static void fireSync(FlowContext ctx, Supplier<? extends BaseGameEvent> eventSupplier) {
        BaseGameEvent event = eventSupplier.get();
        ctx.fireSync(event);
        log.debug("发布事件(同步): type={}, roomId={}", event.getEventType(), event.getRoomId());
    }
}
