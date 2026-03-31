package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 道具加成策略
 *
 * 功能：使用道具后提高高牌型出现概率
 * 使用场景：商业化道具系统，玩家使用好运卡、如意卡等
 *
 * 大厂实践：腾讯欢乐斗地主道具系统，道具可叠加使用
 *
 * @author poker-platform
 */
@Slf4j
public class ItemBoostStrategy implements DealStrategy {

    private final GameType gameType;
    private final boolean isActive;

    // 玩家生效中的道具
    private static final Map<Long, List<ActiveItem>> ACTIVE_ITEMS = new ConcurrentHashMap<>();

    // 道具类型
    @Getter
    public enum ItemType {
        LUCKY_CARD("好运卡", 0.20, 5),      // 提高20%概率，持续5局
        RUYI_CARD("如意卡", 0.15, 3),       // 提高15%概率，持续3局
        BAODI_CARD("保底卡", 0.0, 1),       // 保底牛牛/炸弹，持续1局
        DOUBLECARD("双倍卡", 0.30, 1);      // 提高30%概率，持续1局

        public final String name;
        public final double boostRate;
        public final int duration;

        ItemType(String name, double boostRate, int duration) {
            this.name = name;
            this.boostRate = boostRate;
            this.duration = duration;
        }
    }

    /**
     * 生效中的道具
     */
    @Data
    public static class ActiveItem {
        public final ItemType type;
        public final int remainingGames;

        public ActiveItem(ItemType type, int remainingGames) {
            this.type = type;
            this.remainingGames = remainingGames;
        }
    }

    public ItemBoostStrategy(GameType gameType) {
        this(gameType, true);
    }

    public ItemBoostStrategy(GameType gameType, boolean isActive) {
        this.gameType = gameType;
        this.isActive = isActive;
    }

    @Override
    public String getName() {
        return "道具加成策略";
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
     * 根据玩家生效道具获取目标牌型
     */
    public HandRank getTargetRank(long playerId) {
        List<ActiveItem> items = ACTIVE_ITEMS.get(playerId);
        if (items == null || items.isEmpty()) {
            return null;
        }

        double totalBoost = 0;
        boolean hasGuarantee = false;

        for (ActiveItem item : items) {
            totalBoost += item.type.boostRate;
            if (item.type == ItemType.BAODI_CARD) {
                hasGuarantee = true;
            }
        }

        // 保底道具优先处理
        if (hasGuarantee) {
            log.debug("玩家{}使用保底卡，触发保底牌型", playerId);
            return getGuaranteeRank();
        }

        // 根据加成系数决定是否发放好牌
        if (Math.random() < getBoostProbability(totalBoost)) {
            HandRank boostedRank = getBoostedRank(totalBoost);
            if (boostedRank != null) {
                log.debug("玩家{}道具加成触发，获得牌型: {}", playerId, boostedRank.getName());
                return boostedRank;
            }
        }

        return null;
    }

    private double getBoostProbability(double totalBoost) {
        // 基础概率20%，每增加0.1加成提高5%
        return 0.2 + totalBoost * 0.5;
    }

    private HandRank getBoostedRank(double totalBoost) {
        switch (gameType) {
            case DOUDIZHU:
                if (Math.random() < 0.1 + totalBoost) {
                    return HandRank.DOUDIZHU_ROCKET;
                }
                if (Math.random() < 0.2 + totalBoost) {
                    return HandRank.DOUDIZHU_BOMB;
                }
                break;
            case TEXAS:
                if (Math.random() < 0.05 + totalBoost) {
                    return HandRank.TEXAS_ROYAL_FLUSH;
                }
                if (Math.random() < 0.1 + totalBoost) {
                    return HandRank.TEXAS_STRAIGHT_FLUSH;
                }
                break;
            case BULL:
                if (Math.random() < 0.08 + totalBoost) {
                    return HandRank.BULL_FIVE_SMALL;
                }
                if (Math.random() < 0.15 + totalBoost) {
                    return HandRank.BULL_BULL;
                }
                break;
        }
        return null;
    }

    private HandRank getGuaranteeRank() {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_FLUSH;
            case BULL: return HandRank.BULL_BULL;
            default: return HandRank.DOUDIZHU_BOMB;
        }
    }

    /**
     * 使用道具
     */
    public static void useItem(long playerId, ItemType type) {
        List<ActiveItem> items = ACTIVE_ITEMS.computeIfAbsent(playerId, k -> new java.util.ArrayList<>());

        // 检查是否已有同类型道具（可叠加）
        items.add(new ActiveItem(type, type.duration));
        log.info("玩家{}使用道具: {}, 持续{}局", playerId, type.name, type.duration);
    }

    /**
     * 对局结束，消耗道具次数
     */
    public static void consumeGame(long playerId) {
        List<ActiveItem> items = ACTIVE_ITEMS.get(playerId);
        if (items == null || items.isEmpty()) {
            return;
        }

        items.removeIf(item -> {
            int remaining = item.remainingGames - 1;
            if (remaining <= 0) {
                log.debug("玩家{}道具{}已失效", playerId, item.type.name);
                return true;
            }
            return false;
        });

        if (items.isEmpty()) {
            ACTIVE_ITEMS.remove(playerId);
        }
    }

    /**
     * 获取玩家生效中的道具
     */
    public static List<ActiveItem> getActiveItems(long playerId) {
        return ACTIVE_ITEMS.getOrDefault(playerId, List.of());
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
