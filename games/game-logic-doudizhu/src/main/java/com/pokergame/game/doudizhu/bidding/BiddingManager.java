package com.pokergame.game.doudizhu.bidding;

import com.pokergame.common.card.Card;
import com.pokergame.game.doudizhu.broadcast.DoudizhuBroadcastKit;
import com.pokergame.game.doudizhu.enums.BiddingState;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
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
 * 职责：
 * 1. 管理叫地主流程
 * 2. 记录每个玩家的叫地主情况
 * 3. 确定地主
 * 4. 处理超时
 *
 * @author poker-platform
 */
@Slf4j
public class BiddingManager {

    /** 房间 */
    private final DoudizhuRoom room;
    /** 玩家顺序 */
    private final List<Long> playerOrder;
    /** 叫地主记录 */
    private final Map<Long, BidRecord> bidRecords = new ConcurrentHashMap<>();
    /** 叫地主状态 */
    @Getter
    private BiddingState state = BiddingState.WAITING;
    /** 当前轮数 */
    private int currentIndex = 0;
    /** 当前轮数 */
    @Getter
    private int currentRound = 1;  // 当前第几轮叫地主
    /** 当前地主 */
    @Getter
    private long currentLandlordId = 0;
    /** 当前倍数 */
    @Getter
    private int currentMultiple = 0;

    // 超时定时器
    private final DoudizhuTurnManager turnManager;

    public BiddingManager(DoudizhuRoom room, List<Long> playerOrder) {
        this.room = room;
        this.playerOrder = new ArrayList<>(playerOrder);
        this.turnManager = room.getTurnManager();
    }

    /**
     * 开始叫地主流程
     */
    public void start() {
        log.info("房间 {} 开始叫地主阶段", room.getRoomId());

        state = BiddingState.WAITING;
        currentRound = 1;
        currentIndex = 0;
        currentLandlordId = 0;
        currentMultiple = 0;
        bidRecords.clear();

        // 通知第一个玩家叫地主
        notifyCurrentPlayer();
    }

    /**
     * 处理抢地主
     */
    public synchronized void handleGrab(long userId, int multiple) {
        log.info("玩家 {} 抢地主，倍数: {}", userId, multiple);

        // 1. 状态校验
        if (state != BiddingState.WAITING) {
            log.warn("状态错误，当前状态: {}", state);
            return;
        }

        // 2. 回合校验
        if (!isCurrentPlayer(userId)) {
            log.warn("不是当前玩家，当前玩家: {}", getCurrentPlayerId());
            return;
        }

        // 3. 取消超时定时器
        turnManager.cancelTimeout();

        // 4. 记录抢地主
        BidRecord record = BidRecord.grab(userId, multiple);
        bidRecords.put(userId, record);

        // 5. 更新地主信息
        currentLandlordId = userId;
        currentMultiple = multiple;

        // 6. 广播抢地主
        DoudizhuBroadcastKit.broadcastGrabLandlord(userId, multiple, room);

        // 7. 移动到下一个玩家
        moveToNext();
    }

    /**
     * 处理不抢地主
     */
    public synchronized void handleNotGrab(long userId) {
        log.info("玩家 {} 不抢地主", userId);

        // 1. 状态校验
        if (state != BiddingState.WAITING) {
            log.warn("状态错误，当前状态: {}", state);
            return;
        }

        // 2. 回合校验
        if (!isCurrentPlayer(userId)) {
            log.warn("不是当前玩家，当前玩家: {}", getCurrentPlayerId());
            return;
        }

        // 3. 取消超时定时器
        turnManager.cancelTimeout();

        // 4. 记录不抢
        BidRecord record = BidRecord.notGrab(userId);
        bidRecords.put(userId, record);

        // 5. 广播不抢
        DoudizhuBroadcastKit.broadcastNotGrab(userId, room);

        // 6. 移动到下一个玩家
        moveToNext();
    }

    /**
     * 处理超时（玩家没有在时间内做出选择）
     */
    public synchronized void handleTimeout() {
        long userId = getCurrentPlayerId();
        log.info("玩家 {} 叫地主超时，自动不抢", userId);

        // 自动不抢
        handleNotGrab(userId);
    }

    /**
     * 移动到下一个玩家
     */
    private void moveToNext() {
        currentIndex++;

        // 检查是否一轮结束
        if (currentIndex >= playerOrder.size()) {
            endRound();
            return;
        }

        // 通知下一个玩家
        notifyCurrentPlayer();
    }

