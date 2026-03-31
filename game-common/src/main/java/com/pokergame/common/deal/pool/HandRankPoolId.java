package com.pokergame.common.deal.pool;

import com.pokergame.common.game.GameType;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 牌型池标识枚举
 *
 * 统一管理所有牌型池的ID
 *
 * @author poker-platform
 */
@Getter
public enum HandRankPoolId {

    // ========== 斗地主牌型池 ==========
    DOUDIZHU_BASE(GameType.DOUDIZHU, "base", "斗地主基础池"),
    DOUDIZHU_AI_EASY(GameType.DOUDIZHU, "ai_easy", "斗地主AI简单池", DifficultyRange.EASY),
    DOUDIZHU_AI_NORMAL(GameType.DOUDIZHU, "ai_normal", "斗地主AI普通池", DifficultyRange.NORMAL),
    DOUDIZHU_AI_HARD(GameType.DOUDIZHU, "ai_hard", "斗地主AI困难池", DifficultyRange.HARD),
    DOUDIZHU_AI_EXPERT(GameType.DOUDIZHU, "ai_expert", "斗地主AI专家池", DifficultyRange.EXPERT),
    DOUDIZHU_AI_MASTER(GameType.DOUDIZHU, "ai_master", "斗地主AI大师池", DifficultyRange.MASTER),
    DOUDIZHU_VIP(GameType.DOUDIZHU, "vip", "斗地主VIP池"),
    DOUDIZHU_EVENT(GameType.DOUDIZHU, "event", "斗地主活动池"),

    // ========== 德州扑克牌型池 ==========
    TEXAS_BASE(GameType.TEXAS, "base", "德州基础池"),
    TEXAS_AI_EASY(GameType.TEXAS, "ai_easy", "德州AI简单池", DifficultyRange.EASY),
    TEXAS_AI_NORMAL(GameType.TEXAS, "ai_normal", "德州AI普通池", DifficultyRange.NORMAL),
    TEXAS_AI_HARD(GameType.TEXAS, "ai_hard", "德州AI困难池", DifficultyRange.HARD),
    TEXAS_AI_EXPERT(GameType.TEXAS, "ai_expert", "德州AI专家池", DifficultyRange.EXPERT),
    TEXAS_AI_MASTER(GameType.TEXAS, "ai_master", "德州AI大师池", DifficultyRange.MASTER),
    TEXAS_VIP(GameType.TEXAS, "vip", "德州VIP池"),

    // ========== 牛牛牌型池 ==========
    BULL_BASE(GameType.BULL, "base", "牛牛基础池"),
    BULL_AI_EASY(GameType.BULL, "ai_easy", "牛牛AI简单池", DifficultyRange.EASY),
    BULL_AI_NORMAL(GameType.BULL, "ai_normal", "牛牛AI普通池", DifficultyRange.NORMAL),
    BULL_AI_HARD(GameType.BULL, "ai_hard", "牛牛AI困难池", DifficultyRange.HARD),
    BULL_AI_EXPERT(GameType.BULL, "ai_expert", "牛牛AI专家池", DifficultyRange.EXPERT),
    BULL_AI_MASTER(GameType.BULL, "ai_master", "牛牛AI大师池", DifficultyRange.MASTER),
    BULL_VIP(GameType.BULL, "vip", "牛牛VIP池");

    private final GameType gameType;
    private final String suffix;      // 后缀，用于构建完整ID
    private final String displayName; // 显示名称
    private final DifficultyRange difficultyRange; // 对应的难度范围

    /**
     * 完整ID格式: {gameType}_{suffix}
     * 例如: DOUDIZHU_ai_easy
     */
    public String getPoolId() {
        return gameType.name() + "_" + suffix;
    }

    /**
     * 构造函数（无难度范围）
     */
    HandRankPoolId(GameType gameType, String suffix, String displayName) {
        this(gameType, suffix, displayName, null);
    }

    /**
     * 构造函数（带难度范围）
     */
    HandRankPoolId(GameType gameType, String suffix, String displayName, DifficultyRange difficultyRange) {
        this.gameType = gameType;
        this.suffix = suffix;
        this.displayName = displayName;
        this.difficultyRange = difficultyRange;
    }

    /**
     * 根据难度等级获取对应的AI牌型池ID
     */
    public static HandRankPoolId forAIDifficulty(GameType gameType, int difficulty) {
        for (HandRankPoolId poolId : values()) {
            if (poolId.getGameType() == gameType &&
                    poolId.getDifficultyRange() != null &&
                    poolId.getDifficultyRange().contains(difficulty)) {
                return poolId;
            }
        }
        // 降级到基础池
        return getBasePool(gameType);
    }

    /**
     * 获取游戏的基础牌型池
     */
    public static HandRankPoolId getBasePool(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return DOUDIZHU_BASE;
            case TEXAS: return TEXAS_BASE;
            case BULL: return BULL_BASE;
            default: return DOUDIZHU_BASE;
        }
    }

    /**
     * 获取VIP牌型池
     */
    public static HandRankPoolId getVipPool(GameType gameType) {
        switch (gameType) {
            case DOUDIZHU: return DOUDIZHU_VIP;
            case TEXAS: return TEXAS_VIP;
            case BULL: return BULL_VIP;
            default: return getBasePool(gameType);
        }
    }

    /**
     * 从字符串解析
     */
    public static HandRankPoolId fromString(String poolId) {
        for (HandRankPoolId value : values()) {
            if (value.getPoolId().equals(poolId)) {
                return value;
            }
        }
        throw new IllegalArgumentException("未知的牌型池ID: " + poolId);
    }

    /**
     * 难度范围枚举
     */
    public enum DifficultyRange {
        EASY(1, 2, "简单"),
        NORMAL(3, 4, "普通"),
        HARD(5, 6, "困难"),
        EXPERT(7, 8, "专家"),
        MASTER(9, 10, "大师");

        private final int min;
        private final int max;
        private final String name;

        DifficultyRange(int min, int max, String name) {
            this.min = min;
            this.max = max;
            this.name = name;
        }

        public boolean contains(int difficulty) {
            return difficulty >= min && difficulty <= max;
        }

        public int getMin() { return min; }
        public int getMax() { return max; }
        public String getName() { return name; }
    }
}
