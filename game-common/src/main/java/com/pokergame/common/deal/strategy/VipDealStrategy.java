package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * VIP特权策略 - 无状态版本
 *
 * 功能：根据VIP等级提高高牌型出现概率
 * 使用场景：商业化变现，VIP玩家享受更好的游戏体验
 *
 * 设计原则：
 * - 策略本身无状态，VIP等级从 DealContext 获取
 * - 不存储玩家数据，数据由调用方（service-user）维护
 *
 * 大厂实践：腾讯欢乐斗地主VIP系统，等级越高特权越多
 *
 * @author poker-platform
 */
@Slf4j
public class VipDealStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;

    // VIP等级对应的加成系数
    private static final double[] VIP_BOOST = {
            0.0,    // V0: 0% 加成
            0.10,   // V1: 10% 加成
            0.15,   // V2: 15% 加成
            0.20,   // V3: 20% 加成
            0.25,   // V4: 25% 加成
            0.30,   // V5: 30% 加成
            0.35,   // V6: 35% 加成
            0.40,   // V7: 40% 加成
            0.45,   // V8: 45% 加成
            0.50    // V9: 50% 加成
    };

    // VIP专属牌型池（按游戏类型分组）
    private static final HandRank[] DOUDIZHU_VIP_RANKS = {
            HandRank.DOUDIZHU_ROCKET,
            HandRank.DOUDIZHU_BOMB
    };

    private static final HandRank[] TEXAS_VIP_RANKS = {
            HandRank.TEXAS_ROYAL_FLUSH,
            HandRank.TEXAS_STRAIGHT_FLUSH,
            HandRank.TEXAS_FOUR_OF_KIND
    };

    private static final HandRank[] BULL_VIP_RANKS = {
            HandRank.BULL_FIVE_SMALL,
            HandRank.BULL_FOUR_BOMB,
            HandRank.BULL_BULL
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
    public HandRank getTargetRank(DealContext context) {
        if (context == null) {
            return null;
        }

        int vipLevel = context.getVipLevel();
        if (vipLevel <= 0) {
            return null;
        }

        // VIP加成触发概率（VIP等级越高，触发概率越高）
        double triggerProbability = 0.2 + vipLevel * 0.05;
        triggerProbability = Math.min(triggerProbability, 0.8);

        log.debug("VIP{} 触发概率: {}", vipLevel, triggerProbability);

        // 检查是否触发VIP加成
        if (ThreadLocalRandom.current().nextDouble() >= triggerProbability) {
            return null;
        }

        // VIP7以上有专属牌型概率
        if (vipLevel >= 7 && shouldTriggerVipSpecial(vipLevel)) {
            HandRank specialRank = getRandomVipOnlyRank();
            if (specialRank != null) {
                log.debug("玩家{} VIP{}触发专属牌型: {}",
                        context.getPlayerId(), vipLevel, specialRank.getName());
                return specialRank;
            }
        }

        // 普通牌型加成
        HandRank boostedRank = getBoostedRank(vipLevel);
        if (boostedRank != null) {
            log.debug("玩家{} VIP{}触发加成牌型: {}",
                    context.getPlayerId(), vipLevel, boostedRank.getName());
        }
        return boostedRank;
    }

    /**
     * 判断是否触发VIP专属牌型
     */
    private boolean shouldTriggerVipSpecial(int vipLevel) {
        // VIP7: 5%, VIP8: 8%, VIP9: 12%
        double probability = 0.05 + (vipLevel - 7) * 0.03;
        probability = Math.min(probability, 0.15);
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    /**
     * 获取VIP专属牌型
     */
    private HandRank getRandomVipOnlyRank() {
        HandRank[] ranks = getVipOnlyRanks();
        if (ranks == null || ranks.length == 0) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(ranks.length);
        return ranks[index];
    }

    /**
     * 根据游戏类型获取VIP专属牌型池
     */
    private HandRank[] getVipOnlyRanks() {
        switch (gameType) {
            case DOUDIZHU:
                return DOUDIZHU_VIP_RANKS;
            case TEXAS:
                return TEXAS_VIP_RANKS;
            case BULL:
                return BULL_VIP_RANKS;
            default:
                return new HandRank[0];
        }
    }

    /**
     * 获取VIP加成后的牌型
     */
    private HandRank getBoostedRank(int vipLevel) {
        double boost = getVipBoost(vipLevel);
        double random = ThreadLocalRandom.current().nextDouble();

        switch (gameType) {
            case DOUDIZHU:
                // 王炸：基础8% + VIP加成
                if (random < 0.08 + boost) {
                    return HandRank.DOUDIZHU_ROCKET;
                }
                // 炸弹：基础15% + VIP加成
                if (random < 0.15 + boost) {
                    return HandRank.DOUDIZHU_BOMB;
                }
                // 顺子：基础25% + VIP加成
                if (random < 0.25 + boost * 0.8) {
                    return HandRank.DOUDIZHU_STRAIGHT;
                }
                break;
            case TEXAS:
                // 皇家同花顺：基础2% + VIP加成
                if (random < 0.02 + boost * 0.5) {
                    return HandRank.TEXAS_ROYAL_FLUSH;
                }
                // 同花顺：基础5% + VIP加成
                if (random < 0.05 + boost) {
                    return HandRank.TEXAS_STRAIGHT_FLUSH;
                }
                // 四条/葫芦：基础10% + VIP加成
                if (random < 0.10 + boost) {
                    return HandRank.TEXAS_FOUR_OF_KIND;
                }
                break;
            case BULL:
                // 五小牛：基础2% + VIP加成
                if (random < 0.02 + boost * 0.5) {
                    return HandRank.BULL_FIVE_SMALL;
                }
                // 四炸：基础5% + VIP加成
                if (random < 0.05 + boost) {
                    return HandRank.BULL_FOUR_BOMB;
                }
                // 牛牛：基础12% + VIP加成
                if (random < 0.12 + boost) {
                    return HandRank.BULL_BULL;
                }
                break;
        }
        return null;
    }

    /**
     * 获取VIP加成系数
     */
    private double getVipBoost(int vipLevel) {
        int index = Math.min(vipLevel, VIP_BOOST.length - 1);
        return VIP_BOOST[index];
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
