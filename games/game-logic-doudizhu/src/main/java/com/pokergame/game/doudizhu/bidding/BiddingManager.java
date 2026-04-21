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

    private final DoudizhuRoom room;
    private final DoudizhuGameStateManager gameState;
    private final List<Long> playerOrder;
    private final Map<Long, BidRecord> bidRecords = new ConcurrentHashMap<>();
    private final DoudizhuTurnManager turnManager;

    @Getter
    private BiddingState state = BiddingState.WAITING;
    private int currentIndex = 0;
    @Getter
    private int currentRound = 1;
    @Getter
    private long currentLandlordId = 0;
    @Getter
    private int currentMultiple = 0;

    public BiddingManager(DoudizhuRoom room) {
        this.room = room;
        this.gameState = room.getStateManager();
        // 玩家顺序从 gameState 获取（按座位顺序）
        this.playerOrder = new ArrayList<>(gameState.getPlayers().keySet());
        this.turnManager = room.getTurnManager();
    }

    public void start() {
        log.info("房间 {} 开始叫地主阶段", room.getRoomId());
        state = BiddingState.WAITING;
        currentRound = 1;
        currentIndex = 0;
        currentLandlordId = 0;
        currentMultiple = 0;
        bidRecords.clear();
        notifyCurrentPlayer();
    }

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

    public synchronized void handleTimeout() {
        long userId = getCurrentPlayerId();
        log.info("玩家 {} 叫地主超时，自动不抢", userId);
        handleNotGrab(userId);
    }

    private void moveToNext() {
        currentIndex++;
        if (currentIndex >= playerOrder.size()) {
            endRound();
        } else {
            notifyCurrentPlayer();
        }
    }

    private void endRound() {
        log.info("第 {} 轮叫地主结束", currentRound);
        if (currentLandlordId != 0) {
            determineLandlord();
            return;
        }
        currentRound++;
        currentIndex = 0;
        if (currentRound <= 3) {
            log.info("进入第 {} 轮叫地主", currentRound);
            notifyCurrentPlayer();
        } else {
            state = BiddingState.ALL_PASS;
            assignRandomLandlord();
        }
    }

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

    private void giveExtraCardsToLandlord() {
        DoudizhuPlayer landlord = room.getDoudizhuPlayer(currentLandlordId);
        List<Card> extraCards = gameState.getLandlordExtraCards();
        if (extraCards != null && !extraCards.isEmpty()) {
            landlord.addCards(extraCards);
            log.info("地主拿到底牌: {}", extraCards);
        }
    }

    private void setPlayOrder() {
        List<Long> order = new ArrayList<>();
        int landlordIndex = playerOrder.indexOf(currentLandlordId);
        for (int i = 0; i < playerOrder.size(); i++) {
            order.add(playerOrder.get((landlordIndex + i) % playerOrder.size()));
        }
        gameState.setPlayOrder(order, currentLandlordId);
        log.info("出牌顺序: {}", order);
    }

    private void enterPlayingPhase() {
        // 更新房间展示状态
        room.setGameStatus(DoudizhuGameStatus.PLAYING.name());
        // 更新游戏逻辑状态
        gameState.changeStatus(DoudizhuGameStatus.PLAYING);
        DoudizhuBroadcastKit.broadcastGameStart(room);
        turnManager.startTimeout();
        log.info("房间 {} 进入出牌阶段", room.getRoomId());
    }

    private void notifyCurrentPlayer() {
        long currentPlayerId = getCurrentPlayerId();
        turnManager.setTimeout(this::handleTimeout);
        DoudizhuBroadcastKit.broadcastBiddingTurn(currentPlayerId, currentRound, room);
        log.info("轮到玩家 {} 叫地主，第 {} 轮", currentPlayerId, currentRound);
    }

    private long getCurrentPlayerId() {
        return playerOrder.get(currentIndex);
    }

    private boolean isCurrentPlayer(long userId) {
        return getCurrentPlayerId() == userId;
    }

    public Map<Long, BidRecord> getBidRecords() {
        return new ConcurrentHashMap<>(bidRecords);
    }

    public synchronized void reset() {
        turnManager.cancelTimeout();
        state = BiddingState.WAITING;
        currentRound = 1;
        currentIndex = 0;
        currentLandlordId = 0;
        currentMultiple = 0;
        bidRecords.clear();
        log.info("叫地主管理器已重置");
    }
}
