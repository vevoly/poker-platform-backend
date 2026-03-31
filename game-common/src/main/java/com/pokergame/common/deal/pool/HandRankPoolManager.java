package com.pokergame.common.deal.pool;

import com.pokergame.common.deal.HandRank;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 牌型池管理器
 *
 * 职责：
 * 1. 管理所有牌型池的创建和缓存
 * 2. 支持从配置加载
 * 3. 支持运行时动态创建
 *
 * @author poker-platform
 */
@Slf4j
public class HandRankPoolManager {

    private static final HandRankPoolManager INSTANCE = new HandRankPoolManager();

    private final Map<String, HandRankPool> pools = new ConcurrentHashMap<>();
    private final Map<GameType, Map<String, HandRankPool>> gamePools = new ConcurrentHashMap<>();

    private HandRankPoolManager() {
        // 初始化默认牌型池
        initDefaultPools();
    }

    public static HandRankPoolManager getInstance() {
        return INSTANCE;
    }

    /**
     * 注册牌型池
     */
    public void registerPool(HandRankPool pool) {
        pools.put(pool.getPoolId(), pool);
        gamePools.computeIfAbsent(pool.getGameType(), k -> new ConcurrentHashMap<>())
                .put(pool.getPoolId(), pool);
        log.info("注册牌型池: {} ({})", pool.getPoolId(), pool.getName());
    }

    /**
     * 获取牌型池
     */
    public HandRankPool getPool(String poolId) {
        return pools.get(poolId);
    }

    /**
     * 获取游戏下的所有牌型池
     */
    public Map<String, HandRankPool> getPoolsByGame(GameType gameType) {
        return gamePools.getOrDefault(gameType, Map.of());
    }

    /**
     * 创建并注册一个牌型池
     */
    public HandRankPool createPool(String poolId, GameType gameType,
                                   Map<HandRank, Double> rankWeights) {
        HandRankPool.Builder builder = HandRankPool.builder(poolId, gameType);
        for (Map.Entry<HandRank, Double> entry : rankWeights.entrySet()) {
            builder.addRank(entry.getKey(), entry.getValue());
        }
        HandRankPool pool = builder.build();
        registerPool(pool);
        return pool;
    }

    /**
     * 初始化默认牌型池
     */
    private void initDefaultPools() {
        // 斗地主默认牌型池
        createDefaultDoudizhuPools();
        // 德州默认牌型池
        createDefaultTexasPools();
        // 牛牛默认牌型池
        createDefaultBullPools();

        log.info("牌型池初始化完成，共注册 {} 个池", pools.size());
    }

    // ==================== 斗地主牌型池 ====================

