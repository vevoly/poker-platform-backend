package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;

import java.util.List;

/**
 * AI难度策略
 */
public class AIDealStrategy implements DealStrategy {

    private final GameType gameType;
    private final int difficulty;  // 1-10
    private final HandRank[] rankLevels;

    public AIDealStrategy(GameType gameType, int difficulty) {
        this.gameType = gameType;
        this.difficulty = Math.max(1, Math.min(10, difficulty));
        this.rankLevels = getRankLevels(gameType);
    }

    @Override
    public String getName() { return "AI难度-" + difficulty; }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public GameType getGameType() { return gameType; }

    @Override
    public HandRank getTargetRank(int playerIndex) {
        // 难度越高，目标牌型越强
        int levelIndex = Math.min(rankLevels.length - 1, (difficulty - 1) / 2);
        return rankLevels[levelIndex];
    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        // 所有AI玩家
        List<Integer> all = new java.util.ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            all.add(i);
        }
        return all;
    }

    private HandRank[] getRankLevels(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU:
                return new HandRank[]{
                        HandRank.DOUDIZHU_SINGLE,   // 难度1-2
                        HandRank.DOUDIZHU_PAIR,     // 难度3-4
                        HandRank.DOUDIZHU_STRAIGHT, // 难度5-6
                        HandRank.DOUDIZHU_BOMB,     // 难度7-8
                        HandRank.DOUDIZHU_ROCKET    // 难度9-10
                };
            case TEXAS:
                return new HandRank[]{
                        HandRank.TEXAS_HIGH_CARD,
                        HandRank.TEXAS_ONE_PAIR,
                        HandRank.TEXAS_TWO_PAIR,
                        HandRank.TEXAS_THREE_OF_KIND,
                        HandRank.TEXAS_STRAIGHT,
                        HandRank.TEXAS_FLUSH,
                        HandRank.TEXAS_FULL_HOUSE,
                        HandRank.TEXAS_FOUR_OF_KIND,
                        HandRank.TEXAS_STRAIGHT_FLUSH
                };
            default:
                return new HandRank[]{HandRank.DOUDIZHU_JUNK};
        }
    }
}
