package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 回归奖励策略 - 无状态版本
 *
 * 功能：召回流失玩家，给予回归奖励
 * 使用场景：玩家超过N天未登录后回归，给予好牌型
 *
 * 设计原则：
 * - 策略本身无状态，流失天数和剩余奖励次数从 DealContext 获取
 * - 不存储玩家数据，数据由调用方（service-user）维护
 *
 * 大厂实践：腾讯游戏回归系统，根据流失天数给予不同奖励
 *
 * @author poker-platform
 */
@Slf4j
public class ReturnBonusStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;

    // 流失天数阈值
    private static final int RETURN_DAYS_1 = 3;    // 3天未登录 -> 基础回归
    private static final int RETURN_DAYS_2 = 7;    // 7天未登录 -> 中级回归
    private static final int RETURN_DAYS_3 = 15;   // 15天未登录 -> 高级回归
    private static final int RETURN_DAYS_4 = 30;   // 30天未登录 -> 顶级回归

    public ReturnBonusStrategy(GameType gameType) {
        this(gameType, true);
    }

    public ReturnBonusStrategy(GameType gameType, boolean isActive) {
        this.gameType = gameType;
        this.isActive = isActive;
    }

    @Override
    public String getName() {
        return "回归奖励策略";
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

        // 从 context 获取回归奖励相关信息
        int remainingBonusGames = context.getRemainingBonusGames();
        if (remainingBonusGames <= 0) {
            return null;
        }

        int daysAway = context.getDaysAway();
        if (daysAway < RETURN_DAYS_1) {
            return null;
        }

        // 注意：这里不消耗奖励次数，只返回牌型
        // 消耗由调用方在发牌后通过 service-user 更新
        log.debug("玩家{}回归奖励触发: 流失{}天, 剩余{}局",
                context.getPlayerId(), daysAway, remainingBonusGames);

        // 根据流失天数决定奖励等级
        return getBonusRank(daysAway);
    }

    /**
     * 根据流失天数获取奖励牌型
     */
    private HandRank getBonusRank(int daysAway) {
        if (daysAway >= RETURN_DAYS_4) {
            log.debug("流失{}天，触发顶级回归奖励", daysAway);
            return getTopBonusRank();
        } else if (daysAway >= RETURN_DAYS_3) {
            log.debug("流失{}天，触发高级回归奖励", daysAway);
            return getHighBonusRank();
        } else if (daysAway >= RETURN_DAYS_2) {
            log.debug("流失{}天，触发中级回归奖励", daysAway);
            return getMidBonusRank();
        } else {
            log.debug("流失{}天，触发基础回归奖励", daysAway);
            return getBaseBonusRank();
        }
    }

    private HandRank getBaseBonusRank() {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_TWO_PAIR;
            case BULL: return HandRank.BULL_BULL;
            default: return HandRank.DOUDIZHU_STRAIGHT;
        }
    }

    private HandRank getMidBonusRank() {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_ROCKET;
            case TEXAS: return HandRank.TEXAS_FLUSH;
            case BULL: return HandRank.BULL_FOUR_BOMB;
            default: return HandRank.DOUDIZHU_BOMB;
        }
    }

    private HandRank getHighBonusRank() {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_ROCKET;
            case TEXAS: return HandRank.TEXAS_FULL_HOUSE;
            case BULL: return HandRank.BULL_FIVE_SMALL;
            default: return HandRank.DOUDIZHU_ROCKET;
        }
    }

    private HandRank getTopBonusRank() {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_ROCKET;
            case TEXAS: return HandRank.TEXAS_ROYAL_FLUSH;
            case BULL: return HandRank.BULL_FIVE_SMALL;
            default: return HandRank.DOUDIZHU_ROCKET;
        }
    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        return List.of();
    }

    @Override
    public double getWeightFactor() {
        return 1.0;
    }

    /**
     * 获取流失天数阈值（供外部查询）
     */
    public static int getReturnDaysThreshold() {
        return RETURN_DAYS_1;
    }

    /**
     * 获取奖励局数限制（供外部查询）
     */
    public static int getBonusGamesLimit() {
        return BONUS_GAMES_LIMIT;
    }

    // ==================== 配置常量（供外部使用） ====================

    /** 回归奖励局数限制 */
    public static final int BONUS_GAMES_LIMIT = 10;
}