    private void createDefaultDoudizhuPools() {
        // 1. 基础池
        HandRankPool basePool = HandRankPool.builder(HandRankPoolId.DOUDIZHU_BASE.getPoolId(), GameType.DOUDIZHU)
                .name("斗地主基础池")
                .addRank(HandRank.DOUDIZHU_JUNK, 15)
                .addRank(HandRank.DOUDIZHU_SINGLE, 25)
                .addRank(HandRank.DOUDIZHU_PAIR, 20)
                .addRank(HandRank.DOUDIZHU_TRIPLE, 15)
                .addRank(HandRank.DOUDIZHU_STRAIGHT, 12)
                .addRank(HandRank.DOUDIZHU_BOMB, 8)
                .addRank(HandRank.DOUDIZHU_ROCKET, 5)
                .build();
        registerPool(basePool);

        // 2. AI简单池（难度1-2）
        HandRankPool aiEasyPool = HandRankPool.builder(HandRankPoolId.DOUDIZHU_AI_EASY.getPoolId(), GameType.DOUDIZHU)
                .name("斗地主AI简单池")
                .parent(HandRankPoolId.DOUDIZHU_BASE.getPoolId())
                .addRank(HandRank.DOUDIZHU_JUNK, 40)
                .addRank(HandRank.DOUDIZHU_SINGLE, 30)
                .addRank(HandRank.DOUDIZHU_PAIR, 20)
                .addRank(HandRank.DOUDIZHU_TRIPLE, 8)
                .addRank(HandRank.DOUDIZHU_STRAIGHT, 2)
                .addRank(HandRank.DOUDIZHU_BOMB, 0)
                .addRank(HandRank.DOUDIZHU_ROCKET, 0)
                .build();
        registerPool(aiEasyPool);

        // 3. AI普通池（难度3-4）
        HandRankPool aiNormalPool = HandRankPool.builder(HandRankPoolId.DOUDIZHU_AI_NORMAL.getPoolId(), GameType.DOUDIZHU)
                .name("斗地主AI普通池")
                .parent(HandRankPoolId.DOUDIZHU_BASE.getPoolId())
                .addRank(HandRank.DOUDIZHU_JUNK, 25)
                .addRank(HandRank.DOUDIZHU_SINGLE, 25)
                .addRank(HandRank.DOUDIZHU_PAIR, 20)
                .addRank(HandRank.DOUDIZHU_TRIPLE, 12)
                .addRank(HandRank.DOUDIZHU_STRAIGHT, 10)
                .addRank(HandRank.DOUDIZHU_BOMB, 6)
                .addRank(HandRank.DOUDIZHU_ROCKET, 2)
                .build();
        registerPool(aiNormalPool);

        // 4. AI困难池（难度5-6）
        HandRankPool aiHardPool = HandRankPool.builder(HandRankPoolId.DOUDIZHU_AI_HARD.getPoolId(), GameType.DOUDIZHU)
                .name("斗地主AI困难池")
                .parent(HandRankPoolId.DOUDIZHU_BASE.getPoolId())
                .addRank(HandRank.DOUDIZHU_JUNK, 15)
                .addRank(HandRank.DOUDIZHU_SINGLE, 20)
                .addRank(HandRank.DOUDIZHU_PAIR, 20)
                .addRank(HandRank.DOUDIZHU_TRIPLE, 15)
                .addRank(HandRank.DOUDIZHU_STRAIGHT, 15)
                .addRank(HandRank.DOUDIZHU_BOMB, 12)
                .addRank(HandRank.DOUDIZHU_ROCKET, 3)
                .build();
        registerPool(aiHardPool);

        // 5. AI专家池（难度7-8）
        HandRankPool aiExpertPool = HandRankPool.builder(HandRankPoolId.DOUDIZHU_AI_EXPERT.getPoolId(), GameType.DOUDIZHU)
                .name("斗地主AI专家池")
                .parent(HandRankPoolId.DOUDIZHU_BASE.getPoolId())
                .addRank(HandRank.DOUDIZHU_JUNK, 8)
                .addRank(HandRank.DOUDIZHU_SINGLE, 12)
                .addRank(HandRank.DOUDIZHU_PAIR, 15)
                .addRank(HandRank.DOUDIZHU_TRIPLE, 15)
                .addRank(HandRank.DOUDIZHU_STRAIGHT, 15)
                .addRank(HandRank.DOUDIZHU_BOMB, 20)
                .addRank(HandRank.DOUDIZHU_ROCKET, 15)
                .build();
        registerPool(aiExpertPool);

        // 6. AI大师池（难度9-10）
        HandRankPool aiMasterPool = HandRankPool.builder(HandRankPoolId.DOUDIZHU_AI_MASTER.getPoolId(), GameType.DOUDIZHU)
                .name("斗地主AI大师池")
                .parent(HandRankPoolId.DOUDIZHU_BASE.getPoolId())
                .addRank(HandRank.DOUDIZHU_JUNK, 3)
                .addRank(HandRank.DOUDIZHU_SINGLE, 8)
                .addRank(HandRank.DOUDIZHU_PAIR, 10)
                .addRank(HandRank.DOUDIZHU_TRIPLE, 12)
                .addRank(HandRank.DOUDIZHU_STRAIGHT, 15)
                .addRank(HandRank.DOUDIZHU_BOMB, 25)
                .addRank(HandRank.DOUDIZHU_ROCKET, 27)
                .build();
        registerPool(aiMasterPool);

        // 7. VIP专属池
        HandRankPool vipPool = HandRankPool.builder(HandRankPoolId.DOUDIZHU_VIP.getPoolId(), GameType.DOUDIZHU)
                .name("斗地主VIP池")
                .addRank(HandRank.DOUDIZHU_JUNK, 5)
                .addRank(HandRank.DOUDIZHU_SINGLE, 10)
                .addRank(HandRank.DOUDIZHU_PAIR, 15)
                .addRank(HandRank.DOUDIZHU_TRIPLE, 15)
                .addRank(HandRank.DOUDIZHU_STRAIGHT, 20)
                .addRank(HandRank.DOUDIZHU_BOMB, 20)
                .addRank(HandRank.DOUDIZHU_ROCKET, 15)
                .build();
        registerPool(vipPool);

        log.info("斗地主牌型池初始化完成: base, ai_easy, ai_normal, ai_hard, ai_expert, ai_master, vip");
    }

