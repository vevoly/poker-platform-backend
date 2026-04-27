package com.pokergame.game.doudizhu.bidding;

import com.pokergame.common.card.Card;
import com.pokergame.game.doudizhu.broadcast.DoudizhuBroadcastKit;
import com.pokergame.game.doudizhu.enums.BiddingState;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.state.DoudizhuGameStateManager;
import com.pokergame.game.doudizhu.turn.DoudizhuTurnManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 叫地主管理器
 *
 * 职责：管理叫地主流程，依赖 DoudizhuGameState 存储游戏数据
 *
 * @author poker-platform
 */
@Slf4j
public class BiddingManager {

    /** 斗地主房间 */
    private final DoudizhuRoom room;
    /** 斗地主游戏状态 */
    private final DoudizhuGameStateManager gameState;
    /** 玩家顺序 */
    private final List<Long> playerOrder;
    /** 叫地主记录 */
    private final Map<Long, BidRecord> bidRecords = new ConcurrentHashMap<>();
    /** 叫地主轮次管理器 */
    private final DoudizhuTurnManager turnManager;

    /** 当前状态 */
    @Getter
    private BiddingState state = BiddingState.WAITING;
    /** 当前叫地主玩家索引 */
    private int currentIndex = 0;
    /** 当前叫地主轮数 */
    @Getter
    private int currentBiddingRound = 1;
    /** 当前地主玩家 */
    @Getter
    private long currentLandlordId = 0;
    /** 当前叫地主倍数 */
    @Getter
    private int currentMultiple = 0;

    public BiddingManager(DoudizhuRoom room) {
        this.room = room;
        this.gameState = room.getStateManager();
        this.playerOrder = room.getPlayerOrder();
        this.turnManager = room.getTurnManager();
    }

    /**
     * 开始叫地主
     */
    public void start() {
        log.info("房间 {} 开始叫地主阶段", room.getRoomId());
        state = BiddingState.WAITING;
        currentBiddingRound = 1;
        currentIndex = 0;
        currentLandlordId = 0;
        currentMultiple = 0;
        bidRecords.clear();
        notifyCurrentPlayer();
    }

    /**
     * 处理玩家叫地主
     *
     * @param userId 玩家ID
     * @param multiple 叫地主倍数
     */
    public synchronized void handleGrab(long userId, int multiple) {
        if (state != BiddingState.WAITING) {
            log.warn("状态错误，当前状态: {}", state);
            return;
        }
        if (!isCurrentPlayer(userId)) {
            log.warn("不是当前玩家，当前玩家: {}", getCurrentPlayerId());
            return;
        }
        turnManager.cancelTimeout();
        bidRecords.put(userId, BidRecord.grab(userId, multiple));
        currentLandlordId = userId;
        currentMultiple = multiple;
        DoudizhuBroadcastKit.broadcastGrabLandlord(userId, multiple, room);
        moveToNext();
    }

    /**
     * 处理玩家不叫地主
     *
     * @param userId 玩家ID
     */
    public synchronized void handleNotGrab(long userId) {
        if (state != BiddingState.WAITING) {
            log.warn("状态错误，当前状态: {}", state);
            return;
        }
        if (!isCurrentPlayer(userId)) {
            log.warn("不是当前玩家，当前玩家: {}", getCurrentPlayerId());
            return;
        }
        turnManager.cancelTimeout();
        bidRecords.put(userId, BidRecord.notGrab(userId));
        DoudizhuBroadcastKit.broadcastNotGrab(userId, room);
        moveToNext();
    }

    /**
     * 处理玩家叫地主超时
     */
    public synchronized void handleTimeout() {
        long userId = getCurrentPlayerId();
        log.info("玩家 {} 叫地主超时，自动不抢", userId);
        handleNotGrab(userId);
    }

    /**
     * 移动到下一个玩家
     */
    private void moveToNext() {
        currentIndex++;
        if (currentIndex >= playerOrder.size()) {
            endRound();
        } else {
            notifyCurrentPlayer();
        }
    }

    /**
     * 叫地主结束
     */
    private void endRound() {
        log.info("第 {} 轮叫地主结束", currentBiddingRound);
        if (currentLandlordId != 0) {
            determineLandlord();
            return;
        }
        currentBiddingRound++;
        currentIndex = 0;
        if (currentBiddingRound <= 3) {
            log.info("进入第 {} 轮叫地主", currentBiddingRound);
            notifyCurrentPlayer();
        } else {
            state = BiddingState.ALL_PASS;
            assignRandomLandlord();
        }
    }

