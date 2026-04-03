package com.pokergame.core.room;

import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.pokergame.core.player.GamePlayer;
import com.pokergame.core.player.PlayerStatus;
import com.pokergame.core.turn.TurnManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏房间 - 抽象基类
 * 使用 ioGame 的 FlowContext 进行消息推送
 *
 * @author poker-platform
 */
@Slf4j
@Getter
public abstract class GameRoom {

    /** 房间ID */
    protected final String roomId;
    /** 游戏类型 */
    protected final String gameType;
    /** 房主ID */
    protected final long ownerId;
    /** 最大玩家数量 */
    protected final int maxPlayers;
    /** 玩家列表 */
    protected final Map<Long, GamePlayer> players = new ConcurrentHashMap<>();
    /** 房间状态 */
    protected RoomStatus status = RoomStatus.WAITING;
    /** 轮次管理器 */
    protected TurnManager turnManager;

    // 用于推送消息的 FlowContext（由子类设置）
    protected transient FlowContext flowContext;

    /** 可选：俱乐部 */
    protected Long clubId;
    /** 可选：联盟 */
    protected Long allianceId;

    public GameRoom(String roomId, String gameType, long ownerId, int maxPlayers) {
        this(roomId, gameType, ownerId, maxPlayers, null, null);
    }

    public GameRoom(String roomId, String gameType, long ownerId,
                    int maxPlayers, Long clubId, Long allianceId) {
        this.roomId = roomId;
        this.gameType = gameType;
        this.ownerId = ownerId;
        this.maxPlayers = maxPlayers;
        this.clubId = clubId;
        this.allianceId = allianceId;
    }

    /**
     * 设置 FlowContext（用于推送消息）
     */
    public void setFlowContext(FlowContext flowContext) {
        this.flowContext = flowContext;
    }

    /**
     * 添加玩家
     */
    public boolean addPlayer(long playerId, String playerName) {
        if (players.size() >= maxPlayers) {
            log.warn("房间已满: roomId={}", roomId);
            return false;
        }
        if (players.containsKey(playerId)) {
            log.warn("玩家已在房间中: playerId={}", playerId);
            return false;
        }

        GamePlayer player = new GamePlayer(playerId, playerName);
        players.put(playerId, player);
        log.info("玩家加入房间: roomId={}, playerId={}", roomId, playerId);

        // 人数满足条件时自动开始
        if (players.size() == maxPlayers && status == RoomStatus.WAITING) {
            startGame();
        }

        return true;
    }

    /**
     * 移除玩家
     */
    public void removePlayer(long playerId) {
        GamePlayer player = players.remove(playerId);
        if (player != null) {
            player.setStatus(PlayerStatus.LEFT);
            log.info("玩家离开房间: roomId={}, playerId={}", roomId, playerId);

            // 房主离开且房间未开始时，销毁房间
            if (playerId == ownerId && status == RoomStatus.WAITING) {
                destroy();
            }

            // 房间空了就销毁
            if (players.isEmpty()) {
                destroy();
            }
        }
    }

    /**
     * 获取玩家
     */
    public GamePlayer getPlayer(long playerId) {
        return players.get(playerId);
    }

    /**
     * 获取玩家数量
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * 获取玩家ID列表
     */
    public List<Long> getPlayerIds() {
        return new ArrayList<>(players.keySet());
    }

    /**
     * 检查房间是否为空
     */
    public boolean isEmpty() {
        return players.isEmpty();
    }

    /**
     * 广播消息给房间内所有玩家
     */
    protected void broadcast(Object message) {
        if (flowContext != null) {
            for (Long playerId : players.keySet()) {
                flowContext.broadcastMe(message);
            }
        }
    }

    /**
     * 发送消息给指定玩家
     */
    protected void sendToPlayer(long playerId, Object message) {
        if (flowContext != null) {
            flowContext.broadcastMe(message);
        }
    }

    /**
     * 销毁房间
     */
    public void destroy() {
        if (turnManager != null) {
            turnManager.cleanup();
        }
        status = RoomStatus.CLOSED;
        RoomManager.getInstance().removeRoom(roomId);
        log.info("房间已销毁: roomId={}", roomId);
    }

    // ==================== 抽象方法（子类实现） ====================

    /**
     * 开始游戏
     */
    protected abstract void startGame();

    /**
     * 处理玩家操作
     */
    public abstract void processMove(long playerId, Object moveData);

    /**
     * 结束游戏
     */
    protected abstract void endGame();

    /**
     * 转换为房间信息DTO
     */
    public abstract Object toRoomInfo();
}
