package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 新手保护策略 - 无状态版本
 *
 * 功能：新手玩家获得更好的牌型，提升新手体验
 * 使用场景：新注册玩家、低对局数玩家
 *
 * 设计原则：
 * - 策略本身无状态，新手判断从 DealContext 获取
 * - 新手条件可配置（注册时间、对局数等）
 *
 * 大厂实践：腾讯新手保护系统，根据注册天数和总对局数判断
 *
 * @author poker-platform
 */
@Slf4j
public class RookieDealStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;

    // 新手判断阈值（可配置）
    private static final int MAX_DAYS_SINCE_REGISTER = 7;      // 注册7天内
    private static final int MAX_TOTAL_GAMES = 10;              // 总对局少于50局

    public RookieDealStrategy(GameType gameType) {
        this(gameType, true);
    }

    public RookieDealStrategy(GameType gameType, boolean isActive) {
        this.gameType = gameType;
        this.isActive = isActive;
    }

    @Override
    public String getName() {
        return "新手保护策略";
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

        // 从 context 获取新手状态（由 service-user 计算）
        // 方式1：直接使用传入的 isRookie 标志
        if (context.isRookie()) {
            log.debug("玩家{}触发新手保护，目标牌型: {}",
                    context.getPlayerId(), getRookieRank().getName());
            return getRookieRank();
        }

        // 方式2：也可以根据 context 中的数据进行判断（备选方案）
        // 如果调用方没有设置 isRookie，可以在这里计算
        if (isRookieByContext(context)) {
            log.debug("玩家{}触发新手保护（自动判定），目标牌型: {}",
                    context.getPlayerId(), getRookieRank().getName());
            return getRookieRank();
        }

        return null;
    }

    /**
     * 根据 context 中的数据判断是否为新手（备选方案）
     */
    private boolean isRookieByContext(DealContext context) {
        // 检查注册时间（如果有）
        long registerTime = context.getRegisterTime();
        if (registerTime > 0) {
            long daysSinceRegister = (System.currentTimeMillis() - registerTime) / (24 * 60 * 60 * 1000);
            if (daysSinceRegister <= MAX_DAYS_SINCE_REGISTER) {
                return true;
            }
        }

        // 检查总对局数
        int totalGames = context.getTotalGames();
        if (totalGames <= MAX_TOTAL_GAMES) {
            return true;
        }

        return false;
    }

    /**
     * 获取新手保护牌型
     */
    private HandRank getRookieRank() {
        switch (gameType) {
            case DOUDIZHU:
                return HandRank.DOUDIZHU_BOMB;
            case TEXAS:
                return HandRank.TEXAS_THREE_OF_KIND;
            case BULL:
                return HandRank.BULL_BULL;
            default:
                return HandRank.DOUDIZHU_JUNK;
        }
    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        // 新手保护不依赖玩家索引，在 getTargetRank 中根据 context 判断
        // 返回空列表，表示所有玩家都可能触发（但需要满足新手条件）
        return List.of();
    }

    @Override
    public double getWeightFactor() {
        return 1.0;
    }

    /**
     * 获取新手判断阈值（供外部查询）
     */
    public static int getMaxDaysSinceRegister() {
        return MAX_DAYS_SINCE_REGISTER;
    }

    /**
     * 获取新手总对局数阈值（供外部查询）
     */
    public static int getMaxTotalGames() {
        return MAX_TOTAL_GAMES;
    }
}
