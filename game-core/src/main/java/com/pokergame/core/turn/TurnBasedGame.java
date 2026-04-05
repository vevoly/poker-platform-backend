package com.pokergame.core.turn;

import com.pokergame.core.temp.GameRoom;
import lombok.extern.slf4j.Slf4j;

/**
 * 回合制游戏抽象类
 *
 * 扩展 GameRoom，增加回合制游戏的通用逻辑
 *
 * @author poker-platform
 */
@Slf4j
public abstract class TurnBasedGame extends GameRoom {

    /** 回合管理器 */
    protected TurnManager turnManager;
    /** 当前回合数 */
    protected int currentRound = 1;
    /** 最大回合数 */
    protected int maxRounds;

    public TurnBasedGame(String roomId, String gameType, long ownerId, int maxPlayers) {
        super(roomId, gameType, ownerId, maxPlayers);
        this.maxRounds = 0;
    }

    public TurnBasedGame(String roomId, String gameType, long ownerId,
                         int maxPlayers, int maxRounds) {
        super(roomId, gameType, ownerId, maxPlayers);
        this.maxRounds = maxRounds;
    }

    /**
     * 初始化回合管理器
     */
    protected void initTurnManager(long startPlayerId) {
        this.turnManager = new TurnManager(roomId, getPlayerIds(), startPlayerId);
        log.info("房间{}回合管理器初始化，起始玩家: {}", roomId, startPlayerId);
    }

    /**
     * 设置当前玩家的超时定时器
     */
    protected void setTimeout(int timeoutSeconds, Runnable onTimeout) {
        if (turnManager != null) {
            turnManager.setTimeout(timeoutSeconds, onTimeout);
        }
    }

    /**
     * 取消当前回合的定时器
     */
    protected void cancelTimeout() {
        if (turnManager != null) {
            turnManager.cancelCurrentTimer();
        }
    }

    /**
     * 检查是否为当前玩家
     */
    protected boolean isCurrentPlayer(long playerId) {
        return turnManager != null && turnManager.isCurrentPlayer(playerId);
    }

    /**
     * 获取当前玩家
     */
    protected long getCurrentPlayer() {
        return turnManager != null ? turnManager.getCurrentPlayer() : 0;
    }

    /**
     * 切换到下一个玩家
     */
    protected void nextTurn() {
        if (turnManager != null) {
            turnManager.nextTurn();
            currentRound++;

            // 检查是否达到最大回合数
            if (maxRounds > 0 && currentRound > maxRounds) {
                log.info("房间{}达到最大回合数{}，游戏结束", roomId, maxRounds);
                endGame();
            }
        }
    }

    /**
     * 处理超时（子类可重写）
     */
    protected void onTimeout() {
        log.info("房间{}玩家{}超时，自动过牌", roomId, getCurrentPlayer());
        processPass(getCurrentPlayer());
    }

    /**
     * 处理过牌（子类必须实现）
     */
    protected abstract void processPass(long playerId);

    @Override
    public void destroy() {
        if (turnManager != null) {
            turnManager.cleanup();
        }
        super.destroy();
    }
}
