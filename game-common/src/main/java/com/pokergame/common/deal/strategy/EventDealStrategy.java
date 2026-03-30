package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;

import java.util.List;

/**
 * 活动加成策略
 */
public class EventDealStrategy implements DealStrategy {

    private final GameType gameType;
    private final double boostRate;
    private final HandRank boostedRank;

    public EventDealStrategy(GameType gameType, double boostRate) {
        this.gameType = gameType;
        this.boostRate = Math.min(1.0, Math.max(0, boostRate));
        this.boostedRank = getBoostedRank(gameType);
    }

    @Override
    public String getName() { return "活动加成"; }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public GameType getGameType() { return gameType; }

    @Override
    public HandRank getTargetRank(int playerIndex) {
        return boostedRank;
    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        // 随机选择1-2个玩家
        int count = Math.max(1, playerCount / 3);
        java.util.Set<Integer> indices = new java.util.HashSet<>();
        java.util.Random random = new java.util.Random();
        while (indices.size() < count) {
            indices.add(random.nextInt(playerCount));
        }
        return new java.util.ArrayList<>(indices);
    }

    @Override
    public double getWeightFactor() {
        return boostRate;
    }

    private HandRank getBoostedRank(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_STRAIGHT_FLUSH;
            case BULL: return HandRank.BULL_FOUR_BOMB;
            default: return HandRank.DOUDIZHU_JUNK;
        }
    }
}