    // ==================== 德州扑克牌型池 ====================

    private void createDefaultTexasPools() {
        // 1. 基础池
        HandRankPool basePool = HandRankPool.builder(HandRankPoolId.TEXAS_BASE.getPoolId(), GameType.TEXAS)
                .name("德州基础池")
                .addRank(HandRank.TEXAS_HIGH_CARD, 50)
                .addRank(HandRank.TEXAS_ONE_PAIR, 42)
                .addRank(HandRank.TEXAS_TWO_PAIR, 5)
                .addRank(HandRank.TEXAS_THREE_OF_KIND, 2)
                .addRank(HandRank.TEXAS_STRAIGHT, 0.5)
                .addRank(HandRank.TEXAS_FLUSH, 0.3)
                .addRank(HandRank.TEXAS_FULL_HOUSE, 0.1)
                .addRank(HandRank.TEXAS_FOUR_OF_KIND, 0.05)
                .addRank(HandRank.TEXAS_STRAIGHT_FLUSH, 0.01)
                .addRank(HandRank.TEXAS_ROYAL_FLUSH, 0.001)
                .build();
        registerPool(basePool);

        // 2. AI简单池（难度1-2）
        HandRankPool aiEasyPool = HandRankPool.builder(HandRankPoolId.TEXAS_AI_EASY.getPoolId(), GameType.TEXAS)
                .name("德州AI简单池")
                .parent(HandRankPoolId.TEXAS_BASE.getPoolId())
                .addRank(HandRank.TEXAS_HIGH_CARD, 60)
                .addRank(HandRank.TEXAS_ONE_PAIR, 35)
                .addRank(HandRank.TEXAS_TWO_PAIR, 4)
                .addRank(HandRank.TEXAS_THREE_OF_KIND, 1)
                .addRank(HandRank.TEXAS_STRAIGHT, 0)
                .addRank(HandRank.TEXAS_FLUSH, 0)
                .addRank(HandRank.TEXAS_FULL_HOUSE, 0)
                .addRank(HandRank.TEXAS_FOUR_OF_KIND, 0)
                .addRank(HandRank.TEXAS_STRAIGHT_FLUSH, 0)
                .addRank(HandRank.TEXAS_ROYAL_FLUSH, 0)
                .build();
        registerPool(aiEasyPool);

        // 3. AI普通池（难度3-4）
        HandRankPool aiNormalPool = HandRankPool.builder(HandRankPoolId.TEXAS_AI_NORMAL.getPoolId(), GameType.TEXAS)
                .name("德州AI普通池")
                .parent(HandRankPoolId.TEXAS_BASE.getPoolId())
                .addRank(HandRank.TEXAS_HIGH_CARD, 45)
                .addRank(HandRank.TEXAS_ONE_PAIR, 40)
                .addRank(HandRank.TEXAS_TWO_PAIR, 10)
                .addRank(HandRank.TEXAS_THREE_OF_KIND, 4)
                .addRank(HandRank.TEXAS_STRAIGHT, 1)
                .addRank(HandRank.TEXAS_FLUSH, 0)
                .addRank(HandRank.TEXAS_FULL_HOUSE, 0)
                .addRank(HandRank.TEXAS_FOUR_OF_KIND, 0)
                .addRank(HandRank.TEXAS_STRAIGHT_FLUSH, 0)
                .addRank(HandRank.TEXAS_ROYAL_FLUSH, 0)
                .build();
        registerPool(aiNormalPool);

        // 4. AI困难池（难度5-6）
        HandRankPool aiHardPool = HandRankPool.builder(HandRankPoolId.TEXAS_AI_HARD.getPoolId(), GameType.TEXAS)
                .name("德州AI困难池")
                .parent(HandRankPoolId.TEXAS_BASE.getPoolId())
                .addRank(HandRank.TEXAS_HIGH_CARD, 35)
                .addRank(HandRank.TEXAS_ONE_PAIR, 35)
                .addRank(HandRank.TEXAS_TWO_PAIR, 15)
                .addRank(HandRank.TEXAS_THREE_OF_KIND, 8)
                .addRank(HandRank.TEXAS_STRAIGHT, 4)
                .addRank(HandRank.TEXAS_FLUSH, 2)
                .addRank(HandRank.TEXAS_FULL_HOUSE, 1)
                .addRank(HandRank.TEXAS_FOUR_OF_KIND, 0)
                .addRank(HandRank.TEXAS_STRAIGHT_FLUSH, 0)
                .addRank(HandRank.TEXAS_ROYAL_FLUSH, 0)
                .build();
        registerPool(aiHardPool);

        // 5. AI专家池（难度7-8）
        HandRankPool aiExpertPool = HandRankPool.builder(HandRankPoolId.TEXAS_AI_EXPERT.getPoolId(), GameType.TEXAS)
                .name("德州AI专家池")
                .parent(HandRankPoolId.TEXAS_BASE.getPoolId())
                .addRank(HandRank.TEXAS_HIGH_CARD, 25)
                .addRank(HandRank.TEXAS_ONE_PAIR, 30)
                .addRank(HandRank.TEXAS_TWO_PAIR, 18)
                .addRank(HandRank.TEXAS_THREE_OF_KIND, 12)
                .addRank(HandRank.TEXAS_STRAIGHT, 8)
                .addRank(HandRank.TEXAS_FLUSH, 4)
                .addRank(HandRank.TEXAS_FULL_HOUSE, 2)
                .addRank(HandRank.TEXAS_FOUR_OF_KIND, 1)
                .addRank(HandRank.TEXAS_STRAIGHT_FLUSH, 0)
                .addRank(HandRank.TEXAS_ROYAL_FLUSH, 0)
                .build();
        registerPool(aiExpertPool);

        // 6. AI大师池（难度9-10）
        HandRankPool aiMasterPool = HandRankPool.builder(HandRankPoolId.TEXAS_AI_MASTER.getPoolId(), GameType.TEXAS)
                .name("德州AI大师池")
                .parent(HandRankPoolId.TEXAS_BASE.getPoolId())
                .addRank(HandRank.TEXAS_HIGH_CARD, 15)
                .addRank(HandRank.TEXAS_ONE_PAIR, 22)
                .addRank(HandRank.TEXAS_TWO_PAIR, 18)
                .addRank(HandRank.TEXAS_THREE_OF_KIND, 15)
                .addRank(HandRank.TEXAS_STRAIGHT, 12)
                .addRank(HandRank.TEXAS_FLUSH, 8)
                .addRank(HandRank.TEXAS_FULL_HOUSE, 5)
                .addRank(HandRank.TEXAS_FOUR_OF_KIND, 3)
                .addRank(HandRank.TEXAS_STRAIGHT_FLUSH, 1.5)
                .addRank(HandRank.TEXAS_ROYAL_FLUSH, 0.5)
                .build();
        registerPool(aiMasterPool);

        // 7. VIP专属池
        HandRankPool vipPool = HandRankPool.builder(HandRankPoolId.TEXAS_VIP.getPoolId(), GameType.TEXAS)
                .name("德州VIP池")
                .addRank(HandRank.TEXAS_HIGH_CARD, 10)
                .addRank(HandRank.TEXAS_ONE_PAIR, 20)
                .addRank(HandRank.TEXAS_TWO_PAIR, 18)
                .addRank(HandRank.TEXAS_THREE_OF_KIND, 18)
                .addRank(HandRank.TEXAS_STRAIGHT, 15)
                .addRank(HandRank.TEXAS_FLUSH, 10)
                .addRank(HandRank.TEXAS_FULL_HOUSE, 5)
                .addRank(HandRank.TEXAS_FOUR_OF_KIND, 3)
                .addRank(HandRank.TEXAS_STRAIGHT_FLUSH, 1)
                .addRank(HandRank.TEXAS_ROYAL_FLUSH, 0.3)
                .build();
        registerPool(vipPool);

        log.info("德州牌型池初始化完成: base, ai_easy, ai_normal, ai_hard, ai_expert, ai_master, vip");
    }

