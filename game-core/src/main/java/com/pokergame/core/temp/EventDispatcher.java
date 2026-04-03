package com.pokergame.core.temp;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 事件分发器 - 单例
 *
 * 支持：
 * - 事件监听器的注册和移除
 * - 事件的异步分发
 * - 事件链支持
 *
 * @author poker-platform
 */
@Slf4j
public class EventDispatcher {

    private static final EventDispatcher INSTANCE = new EventDispatcher();

    private final ConcurrentHashMap<GameEventType, List<Consumer<GameEvent>>> listeners = new ConcurrentHashMap<>();

    private EventDispatcher() {}

    public static EventDispatcher getInstance() {
        return INSTANCE;
    }

    /**
     * 注册事件监听器
     */
    public void register(GameEventType type, Consumer<GameEvent> listener) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>())
                .add(listener);
        log.debug("注册事件监听器: {}", type);
    }

    /**
     * 移除事件监听器
     */
    public void unregister(GameEventType type, Consumer<GameEvent> listener) {
        List<Consumer<GameEvent>> list = listeners.get(type);
        if (list != null) {
            list.remove(listener);
        }
    }

    /**
     * 分发事件（异步）
     */
    public void dispatch(GameEvent event) {
        List<Consumer<GameEvent>> list = listeners.get(event.getType());
        if (list == null || list.isEmpty()) {
            return;
        }

        // 异步执行，避免阻塞主流程
        list.forEach(listener -> {
            try {
                listener.accept(event);
            } catch (Exception e) {
                log.error("事件处理异常: type={}, roomId={}", event.getType(), event.getRoomId(), e);
            }
        });

        log.debug("事件分发完成: type={}, roomId={}", event.getType(), event.getRoomId());
    }

    /**
     * 同步分发事件
     */
    public void dispatchSync(GameEvent event) {
        List<Consumer<GameEvent>> list = listeners.get(event.getType());
        if (list == null || list.isEmpty()) {
            return;
        }

        for (Consumer<GameEvent> listener : list) {
            listener.accept(event);
        }
    }

    /**
     * 清除所有监听器
     */
    public void clear() {
        listeners.clear();
    }
}
