package com.pokergame.game.doudizhu.room;

import com.iohao.game.widget.light.room.SimpleRoom;
import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.game.doudizhu.bidding.BiddingManager;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.state.DoudizhuGameState;
import com.pokergame.game.doudizhu.turn.DoudizhuTurnManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 斗地主房间类
 *
 * 继承 SimpleRoom，扩展斗地主游戏所需的房间属性
 *
 * 职责：
 * 1. 管理房间内的玩家
 * 2. 维护游戏状态
 * 3. 记录出牌历史
 * 4. 管理倍率
 *
 * @author poker-platform
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class DoudizhuRoom extends SimpleRoom {

    // ==================== 房间基础信息 ====================

    /** 房主ID */
    private long ownerId;

    /** 最大玩家数（2-3人） */
    private int maxPlayers;

    /** 游戏状态 */
    private DoudizhuGameStatus gameStatus = DoudizhuGameStatus.WAITING;

    /** 游戏状态对象（用于规则检查） */
    private DoudizhuGameState gameState;

    /** 回合管理器 */
    private DoudizhuTurnManager turnManager;

    /** 叫地主管理器 */
    private BiddingManager biddingManager;


    // ==================== 游戏数据 ====================

    /** 出牌顺序列表（玩家ID列表，按座位顺序） */
    private List<Long> playOrder = new ArrayList<>();

    /** 当前回合索引（指向 playOrder 中的当前位置） */
    private int currentTurnIndex = 0;

    /** 上一手出的牌 */
    private List<Card> lastPlayCards = null;

    /** 上一手出牌的玩家ID */
    private long lastPlayPlayerId = 0;

    /** 上一手牌型 */
    private CardPattern lastPattern = null;

    /** 上一手主牌值 */
    private int lastMainRank = 0;

    /** 上一手副牌值 */
    private int lastSubRank = 0;

    // ==================== 地主相关 ====================

    /** 地主ID */
    private long landlordId = 0;

    /** 叫地主倍数 */
    private int currentMultiple = 1;

    /** 地主底牌（3张） */
    private List<Card> landlordExtraCards = new ArrayList<>();

    // ==================== 倍率相关 ====================

    /** 炸弹次数（用于倍率计算） */
    private int bombCount = 0;

    /** 当前倍率（基础1，每炸一次+1） */
    private int multiplier = 1;

    // ==================== 玩家管理 ====================

    /**
     * 获取斗地主玩家（类型转换）
     *
     * @param userId 玩家ID
     * @return 斗地主玩家对象，如果不存在返回 null
     */
    public DoudizhuPlayer getDoudizhuPlayer(long userId) {
        return (DoudizhuPlayer) getPlayerMap().get(userId);
    }

    /**
     * 获取玩家数量
     * @return
     */
    public int getPlayerCount() {
        return getPlayerMap().size();
    }

    /**
     * 获取所有斗地主玩家列表
     *
     * @return 斗地主玩家列表
     */
    public List<DoudizhuPlayer> getAllDoudizhuPlayers() {
        return getPlayerMap().values().stream()
                .map(p -> (DoudizhuPlayer) p)
                .collect(Collectors.toList());
    }

    /**
     * 添加斗地主玩家
     *
     * @param player 斗地主玩家
     */
    public void addDoudizhuPlayer(DoudizhuPlayer player) {
        // 检查游戏是否已开始
        if (gameStatus != DoudizhuGameStatus.WAITING && gameStatus != DoudizhuGameStatus.READY) {
            log.warn("游戏已开始，不能加入新玩家。当前状态: {}", gameStatus);
            return;
        }
        // 检查房间是否已满
        if (isFull()) {
            log.warn("房间已满，不能加入新玩家。房间: {}, 玩家: {}", getRoomId(), player.getUserId());
            return;
        }
        getPlayerMap().put(player.getUserId(), player);
        if (!player.isRobot()) {
            getRealPlayerMap().put(player.getUserId(), player);
        } else {
            getRobotMap().put(player.getUserId(), player);
        }
        log.info("玩家 {} 加入房间 {}", player.getUserId(), getRoomId());
    }

    /**
     * 移除斗地主玩家
     *
     * @param userId 玩家ID
     */
    public void removeDoudizhuPlayer(long userId) {
        getPlayerMap().remove(userId);
        getRealPlayerMap().remove(userId);
        getRobotMap().remove(userId);
        log.info("玩家 {} 离开房间 {}", userId, getRoomId());
    }

    /**
     * 检查房间是否已满
     *
     * @return true 如果房间已满
     */
    public boolean isFull() {
        return getPlayerMap().size() >= maxPlayers;
    }

    /**
     * 检查房间是否为空
     *
     * @return true 如果房间为空
     */
    public boolean isEmpty() {
        return getPlayerMap().isEmpty();
    }

    /**
     * 检查所有玩家是否都已准备
     *
     * @return true 如果所有玩家都已准备
     */
    public boolean isAllReady() {
        if (getPlayerMap().isEmpty()) {
            return false;
        }
        return getPlayerMap().values().stream()
                .map(p -> (DoudizhuPlayer) p)
                .allMatch(DoudizhuPlayer::isReady);
    }

    // ==================== 游戏流程控制 ====================

    /** 开始叫地主流程 */
    public void startBidding() {
        if (biddingManager != null) {
            biddingManager.start();
        }
    }

    /**
     * 获取当前回合玩家
     *
     * @return 当前回合玩家对象，如果不存在返回 null
     */
    public DoudizhuPlayer getCurrentPlayer() {
        if (playOrder.isEmpty() || currentTurnIndex >= playOrder.size()) {
            return null;
        }
        long currentUserId = playOrder.get(currentTurnIndex);
        return getDoudizhuPlayer(currentUserId);
    }

    /**
     * 检查是否为当前玩家
     */
    public boolean isCurrentPlayer(long userId) {
        DoudizhuPlayer current = getCurrentPlayer();
        return current != null && current.getUserId() == userId;
    }

    /**
     * 切换到下一个玩家
     */
    public void nextTurn() {
        if (playOrder.isEmpty()) {
            return;
        }
        currentTurnIndex = (currentTurnIndex + 1) % playOrder.size();
        log.debug("房间 {} 切换到下一个玩家: {}", getRoomId(), getCurrentPlayer());
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
     * 增加炸弹计数，更新倍率
     */
    public void addBomb() {
        bombCount++;
        multiplier = 1 + bombCount;
        log.debug("房间 {} 炸弹触发，当前倍率: {}", getRoomId(), multiplier);
    }

    /**
     * 更新出牌记录
     *
     * @param playerId 出牌玩家ID
     * @param cards 出的牌
     * @param pattern 牌型
     * @param mainRank 主牌值
     * @param subRank 副牌值
     */
    public void updateLastPlay(long playerId, List<Card> cards, CardPattern pattern, int mainRank, int subRank) {
        this.lastPlayPlayerId = playerId;
        this.lastPlayCards = cards;
        this.lastPattern = pattern;
        this.lastMainRank = mainRank;
        this.lastSubRank = subRank;
    }

    /**
     * 重置回合状态（新的一局）
     */
    public void resetRound() {
        this.lastPlayCards = null;
        this.lastPlayPlayerId = 0;
        this.lastPattern = null;
        this.bombCount = 0;
        this.multiplier = 1;
        this.currentTurnIndex = 0;
    }

    /**
     * 重置房间状态（新的一局）
     */
    public void reset() {
        resetRound();
        this.gameStatus = DoudizhuGameStatus.WAITING;
        this.landlordId = 0;
        this.landlordExtraCards.clear();

        // 重置所有玩家状态
        for (DoudizhuPlayer player : getAllDoudizhuPlayers()) {
            player.reset();
        }
    }

    /**
     * 更改游戏状态
     *
     * @param newStatus 新状态
     */
    public boolean changeGameStatus(DoudizhuGameStatus newStatus) {
        // 检查状态转换是否合法
        if (!isValidTransition(this.gameStatus, newStatus)) {
            log.warn("非法的状态转换: {} -> {}", this.gameStatus, newStatus);
            return false;
        }

        this.gameStatus = newStatus;
        log.info("房间 {} 状态变更: {} -> {}", getRoomId(), gameStatus, newStatus);
        return true;
    }

    @Override
    public String toString() {
        return String.format("DoudizhuRoom{roomId=%d, gameStatus=%s, playerCount=%d, maxPlayers=%d, ownerId=%d}",
                getRoomId(), gameStatus, getPlayerCount(), maxPlayers, ownerId);
    }

    /**
     * 检查状态转换是否合法
     */
    private boolean isValidTransition(DoudizhuGameStatus current, DoudizhuGameStatus target) {
        // 相同状态不需要转换
        if (current == target) {
            return true;
        }

        switch (current) {
            case WAITING:
                // WAITING 可以转换为 READY 或 BIDDING（直接开始）
                return target == DoudizhuGameStatus.READY || target == DoudizhuGameStatus.BIDDING;
            case READY:
                // READY 可以转换为 BIDDING（开始游戏）
                return target == DoudizhuGameStatus.BIDDING;
            case BIDDING:
                // BIDDING 可以转换为 PLAYING（叫地主结束）或 FINISHED（异常结束）
                return target == DoudizhuGameStatus.PLAYING || target == DoudizhuGameStatus.FINISHED;
            case PLAYING:
                // PLAYING 只能转换为 FINISHED（游戏结束）
                return target == DoudizhuGameStatus.FINISHED;
            case FINISHED:
                // FINISHED 可以转换为 WAITING（重置）或直接销毁
                return target == DoudizhuGameStatus.WAITING;
            default:
                return false;
        }
    }

}
