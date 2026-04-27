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

    // ========== 游戏进程数据 ==========

    /** 出牌顺序（玩家ID列表） */
    private List<Long> playOrder = new ArrayList<>();

    /** 当前回合索引 */
    private int currentTurnIndex = 0;

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

    /** 炸弹次数 */
    private int bombCount = 0;

    /** 当前倍率 */
    private int multiplier = 1;

    /** 当前游戏状态 */
    protected Enum<?> status;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     */
    public BaseGameStateManager() {}

    // ==================== 回合管理 ====================

    /**
     * 获取当前回合玩家
     */
    public P getCurrentPlayer(BaseRoom room) {
        if (playOrder.isEmpty() || currentTurnIndex >= playOrder.size()) return null;
        long userId = playOrder.get(currentTurnIndex);
        return room.getPlayerById(userId);
    }

    /**
     * 切换到下一个玩家
     */
    public void nextTurn() {
        if (playOrder.isEmpty()) return;
        currentTurnIndex = (currentTurnIndex + 1) % playOrder.size();
        log.debug("切换到下一个玩家，索引={}", currentTurnIndex);
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
        if (this.currentTurnIndex == -1) this.currentTurnIndex = 0;
        log.debug("设置出牌顺序: {}, 起始玩家索引={}", playOrder, currentTurnIndex);
    }

    /**
     * 检查是否为当前玩家
     */
    public boolean isCurrentPlayer(long userId, BaseRoom room) {
        P current = getCurrentPlayer(room);
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
        log.debug("炸弹触发，当前倍率: {}", multiplier);
    }

    /**
     * 重置倍率
     */
    public void resetMultiplier() {
        this.bombCount = 0;
        this.multiplier = 1;
    }

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

}