    /**
     * 结束一轮叫地主
     */
    private void endRound() {
        log.info("第 {} 轮叫地主结束", currentRound);

        // 检查是否有玩家抢地主
        if (currentLandlordId != 0) {
            // 有玩家抢地主，确定地主
            determineLandlord();
            return;
        }

        // 没有玩家抢地主，检查是否还有下一轮
        currentRound++;
        currentIndex = 0;

        if (currentRound <= 3) {
            // 进入下一轮叫地主
            log.info("进入第 {} 轮叫地主", currentRound);
            notifyCurrentPlayer();
        } else {
            // 三轮都无人抢，随机分配地主
            state = BiddingState.ALL_PASS;
            assignRandomLandlord();
        }
    }

    /**
     * 确定地主（有玩家抢地主时）
     */
    private void determineLandlord() {
        state = BiddingState.LANDLORD_DETERMINED;

        log.info("地主确定: 玩家 {}, 倍数: {}", currentLandlordId, currentMultiple);

        // 设置地主
        room.setLandlordId(currentLandlordId);
        room.setCurrentMultiple(currentMultiple);

        // 地主拿底牌
        giveExtraCardsToLandlord();

        // 设置出牌顺序
        setPlayOrder();

        // 进入出牌阶段
        enterPlayingPhase();
    }

    /**
     * 随机分配地主（无人抢地主时）
     */
    private void assignRandomLandlord() {
        log.info("所有玩家都不抢，随机分配地主");

        // 随机选择一个玩家作为地主
        int randomIndex = (int) (Math.random() * playerOrder.size());
        currentLandlordId = playerOrder.get(randomIndex);
        currentMultiple = 1;

        state = BiddingState.LANDLORD_DETERMINED;

        room.setLandlordId(currentLandlordId);
        room.setCurrentMultiple(currentMultiple);

        // 地主拿底牌
        giveExtraCardsToLandlord();

        // 设置出牌顺序
        setPlayOrder();

        // 进入出牌阶段
        enterPlayingPhase();
    }

    /**
     * 地主拿底牌
     */
    private void giveExtraCardsToLandlord() {
        DoudizhuPlayer landlord = room.getDoudizhuPlayer(currentLandlordId);
        List<Card> extraCards = room.getLandlordExtraCards();

        if (extraCards != null && !extraCards.isEmpty()) {
            landlord.addCards(extraCards);
            log.info("地主拿到底牌: {}", extraCards);
        }
    }

    /**
     * 设置出牌顺序（地主先出）
     */
    private void setPlayOrder() {
        List<Long> playOrder = new ArrayList<>();
        int landlordIndex = playerOrder.indexOf(currentLandlordId);

        for (int i = 0; i < playerOrder.size(); i++) {
            int index = (landlordIndex + i) % playerOrder.size();
            playOrder.add(playerOrder.get(index));
        }

        room.setPlayOrder(playOrder, currentLandlordId);
        log.info("出牌顺序: {}", playOrder);
    }

    /**
     * 进入出牌阶段
     */
    private void enterPlayingPhase() {
        room.setGameStatus(DoudizhuGameStatus.PLAYING);

        // 广播游戏开始
        DoudizhuBroadcastKit.broadcastGameStart(room);

        // 启动第一回合定时器
        turnManager.startTimeout();

        log.info("房间 {} 进入出牌阶段", room.getRoomId());
    }

    /**
     * 通知当前玩家叫地主
     */
    private void notifyCurrentPlayer() {
        long currentPlayerId = getCurrentPlayerId();

        // 启动超时定时器（15秒）
        turnManager.setTimeout(() -> {
            handleTimeout();
        });

        // 广播轮到该玩家叫地主
        DoudizhuBroadcastKit.broadcastBiddingTurn(currentPlayerId, currentRound, room);

        log.info("轮到玩家 {} 叫地主，第 {} 轮", currentPlayerId, currentRound);
    }

    /**
     * 获取当前玩家ID
     */
    private long getCurrentPlayerId() {
        return playerOrder.get(currentIndex);
    }

    /**
     * 判断是否为当前玩家
     */
    private boolean isCurrentPlayer(long userId) {
        return getCurrentPlayerId() == userId;
    }

    // ==================== Getter 方法 ====================

    public Map<Long, BidRecord> getBidRecords() {
        return new ConcurrentHashMap<>(bidRecords);
    }
}