    /**
     * 确定地主
     */
    private void determineLandlord() {
        state = BiddingState.LANDLORD_DETERMINED;
        log.info("地主确定: 玩家 {}, 倍数: {}", currentLandlordId, currentMultiple);
        // 更新 gameState
        gameState.setLandlordId(currentLandlordId);
        // 注意：currentMultiple 对应倍率，gameState 中有 multiplier 字段，可能需要单独存储叫地主倍数
        // 这里简化，直接设置 multiplier（根据业务调整）
        gameState.setMultiplier(currentMultiple);
        giveExtraCardsToLandlord();
        setPlayOrder();
        enterPlayingPhase();
    }

    /**
     * 随机分配地主
     */
    private void assignRandomLandlord() {
        log.info("所有玩家都不抢，随机分配地主");
        int randomIndex = (int) (Math.random() * playerOrder.size());
        currentLandlordId = playerOrder.get(randomIndex);
        currentMultiple = 1;
        state = BiddingState.LANDLORD_DETERMINED;
        gameState.setLandlordId(currentLandlordId);
        gameState.setMultiplier(currentMultiple);
        giveExtraCardsToLandlord();
        setPlayOrder();
        enterPlayingPhase();
    }

    /**
     * 给地主发底牌
     */
    private void giveExtraCardsToLandlord() {
        DoudizhuPlayer landlord = room.getDoudizhuPlayer(currentLandlordId);
        List<Card> extraCards = gameState.getLandlordExtraCards();
        if (extraCards != null && !extraCards.isEmpty()) {
            landlord.addCards(extraCards);
            log.info("地主拿到底牌: {}", extraCards);
        }
    }

    /**
     * 设置出牌顺序
     */
    private void setPlayOrder() {
        List<Long> order = new ArrayList<>();
        int landlordIndex = playerOrder.indexOf(currentLandlordId);
        for (int i = 0; i < playerOrder.size(); i++) {
            order.add(playerOrder.get((landlordIndex + i) % playerOrder.size()));
        }
        gameState.setPlayOrder(order, currentLandlordId);
        log.info("出牌顺序: {}", order);
    }

    /**
     * 进入出牌阶段
     */
    private void enterPlayingPhase() {
        // 更新房间展示状态
        room.setGameStatus(DoudizhuGameStatus.PLAYING.name());
        // 更新游戏逻辑状态
        gameState.changeStatus(DoudizhuGameStatus.PLAYING);
        turnManager.startTimeout();
        // 广播当前回合玩家（地主先出）
        long currentPlayerId = room.getCurrentPlayer() != null ? room.getCurrentPlayer().getUserId() : currentLandlordId;
        DoudizhuBroadcastKit.broadcastTurn(currentPlayerId, room);
        log.info("房间 {} 进入出牌阶段，当前玩家 {}", room.getRoomId(), currentPlayerId);
    }

    /**
     * 通知当前玩家叫地主
     */
    private void notifyCurrentPlayer() {
        long currentPlayerId = getCurrentPlayerId();
        turnManager.setTimeout(this::handleTimeout);
        DoudizhuBroadcastKit.broadcastBiddingTurn(currentPlayerId, currentBiddingRound, room);
        log.info("轮到玩家 {} 叫地主，第 {} 轮", currentPlayerId, currentBiddingRound);
    }

    /**
     * 获取当前玩家ID
     * @return
     */
    private long getCurrentPlayerId() {
        return playerOrder.get(currentIndex);
    }

    /**
     * 判断是否是当前玩家
     * @param userId
     * @return
     */
    private boolean isCurrentPlayer(long userId) {
        return getCurrentPlayerId() == userId;
    }

    /**
     * 获取叫地主记录
     * @return
     */
    public Map<Long, BidRecord> getBidRecords() {
        return new ConcurrentHashMap<>(bidRecords);
    }

    /**
     * 重置
     */
    public synchronized void reset() {
        turnManager.cancelTimeout();
        state = BiddingState.WAITING;
        currentBiddingRound = 1;
        currentIndex = 0;
        currentLandlordId = 0;
        currentMultiple = 0;
        bidRecords.clear();
        log.info("叫地主管理器已重置");
    }
}