    // ==================== 牛牛牌型池 ====================

    private void createDefaultBullPools() {
        // 1. 基础池
        HandRankPool basePool = HandRankPool.builder(HandRankPoolId.BULL_BASE.getPoolId(), GameType.BULL)
                .name("牛牛基础池")
                .addRank(HandRank.BULL_NO, 25)
                .addRank(HandRank.BULL_1, 10)
                .addRank(HandRank.BULL_2, 10)
                .addRank(HandRank.BULL_3, 10)
                .addRank(HandRank.BULL_4, 10)
                .addRank(HandRank.BULL_5, 8)
                .addRank(HandRank.BULL_6, 7)
                .addRank(HandRank.BULL_7, 6)
                .addRank(HandRank.BULL_8, 5)
                .addRank(HandRank.BULL_9, 4)
                .addRank(HandRank.BULL_BULL, 3)
                .addRank(HandRank.BULL_FOUR_BOMB, 1)
                .addRank(HandRank.BULL_FIVE_SMALL, 1)
                .build();
        registerPool(basePool);

        // 2. AI简单池（难度1-2）
        HandRankPool aiEasyPool = HandRankPool.builder(HandRankPoolId.BULL_AI_EASY.getPoolId(), GameType.BULL)
                .name("牛牛AI简单池")
                .parent(HandRankPoolId.BULL_BASE.getPoolId())
                .addRank(HandRank.BULL_NO, 40)
                .addRank(HandRank.BULL_1, 15)
                .addRank(HandRank.BULL_2, 12)
                .addRank(HandRank.BULL_3, 10)
                .addRank(HandRank.BULL_4, 8)
                .addRank(HandRank.BULL_5, 6)
                .addRank(HandRank.BULL_6, 4)
                .addRank(HandRank.BULL_7, 3)
                .addRank(HandRank.BULL_8, 2)
                .addRank(HandRank.BULL_9, 0)
                .addRank(HandRank.BULL_BULL, 0)
                .addRank(HandRank.BULL_FOUR_BOMB, 0)
                .addRank(HandRank.BULL_FIVE_SMALL, 0)
                .build();
        registerPool(aiEasyPool);

        // 3. AI普通池（难度3-4）
        HandRankPool aiNormalPool = HandRankPool.builder(HandRankPoolId.BULL_AI_NORMAL.getPoolId(), GameType.BULL)
                .name("牛牛AI普通池")
                .parent(HandRankPoolId.BULL_BASE.getPoolId())
                .addRank(HandRank.BULL_NO, 30)
                .addRank(HandRank.BULL_1, 12)
                .addRank(HandRank.BULL_2, 12)
                .addRank(HandRank.BULL_3, 12)
                .addRank(HandRank.BULL_4, 10)
                .addRank(HandRank.BULL_5, 8)
                .addRank(HandRank.BULL_6, 6)
                .addRank(HandRank.BULL_7, 4)
                .addRank(HandRank.BULL_8, 3)
                .addRank(HandRank.BULL_9, 2)
                .addRank(HandRank.BULL_BULL, 1)
                .addRank(HandRank.BULL_FOUR_BOMB, 0)
                .addRank(HandRank.BULL_FIVE_SMALL, 0)
                .build();
        registerPool(aiNormalPool);

        // 4. AI困难池（难度5-6）
        HandRankPool aiHardPool = HandRankPool.builder(HandRankPoolId.BULL_AI_HARD.getPoolId(), GameType.BULL)
                .name("牛牛AI困难池")
                .parent(HandRankPoolId.BULL_BASE.getPoolId())
                .addRank(HandRank.BULL_NO, 20)
                .addRank(HandRank.BULL_1, 10)
                .addRank(HandRank.BULL_2, 10)
                .addRank(HandRank.BULL_3, 10)
                .addRank(HandRank.BULL_4, 9)
                .addRank(HandRank.BULL_5, 8)
                .addRank(HandRank.BULL_6, 7)
                .addRank(HandRank.BULL_7, 6)
                .addRank(HandRank.BULL_8, 5)
                .addRank(HandRank.BULL_9, 5)
                .addRank(HandRank.BULL_BULL, 5)
                .addRank(HandRank.BULL_FOUR_BOMB, 3)
                .addRank(HandRank.BULL_FIVE_SMALL, 2)
                .build();
        registerPool(aiHardPool);

        // 5. AI专家池（难度7-8）
        HandRankPool aiExpertPool = HandRankPool.builder(HandRankPoolId.BULL_AI_EXPERT.getPoolId(), GameType.BULL)
                .name("牛牛AI专家池")
                .parent(HandRankPoolId.BULL_BASE.getPoolId())
                .addRank(HandRank.BULL_NO, 12)
                .addRank(HandRank.BULL_1, 8)
                .addRank(HandRank.BULL_2, 8)
                .addRank(HandRank.BULL_3, 8)
                .addRank(HandRank.BULL_4, 8)
                .addRank(HandRank.BULL_5, 7)
                .addRank(HandRank.BULL_6, 7)
                .addRank(HandRank.BULL_7, 7)
                .addRank(HandRank.BULL_8, 7)
                .addRank(HandRank.BULL_9, 7)
                .addRank(HandRank.BULL_BULL, 8)
                .addRank(HandRank.BULL_FOUR_BOMB, 6)
                .addRank(HandRank.BULL_FIVE_SMALL, 7)
                .build();
        registerPool(aiExpertPool);

        // 6. AI大师池（难度9-10）
        HandRankPool aiMasterPool = HandRankPool.builder(HandRankPoolId.BULL_AI_MASTER.getPoolId(), GameType.BULL)
                .name("牛牛AI大师池")
                .parent(HandRankPoolId.BULL_BASE.getPoolId())
                .addRank(HandRank.BULL_NO, 5)
                .addRank(HandRank.BULL_1, 5)
                .addRank(HandRank.BULL_2, 5)
                .addRank(HandRank.BULL_3, 5)
                .addRank(HandRank.BULL_4, 5)
                .addRank(HandRank.BULL_5, 5)
                .addRank(HandRank.BULL_6, 6)
                .addRank(HandRank.BULL_7, 7)
                .addRank(HandRank.BULL_8, 8)
                .addRank(HandRank.BULL_9, 9)
                .addRank(HandRank.BULL_BULL, 10)
                .addRank(HandRank.BULL_FOUR_BOMB, 12)
                .addRank(HandRank.BULL_FIVE_SMALL, 18)
                .build();
        registerPool(aiMasterPool);

        // 7. VIP专属池
        HandRankPool vipPool = HandRankPool.builder(HandRankPoolId.BULL_VIP.getPoolId(), GameType.BULL)
                .name("牛牛VIP池")
                .addRank(HandRank.BULL_NO, 8)
                .addRank(HandRank.BULL_1, 6)
                .addRank(HandRank.BULL_2, 6)
                .addRank(HandRank.BULL_3, 6)
                .addRank(HandRank.BULL_4, 6)
                .addRank(HandRank.BULL_5, 6)
                .addRank(HandRank.BULL_6, 6)
                .addRank(HandRank.BULL_7, 7)
                .addRank(HandRank.BULL_8, 7)
                .addRank(HandRank.BULL_9, 8)
                .addRank(HandRank.BULL_BULL, 10)
                .addRank(HandRank.BULL_FOUR_BOMB, 12)
                .addRank(HandRank.BULL_FIVE_SMALL, 12)
                .build();
        registerPool(vipPool);

        log.info("牛牛牌型池初始化完成: base, ai_easy, ai_normal, ai_hard, ai_expert, ai_master, vip");
    }
}
