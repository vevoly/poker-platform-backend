package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VIP特权策略
 *
 * 功能：根据VIP等级提高高牌型出现概率
 * 使用场景：商业化变现，VIP玩家享受更好的游戏体验
 *
 * 大厂实践：腾讯欢乐斗地主VIP系统，等级越高特权越多
 *
 * @author poker-platform
 */
@Slf4j
public class VipDealStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;
    private final Map<Long, Integer> vipLevelCache = new ConcurrentHashMap<>();

    // VIP等级对应的加成系数
    private static final double[] VIP_BOOST = {
            0.0,    // V0: 0% 加成
            0.05,   // V1: 5% 加成
            0.10,   // V2: 10% 加成
            0.15,   // V3: 15% 加成
            0.20,   // V4: 20% 加成
            0.25,   // V5: 25% 加成
            0.30,   // V6: 30% 加成
            0.35,   // V7: 35% 加成
            0.40,   // V8: 40% 加成
            0.50    // V9: 50% 加成
    };

    // VIP专属牌型池
    private static final HandRank[] VIP_ONLY_RANKS = {
            HandRank.DOUDIZHU_ROCKET,
            HandRank.TEXAS_ROYAL_FLUSH,
            HandRank.BULL_FIVE_SMALL
    };

    public VipDealStrategy(GameType gameType) {
        this(gameType, true);
    }

    public VipDealStrategy(GameType gameType, boolean isActive) {
        this.gameType = gameType;
        this.isActive = isActive;
    }

    @Override
    public String getName() {
        return "VIP特权策略";
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    @Override
    public GameType getGameType() {
        return gameType;
    }

    @Override
    public HandRank getTargetRank(int playerIndex) {
        throw new UnsupportedOperationException("请使用 getTargetRank(playerId) 方法");
    }

    /**
     * 根据VIP等级获取目标牌型
     */
    public HandRank getTargetRank(long playerId, int vipLevel) {
        if (!shouldApplyVipBoost(vipLevel)) {
            return null;
        }

        // VIP专属牌型概率
        if (vipLevel >= 7 && Math.random() < getVipSpecialProb(vipLevel)) {
            return getRandomVipOnlyRank();
        }

        // 普通牌型加成
        return getBoostedRank(vipLevel);
    }

    private boolean shouldApplyVipBoost(int vipLevel) {
        return vipLevel > 0 && Math.random() < getBoostProbability(vipLevel);
    }

    private double getBoostProbability(int vipLevel) {
        // VIP等级越高，触发概率越高
        return Math.min(0.3 + vipLevel * 0.03, 0.6);
    }

    private double getVipSpecialProb(int vipLevel) {
        // VIP7以上才有专属牌型
        if (vipLevel < 7) return 0;
        return 0.01 + (vipLevel - 7) * 0.005;
    }

    private HandRank getBoostedRank(int vipLevel) {
        double boost = VIP_BOOST[Math.min(vipLevel, VIP_BOOST.length - 1)];

        switch (gameType) {
            case DOUDIZHU:
                // 提升炸弹/王炸出现概率
                if (Math.random() < 0.1 + boost) {
                    return HandRank.DOUDIZHU_BOMB;
                }
                if (Math.random() < 0.05 + boost * 0.5) {
                    return HandRank.DOUDIZHU_ROCKET;
                }
                break;
            case TEXAS:
                if (Math.random() < 0.05 + boost) {
                    return HandRank.TEXAS_STRAIGHT_FLUSH;
                }
                if (Math.random() < 0.02 + boost * 0.5) {
                    return HandRank.TEXAS_ROYAL_FLUSH;
                }
                break;
            case BULL:
                if (Math.random() < 0.08 + boost) {
                    return HandRank.BULL_BULL;
                }
                if (Math.random() < 0.03 + boost * 0.5) {
                    return HandRank.BULL_FIVE_SMALL;
                }
                break;
        }
        return null;
    }

    private HandRank getRandomVipOnlyRank() {
        int index = (int) (Math.random() * VIP_ONLY_RANKS.length);
        return VIP_ONLY_RANKS[index];
    }

    /**
     * 设置玩家VIP等级
     */
    public void setVipLevel(long playerId, int level) {
        vipLevelCache.put(playerId, level);
        log.debug("玩家{} VIP等级设置为: {}", playerId, level);
    }

    /**
     * 获取玩家VIP等级
     */
    public int getVipLevel(long playerId) {
        return vipLevelCache.getOrDefault(playerId, 0);
    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        return List.of();
    }

    @Override
    public double getWeightFactor() {
        return 1.0;
    }
}
