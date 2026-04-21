package com.pokergame.core.base;

import com.pokergame.common.card.Card;
import com.pokergame.common.pattern.PatternResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏状态基类
 *
 * 封装了通用的游戏状态管理：
 * 1. 房间信息
 * 2. 玩家管理
 * 3. 回合管理
 * 4. 出牌记录
 * 5. 倍率管理
 *
 * 注意：使用 @Getter/@Setter 代替 @Data，避免生成不必要的 setter
 *
 * @param <P> 玩家类型
 * @author poker-platform
 */
@Slf4j
@Getter
@Setter
public abstract class BaseGameStateManager<P extends BasePlayer> {

    // ==================== 房间信息 ====================

    /** 房间ID */
    private long roomId;

    /** 房主ID */
    private long ownerId;

    /** 最大玩家数 */
    private int maxPlayers;

    /** 游戏状态枚举（子类定义） */
    protected Enum<?> status;

    // ==================== 玩家数据 ====================

    /** 玩家映射 */
    protected final Map<Long, P> players = new ConcurrentHashMap<>();

    /** 出牌顺序（玩家ID列表） */
    private List<Long> playOrder = new ArrayList<>();

    /** 当前回合索引 */
    private int currentTurnIndex = 0;

    // ==================== 牌局数据 ====================

    /** 地主ID */
    private long landlordId = 0;

    /** 地主底牌 */
    private List<Card> landlordExtraCards = new ArrayList<>();

    /** 上一手出的牌 */
    private List<Card> lastPlayCards = null;

    /** 上一手出牌的玩家 */
    private long lastPlayPlayerId = 0;

    /** 上一手牌型 */
    private PatternResult lastPattern = null;

    // ==================== 倍率数据 ====================

    /** 炸弹次数 */
    private int bombCount = 0;

    /** 当前倍率 */
    private int multiplier = 1;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     *
     * @param roomId 房间ID
     * @param ownerId 房主ID
     * @param maxPlayers 最大玩家数
     */
    public BaseGameStateManager(long roomId, long ownerId, int maxPlayers) {
        this.roomId = roomId;
        this.ownerId = ownerId;
        this.maxPlayers = maxPlayers;
    }

    // ==================== 玩家管理 ====================

    /**
     * 添加玩家
     */
    public void addPlayer(P player) {
        players.put(player.getUserId(), player);
        log.info("玩家 {} 加入房间 {}", player.getUserId(), roomId);
    }

    /**
     * 移除玩家
     */
    public void removePlayer(long userId) {
        players.remove(userId);
        log.info("玩家 {} 离开房间 {}", userId, roomId);
    }

    /**
     * 获取玩家
     */
    public P getPlayer(long userId) {
        return players.get(userId);
    }

    /**
     * 获取玩家数量
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * 检查房间是否已满
     */
    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    /**
     * 检查房间是否为空
     */
    public boolean isEmpty() {
        return players.isEmpty();
    }

    /**
     * 检查所有玩家是否都已准备
     */
    public boolean isAllReady() {
        if (players.isEmpty()) return false;
        return players.values().stream().allMatch(BasePlayer::isReady);
    }

    // ==================== 回合管理 ====================

    /**
     * 获取当前回合玩家
     */
    public P getCurrentPlayer() {
        if (playOrder.isEmpty() || currentTurnIndex >= playOrder.size()) {
            return null;
        }
        return players.get(playOrder.get(currentTurnIndex));
    }

    /**
     * 切换到下一个玩家
     */
    public void nextTurn() {
        if (playOrder.isEmpty()) return;
        currentTurnIndex = (currentTurnIndex + 1) % playOrder.size();
        log.debug("房间 {} 切换到下一个玩家: {}", roomId, getCurrentPlayer());
    }

    /**
     * 设置出牌顺序
     *
     * @param order 出牌顺序列表
     * @param startPlayerId 起始玩家ID
     */
    public void setPlayOrder(List<Long> order, long startPlayerId) {
        this.playOrder = new ArrayList<>(order);
        this.currentTurnIndex = playOrder.indexOf(startPlayerId);
    }

    /**
     * 检查是否为当前玩家
     */
    public boolean isCurrentPlayer(long userId) {
        P current = getCurrentPlayer();
        return current != null && current.getUserId() == userId;
    }

    // ==================== 出牌记录 ====================

    /**
     * 更新出牌记录
     *
     * @param playerId 出牌玩家ID
     * @param cards 出的牌
     * @param pattern 牌型
     */
    public void updateLastPlay(long playerId, List<Card> cards, PatternResult pattern) {
        this.lastPlayPlayerId = playerId;
        this.lastPlayCards = cards;
        this.lastPattern = pattern;
    }

    /**
     * 重置出牌记录
     */
    public void resetLastPlay() {
        this.lastPlayCards = null;
        this.lastPlayPlayerId = 0;
        this.lastPattern = null;
    }

    // ==================== 倍率管理 ====================

    /**
     * 增加炸弹计数，更新倍率
     */
    public void addBomb() {
        bombCount++;
        multiplier = 1 + bombCount;
        log.debug("房间 {} 炸弹触发，当前倍率: {}", roomId, multiplier);
    }

    /**
     * 重置倍率
     */
    public void resetMultiplier() {
        this.bombCount = 0;
        this.multiplier = 1;
    }

    // ==================== 状态管理（子类实现） ====================

    /**
     * 更改游戏状态
     *
     * @param newStatus 新状态
     */
    public abstract void changeStatus(Enum<?> newStatus);

    /**
     * 获取当前状态
     */
    public abstract Enum<?> getCurrentStatus();

    /**
     * 重置游戏状态（新的一局）
     */
    public abstract void reset();

    // ==================== 辅助方法 ====================

    /**
     * 重置回合相关状态（不重置玩家数据）
     */
    public void resetRound() {
        this.playOrder.clear();
        this.currentTurnIndex = 0;
        this.landlordId = 0;
        this.landlordExtraCards.clear();
        this.lastPlayCards = null;
        this.lastPlayPlayerId = 0;
        this.lastPattern = null;
        this.bombCount = 0;
        this.multiplier = 1;
    }
}