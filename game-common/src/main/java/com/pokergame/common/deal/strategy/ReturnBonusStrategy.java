package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 回归奖励策略
 *
 * 功能：召回流失玩家，给予回归奖励
 * 使用场景：玩家超过N天未登录后回归，给予好牌型
 *
 * 大厂实践：腾讯游戏回归系统，根据流失天数给予不同奖励
 *
 * @author poker-platform
 */
@Slf4j
public class ReturnBonusStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;

    // 玩家最后登录时间记录
    private static final Map<Long, Long> LAST_LOGIN_TIME = new ConcurrentHashMap<>();

    // 流失天数阈值
    private static final int RETURN_DAYS_1 = 3;    // 3天未登录 -> 基础回归
    private static final int RETURN_DAYS_2 = 7;    // 7天未登录 -> 中级回归
    private static final int RETURN_DAYS_3 = 15;   // 15天未登录 -> 高级回归
    private static final int RETURN_DAYS_4 = 30;   // 30天未登录 -> 顶级回归

    // 回归奖励局数限制（回归后前N局享受奖励）
    private static final int BONUS_GAMES_LIMIT = 10;

    // 玩家剩余奖励局数
    private static final Map<Long, Integer> REMAINING_BONUS_GAMES = new ConcurrentHashMap<>();

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
    public HandRank getTargetRank(int playerIndex) {
        throw new UnsupportedOperationException("请使用 getTargetRank(playerId) 方法");
    }

    /**
     * 检查玩家是否回归并获取奖励牌型
     */
    public HandRank getTargetRank(long playerId) {
        // 检查是否还有剩余奖励局数
        Integer remaining = REMAINING_BONUS_GAMES.get(playerId);
        if (remaining == null || remaining <= 0) {
            return null;
        }

        // 获取流失天数
        int daysAway = getDaysAway(playerId);
        if (daysAway < RETURN_DAYS_1) {
            return null;
        }

        // 消耗一次奖励机会
        REMAINING_BONUS_GAMES.put(playerId, remaining - 1);
        log.debug("玩家{}回归奖励剩余次数: {}", playerId, remaining - 1);

        // 根据流失天数决定奖励等级
        return getBonusRank(daysAway);
    }

    private HandRank getBonusRank(int daysAway) {
        if (daysAway >= RETURN_DAYS_4) {
            log.debug("玩家流失{}天，触发顶级回归奖励", daysAway);
            return getTopBonusRank();
        } else if (daysAway >= RETURN_DAYS_3) {
            log.debug("玩家流失{}天，触发高级回归奖励", daysAway);
            return getHighBonusRank();
        } else if (daysAway >= RETURN_DAYS_2) {
            log.debug("玩家流失{}天，触发中级回归奖励", daysAway);
            return getMidBonusRank();
        } else {
            log.debug("玩家流失{}天，触发基础回归奖励", daysAway);
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

    /**
     * 获取玩家流失天数
     */
    private int getDaysAway(long playerId) {
        Long lastLogin = LAST_LOGIN_TIME.get(playerId);
        if (lastLogin == null) {
            return 0;
        }
        long days = (System.currentTimeMillis() - lastLogin) / (24 * 60 * 60 * 1000);
        return (int) days;
    }

    /**
     * 记录玩家登录（在登录时调用）
     */
    public static void recordLogin(long playerId) {
        Long lastLogin = LAST_LOGIN_TIME.get(playerId);
        LAST_LOGIN_TIME.put(playerId, System.currentTimeMillis());

        if (lastLogin != null) {
            int daysAway = (int)((System.currentTimeMillis() - lastLogin) / (24 * 60 * 60 * 1000));
            if (daysAway >= RETURN_DAYS_1) {
                // 触发回归奖励，给予10局奖励机会
                REMAINING_BONUS_GAMES.put(playerId, BONUS_GAMES_LIMIT);
                log.info("玩家{}流失{}天回归，给予{}局回归奖励", playerId, daysAway, BONUS_GAMES_LIMIT);
            }
        }
    }

    /**
     * 获取剩余奖励局数
     */
    public static int getRemainingBonusGames(long playerId) {
        return REMAINING_BONUS_GAMES.getOrDefault(playerId, 0);
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
