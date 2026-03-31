package com.pokergame.common.deal.strategy;

import com.pokergame.common.deal.DealContext;
import com.pokergame.common.deal.DealStrategy;
import com.pokergame.common.deal.HandRank;
import com.pokergame.common.deal.pool.HandRankPool;
import com.pokergame.common.deal.pool.HandRankPoolId;
import com.pokergame.common.deal.pool.HandRankPoolManager;
import com.pokergame.common.game.GameType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * AI难度策略
 *
 * 功能：根据难度等级控制AI获得的牌型强度
 *
 * 核心概念 - 牌型池（HandRankPool）：
 * - 每个难度等级对应一个牌型池
 * - 牌型池包含该难度下AI可能获得的牌型及权重
 * - 难度越高，池中高等级牌型的权重越大
 *
 * @author poker-platform
 */
@Slf4j
public class AIDealStrategy implements DealStrategy {

    private final GameType gameType;
    @Getter
    private final int difficulty;  // 1-10
    private final HandRankPool rankPool;
    @Getter
    private HandRankPoolId poolId;

    public AIDealStrategy(GameType gameType, int difficulty) {
        this.gameType = gameType;
        this.difficulty = Math.max(1, Math.min(10, difficulty));
        // 使用枚举获取对应的牌型池ID
        this.poolId = HandRankPoolId.forAIDifficulty(gameType, this.difficulty);
        // 从管理器获取牌型池
        HandRankPoolManager manager = HandRankPoolManager.getInstance();
        HandRankPool pool = manager.getPool(poolId.getPoolId());

        if (pool == null) {
            log.warn("牌型池[{}]不存在，使用基础池", poolId.getPoolId());
            HandRankPoolId basePoolId = HandRankPoolId.getBasePool(gameType);
            pool = manager.getPool(basePoolId.getPoolId());
            this.poolId = basePoolId;
        }

        this.rankPool = pool;
        log.info("AI难度策略初始化: game={}, difficulty={}, pool={}, range={}",
                gameType, difficulty, poolId.getPoolId(),
                poolId.getDifficultyRange() != null ? poolId.getDifficultyRange().getName() : "base");
    }

    @Override
    public String getName() {
        return "AI难度策略-" + difficulty + "[" + poolId.getDisplayName() + "]";
    }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public GameType getGameType() { return gameType; }

    @Override
    public HandRank getTargetRank(DealContext context) {
        if (!context.isAI()) {
            return null;
        }
        return rankPool.selectRandom();
    }

    @Override
    public List<Integer> getSpecialPlayerIndices(int playerCount) {
        return List.of();
    }

    @Override
    public double getWeightFactor() {
        // 根据难度范围返回权重因子
        if (poolId.getDifficultyRange() != null) {
            int rangeValue = poolId.getDifficultyRange().ordinal() + 1;
            return 0.3 + rangeValue * 0.07;
        }
        return 0.5;
    }

}
