package com.pokergame.common.deal.strategy;

import com.pokergame.common.activity.ActiveActivity;
import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 活动加成策略 - 无状态版本
 *
 * 功能：活动期间提高高牌型出现概率
 * 使用场景：节日活动、运营活动等
 *
 * 设计原则：
 * - 策略本身无状态，活动配置从 DealContext 获取
 * - 支持多种活动类型，可配置不同加成效果
 *
 * @author poker-platform
 */
@Slf4j
public class EventDealStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;
    private final String eventId;           // 活动ID，用于区分不同活动
    private final double boostRate;          // 加成系数（0-1）
    private final HandRank targetRank;       // 目标牌型（null表示提升整体概率）
    private final List<HandRank> targetRanks; // 目标牌型列表（多个）

    /**
     * 简化构造函数（保持向后兼容）
     */
    public EventDealStrategy(GameType gameType, double boostRate) {
        this(gameType, "default_event", boostRate, null, null);
    }

    /**
     * 完整构造函数
     * @param gameType 游戏类型
     * @param eventId 活动ID
     * @param boostRate 加成系数
     * @param targetRank 目标牌型（单牌型加成）
     * @param targetRanks 目标牌型列表（多牌型加成）
     */
    public EventDealStrategy(GameType gameType, String eventId,
                             double boostRate, HandRank targetRank,
                             List<HandRank> targetRanks) {
        this.gameType = gameType;
        this.eventId = eventId;
        this.boostRate = Math.min(1.0, Math.max(0, boostRate));
        this.targetRank = targetRank != null ? targetRank : getDefaultTargetRank(gameType);
        this.targetRanks = targetRanks;
        this.isActive = true;

        log.info("活动加成策略初始化: game={}, event={}, boostRate={}, target={}",
                gameType, eventId, boostRate,
                targetRanks != null ? targetRanks : targetRank);
    }

    @Override
    public String getName() {
        return "活动加成策略[" + eventId + "]";
    }

    @Override
    public boolean isEnabled() {
        return isActive && boostRate > 0;
    }

    @Override
    public GameType getGameType() { return gameType; }

    @Override
    public HandRank getTargetRank(DealContext context) {
        // 检查玩家是否参与活动
        if (!isPlayerInEvent(context)) {
            return null;
        }

        // 检查是否触发加成
        if (!shouldTriggerBoost()) {
            return null;
        }

        // 返回目标牌型
        if (targetRanks != null && !targetRanks.isEmpty()) {
            // 从目标牌型列表中随机选择一个
            int index = ThreadLocalRandom.current().nextInt(targetRanks.size());
            HandRank selectedRank = targetRanks.get(index);
            log.debug("活动[{}]触发多牌型加成: player={}, rank={}",
                    eventId, context.getPlayerId(), selectedRank.getName());
            return selectedRank;
        }

        log.debug("活动[{}]触发加成: player={}, rank={}",
                eventId, context.getPlayerId(), targetRank.getName());
        return targetRank;
    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        // 活动加成不依赖玩家索引，而是在 getTargetRank 中根据 context 判断
        // 返回空列表，表示所有玩家都可能触发（但需要满足活动参与条件）
        return List.of();
    }

    @Override
    public double getWeightFactor() {
        return boostRate;
    }

    /**
     * 判断玩家是否参与活动
     */
    private boolean isPlayerInEvent(DealContext context) {
        // 从 context 获取活动列表，检查是否包含当前活动
        List<ActiveActivity> activeEvents = context.getActiveEvents();
        if (activeEvents == null || activeEvents.isEmpty()) {
            return false;
        }

        return activeEvents.stream()
                .anyMatch(event -> event.getActivityId().equals(eventId) && event.isActive());
    }

    /**
     * 判断是否触发加成（概率判定）
     */
    private boolean shouldTriggerBoost() {
        // 加成系数越高，触发概率越大
        double probability = 0.1 + boostRate * 0.5;  // 基础10%，最高60%
        probability = Math.min(probability, 0.8);
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    /**
     * 获取默认目标牌型（向后兼容）
     */
    private HandRank getDefaultTargetRank(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_STRAIGHT_FLUSH;
            case BULL: return HandRank.BULL_FOUR_BOMB;
            default: return HandRank.DOUDIZHU_JUNK;
        }
    }


}
