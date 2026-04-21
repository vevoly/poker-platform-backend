package com.pokergame.core.base;

import com.iohao.game.common.kit.concurrent.TaskListener;
import com.iohao.game.common.kit.concurrent.timer.delay.DelayTaskKit;
import com.iohao.game.widget.light.room.Room;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 回合管理器基类
 *
 * 封装了通用的回合管理逻辑：
 * 1. 超时定时器管理
 * 2. 使用 ioGame 的 DelayTaskKit
 *
 * @param <R> 房间类型（必须继承 Room，因为需要调用 operation 方法）
 * @author poker-platform
 */
@Slf4j
public abstract class BaseTurnManager<R extends Room> {

    protected final R room;
    protected final AtomicReference<String> currentTaskId = new AtomicReference<>();

    /** 默认超时时间（秒） */
    protected static final int DEFAULT_TIMEOUT_SECONDS = 15;

    public BaseTurnManager(R room) {
        this.room = room;
    }

    /**
     * 启动超时定时器
     */
    public void startTimeout() {
        startTimeout(DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 启动超时定时器（自定义超时时间）
     */
    public void startTimeout(int seconds) {
        cancelTimeout();

        String taskId = generateTaskId();
        currentTaskId.set(taskId);

        DelayTaskKit.of(taskId, this::onTimeout)
                .plusTime(Duration.ofSeconds(seconds))
                .task();

        log.debug("启动超时定时器: {}秒", seconds);
    }

    /**
     * 设置超时回调（使用默认超时时间）
     *
     * @param onTimeout 超时回调
     */
    public void setTimeout(Runnable onTimeout) {
        setTimeout(DEFAULT_TIMEOUT_SECONDS, onTimeout);
    }

    /**
     * 设置超时回调（自定义超时时间）
     *
     * @param seconds 超时秒数
     * @param onTimeout 超时回调
     */
    /**
     * 设置超时回调（自定义超时时间）
     *
     * @param seconds 超时秒数
     * @param onTimeout 超时回调（Runnable）
     */
    public void setTimeout(int seconds, Runnable onTimeout) {
        cancelTimeout();

        String taskId = generateTaskId();
        currentTaskId.set(taskId);

        // 将 Runnable 包装成 TaskListener
        DelayTaskKit.of(taskId, (TaskListener) onTimeout::run)
                .plusTime(Duration.ofSeconds(seconds))
                .task();

        log.debug("设置超时定时器: {}秒", seconds);
    }

    /**
     * 取消超时定时器
     */
    public void cancelTimeout() {
        String taskId = currentTaskId.get();
        if (taskId != null) {
            DelayTaskKit.cancel(taskId);
            currentTaskId.set(null);
            log.debug("取消超时定时器");
        }
    }

    /**
     * 重置定时器（取消后重新开始）
     */
    public void resetTimeout() {
        cancelTimeout();
        startTimeout();
    }

    /**
     * 生成任务ID（子类可重写）
     */
    protected String generateTaskId() {
        return "turn_timeout_" + System.currentTimeMillis() + "_" + System.nanoTime();
    }

    /**
     * 超时回调（子类必须实现）
     */
    protected abstract void onTimeout();

    /**
     * 重置管理器（取消任务）
     */
    public void reset() {
        cancelTimeout();
    }
}
