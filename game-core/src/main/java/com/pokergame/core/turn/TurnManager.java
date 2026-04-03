package com.pokergame.core.turn;

import com.iohao.game.common.kit.concurrent.timer.delay.DelayTaskKit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 回合管理器 - 管理玩家出牌顺序
 *
 * @author poker-platform
 */
@Slf4j
@Getter
public class TurnManager {

    /** 玩家顺序 */
    private final List<Long> playerOrder;
    /** 当前玩家索引 */
    private int currentIndex;
    /** 连续过牌次数 */
    private int passCount;

    // 管理房间内的定时任务
    private final ConcurrentMap<String, String> taskIds = new ConcurrentHashMap<>();
    private final String roomId;

    public TurnManager(String roomId, List<Long> playerOrder, long startPlayerId) {
        this.roomId = roomId;
        this.playerOrder = playerOrder;
        this.currentIndex = playerOrder.indexOf(startPlayerId);
        this.passCount = 0;
        log.debug("回合管理器初始化，房间: {}, 起始玩家: {}", roomId, startPlayerId);
    }

    /**
     * 获取当前玩家
     */
    public long getCurrentPlayer() {
        return playerOrder.get(currentIndex);
    }

    /**
     * 切换到下一个玩家，并取消之前的定时器
     */
    public void nextTurn() {
        // 取消当前回合的定时器
        cancelCurrentTimer();

        currentIndex = (currentIndex + 1) % playerOrder.size();
        passCount = 0;
        log.debug("房间: {}, 切换到下一个玩家: {}", roomId, getCurrentPlayer());
    }

    /**
     * 过牌（不切换玩家，只增加过牌计数）
     */
    public void pass() {
        cancelCurrentTimer();
        passCount++;
        log.debug("房间: {}, 玩家{}过牌，连续过牌次数: {}", roomId, getCurrentPlayer(), passCount);
        nextTurn();
    }

    /**
     * 设置当前玩家的超时定时器
     * @param timeoutSeconds 超时秒数
     * @param onTimeout 超时回调
     */
    public void setTimeout(int timeoutSeconds, Runnable onTimeout) {
        cancelCurrentTimer();

        String taskId = roomId + "_turn_timeout";
        taskIds.put("current", taskId);

        DelayTaskKit.of(taskId, () -> {
            log.info("房间: {}, 玩家{}出牌超时，执行超时回调", roomId, getCurrentPlayer());
            onTimeout.run();
        }).plusTime(Duration.ofSeconds(timeoutSeconds)).task();

        log.debug("房间: {}, 设置玩家{}超时定时器: {}秒", roomId, getCurrentPlayer(), timeoutSeconds);
    }

    /**
     * 取消当前回合的定时器
     */
    void cancelCurrentTimer() {
        String taskId = taskIds.remove("current");
        if (taskId != null) {
            DelayTaskKit.cancel(taskId);
            log.debug("房间: {}, 取消当前回合定时器", roomId);
        }
    }

    /**
     * 检查是否所有玩家都过牌
     */
    public boolean isAllPassed() {
        return passCount >= playerOrder.size() - 1;
    }

    /**
     * 重置过牌计数
     */
    public void resetPassCount() {
        passCount = 0;
    }

    /**
     * 检查是否为当前玩家
     */
    public boolean isCurrentPlayer(long playerId) {
        return getCurrentPlayer() == playerId;
    }

    /**
     * 获取下一个玩家
     */
    public long getNextPlayer() {
        int nextIndex = (currentIndex + 1) % playerOrder.size();
        return playerOrder.get(nextIndex);
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        cancelCurrentTimer();
        taskIds.clear();
    }

}
