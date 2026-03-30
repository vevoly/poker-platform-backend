package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;

import java.util.List;

/**
 * 新手保护策略
 */
public class RookieDealStrategy implements DealStrategy {

    private final GameType gameType;
    private final HandRank rookieRank;

    public RookieDealStrategy(GameType gameType) {
        this.gameType = gameType;
        this.rookieRank = getRookieRank(gameType);
    }

    @Override
    public String getName() { return "新手保护"; }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public GameType getGameType() { return gameType; }

    @Override
    public HandRank getTargetRank(int playerIndex) {
        return rookieRank;
    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        // 新手玩家在索引0位置
        return List.of(0);
    }

    private HandRank getRookieRank(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return HandRank.DOUDIZHU_BOMB;
            case TEXAS: return HandRank.TEXAS_THREE_OF_KIND;
            case BULL: return HandRank.BULL_BULL;
            default: return HandRank.DOUDIZHU_JUNK;
        }
    }
}
