package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.VipConfigData;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * VIP特权策略 - 配置驱动版本
 *
 * 功能：根据VIP等级提高高牌型出现概率
 * 使用场景：商业化变现，VIP玩家享受更好的游戏体验
 *
 * 设计原则：
 * - 策略本身无状态，VIP等级从 DealContext 获取
 * - VIP配置通过 VipConfigData 传入，支持动态调整
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

    // 默认配置（当 context 中没有传入配置时使用）
    private static final double[] DEFAULT_VIP_BOOST = {
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

    // 默认触发概率配置
    private static final double[] DEFAULT_TRIGGER_PROB = {
            0.00,   // V0: 0%
            0.08,   // V1: 8%
            0.10,   // V2: 10%
            0.12,   // V3: 12%
            0.14,   // V4: 14%
            0.16,   // V5: 16%
            0.18,   // V6: 18%
            0.20,   // V7: 20%
            0.22,   // V8: 22%
            0.25    // V9: 25%
    };

    // 默认专属牌型概率配置
    private static final double[] DEFAULT_SPECIAL_PROB = {
            0.00,   // V0-V6: 0%
            0.00,   // V1
            0.00,   // V2
            0.00,   // V3
            0.00,   // V4
            0.00,   // V5
            0.00,   // V6
            0.05,   // V7: 5%
            0.08,   // V8: 8%
            0.12    // V9: 12%
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

        // 获取VIP配置（优先使用传入的配置，否则使用默认配置）
        VipConfigData config = context.getVipConfig();

        // VIP加成触发概率
        double triggerProbability = getTriggerProbability(vipLevel, config);
        log.debug("VIP{} 触发概率: {}", vipLevel, triggerProbability);

        // 检查是否触发VIP加成
        if (ThreadLocalRandom.current().nextDouble() >= triggerProbability) {
            return null;
        }

        // VIP7以上有专属牌型概率
        if (vipLevel >= 7 && shouldTriggerVipSpecial(vipLevel, config)) {
            HandRank specialRank = getRandomVipOnlyRank();
            if (specialRank != null) {
                log.debug("玩家{} VIP{}触发专属牌型: {}",
                        context.getPlayerId(), vipLevel, specialRank.getName());
                return specialRank;
            }
        }

        // 普通牌型加成
        HandRank boostedRank = getBoostedRank(vipLevel, config);
        if (boostedRank != null) {
            log.debug("玩家{} VIP{}触发加成牌型: {}",
                    context.getPlayerId(), vipLevel, boostedRank.getName());
        }
        return boostedRank;
    }

    /**
     * 获取VIP加成触发概率
     */
    private double getTriggerProbability(int vipLevel, VipConfigData config) {
        if (config != null && config.getTriggerProbability(vipLevel) > 0) {
            return config.getTriggerProbability(vipLevel);
        }
        // 使用默认配置
        int index = Math.min(vipLevel, DEFAULT_TRIGGER_PROB.length - 1);
        return DEFAULT_TRIGGER_PROB[index];
    }

    /**
     * 判断是否触发VIP专属牌型
     */
    private boolean shouldTriggerVipSpecial(int vipLevel, VipConfigData config) {
        double probability;
        if (config != null && config.getSpecialProbability(vipLevel) > 0) {
            probability = config.getSpecialProbability(vipLevel);
        } else {
            int index = Math.min(vipLevel, DEFAULT_SPECIAL_PROB.length - 1);
            probability = DEFAULT_SPECIAL_PROB[index];
        }
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
    private HandRank getBoostedRank(int vipLevel, VipConfigData config) {
        double boost = getVipBoost(vipLevel, config);
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
    private double getVipBoost(int vipLevel, VipConfigData config) {
        if (config != null && config.getBoostRate(vipLevel) > 0) {
            return config.getBoostRate(vipLevel);
        }
        int index = Math.min(vipLevel, DEFAULT_VIP_BOOST.length - 1);
        return DEFAULT_VIP_BOOST[index];
    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        return List.of();
    }

    @Override
    public double getWeightFactor() {
        // 返回VIP5的默认加成系数
        return DEFAULT_VIP_BOOST[5];
    }
}
