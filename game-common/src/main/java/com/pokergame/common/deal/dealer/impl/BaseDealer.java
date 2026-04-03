package com.pokergame.common.deal.dealer.impl;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardDeck;
import com.pokergame.common.deal.*;
import com.pokergame.common.deal.dealer.Dealer;
import com.pokergame.common.deal.generator.impl.CompositeHandGenerator;
import com.pokergame.common.deal.generator.HandGenerator;
import com.pokergame.common.item.ActiveItem;
import com.pokergame.common.deal.validator.DealValidator;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 发牌器基类
 *
 * 职责：
 * 1. 封装所有游戏通用的发牌逻辑
 * 2. 整合策略管理器、手牌生成器、发牌验证器
 * 3. 提供扩展点供子类实现游戏特定逻辑
 *
 * 设计模式：模板方法模式
 * - 主流程 deal() 由基类实现
 * - 扩展点由子类实现
 *
 * @author poker-platform
 */
@Slf4j
public abstract class BaseDealer implements Dealer {

    // ==================== 核心组件 ====================

    /** 游戏类型 */
    protected final GameType gameType;

    /** 玩家数量 */
    protected final int playerCount;

    /** 策略管理器 - 负责决策要什么牌 */
    protected final DealStrategyManager strategyManager;

    /** 手牌生成器 - 负责生成手牌 */
    protected final HandGenerator handGenerator;

    /** 牌型评估器 - 用于验证生成的牌型 */
    protected final HandRankEvaluator rankEvaluator;

    // ==================== 构造函数 ====================

    public BaseDealer(GameType gameType, int playerCount) {
        this.gameType = gameType;
        this.playerCount = playerCount;
        this.strategyManager = new DealStrategyManager(gameType);
        this.handGenerator = createHandGenerator();
        this.rankEvaluator = new HandRankEvaluator();

        // 验证玩家数量
        validatePlayerCount(playerCount);

        log.info("发牌器初始化: game={}, players={}", gameType, playerCount);
    }

    // ==================== 发牌主流程（模板方法） ====================

    /**
     * 发牌主流程 - 模板方法
     * 子类不应重写此方法
     */
    @Override
    public final List<List<Card>> deal(MultiPlayerDealContext context) {
        log.debug("开始发牌: game={}, players={}", gameType, playerCount);

        // 1. 初始化牌堆
        CardDeck deck = new CardDeck(getDecksCount());

        // 2. 确定地主/庄家
        int landlordIndex = getLandlordIndex(context);

        // 3. 按优先级排序玩家
        List<Integer> sortedIndices = sortPlayersByPriority(context);

        // 4. 初始化手牌列表
        List<List<Card>> hands = initializeHands();

        // 5. 为每个玩家发牌
        for (int playerIndex : sortedIndices) {
            boolean isLandlord = (playerIndex == landlordIndex);
            int handSize = getHandSize(playerIndex, isLandlord);

            // 获取该玩家的单玩家上下文
            DealContext playerContext = context.getPlayerContext(playerIndex);

            // 获取目标牌型（策略决策）
            HandRank targetRank = strategyManager.getTargetRank(playerContext);

            // 生成手牌
            List<Card> hand = generatePlayerHand(deck, targetRank, handSize, playerIndex);

            hands.set(playerIndex, hand);
            log.debug("玩家{}发牌完成: handSize={}, targetRank={}",
                    playerIndex, hand.size(), targetRank);
        }

        // 6. 处理地主底牌
        List<Card> landlordExtra = extractLandlordCards(deck, landlordIndex);
        if (landlordExtra != null && !landlordExtra.isEmpty()) {
            hands.get(landlordIndex).addAll(landlordExtra);
            log.debug("地主底牌: {}张", landlordExtra.size());
        }

        // 7. 验证发牌结果（动态创建验证器）
        DealValidator validator = createDefaultValidator(context);
        validator.validate(hands);

        log.info("发牌完成: game={}, totalCards={}", gameType, getTotalCardCount());

        return hands;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成单个玩家的手牌
     */
    private List<Card> generatePlayerHand(CardDeck deck, HandRank targetRank,
                                          int handSize, int playerIndex) {
        // 使用手牌生成器生成手牌
        List<Card> hand = handGenerator.generate(deck, targetRank, handSize, 50);

        // 生成失败时的降级处理
        if (hand == null || hand.size() != handSize) {
            log.warn("玩家{}手牌生成异常，使用随机补牌", playerIndex);
            hand = handGenerator.generate(deck, null, handSize, 1);
        }

        return hand;
    }

    /**
     * 初始化手牌列表
     */
    private List<List<Card>> initializeHands() {
        List<List<Card>> hands = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            hands.add(new ArrayList<>());
        }
        return hands;
    }

    /**
     * 按策略优先级排序玩家
     */
    protected List<Integer> sortPlayersByPriority(MultiPlayerDealContext context) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            indices.add(i);
        }

        indices.sort((a, b) -> {
            int pa = getPlayerPriority(context, a);
            int pb = getPlayerPriority(context, b);
            return Integer.compare(pb, pa);
        });

        return indices;
    }

    /**
     * 获取玩家优先级（子类可重写）
     */
    protected int getPlayerPriority(MultiPlayerDealContext context, int playerIndex) {
        int priority = 0;

        // 有保底道具
        if (hasGuaranteeItem(context, playerIndex)) priority += 100;
        // 连败
        if (getLossCount(context, playerIndex) >= 3) priority += 50;
        // VIP
        if (getVipLevel(context, playerIndex) >= 5) priority += 30;
        // 新手
        if (isRookie(context, playerIndex)) priority += 20;

        return priority;
    }

    // ==================== 数据获取方法 ====================

    protected int getVipLevel(MultiPlayerDealContext context, int index) {
        List<Integer> list = context.getVipLevels();
        return list != null && index < list.size() ? list.get(index) : 0;
    }

    protected int getLossCount(MultiPlayerDealContext context, int index) {
        List<Integer> list = context.getConsecutiveLosses();
        return list != null && index < list.size() ? list.get(index) : 0;
    }

    protected int getWinCount(MultiPlayerDealContext context, int index) {
        List<Integer> list = context.getConsecutiveWins();
        return list != null && index < list.size() ? list.get(index) : 0;
    }

    protected boolean isRookie(MultiPlayerDealContext context, int index) {
        List<Boolean> list = context.getRookieFlags();
        return list != null && index < list.size() && list.get(index);
    }

    protected boolean isAI(MultiPlayerDealContext context, int index) {
        List<Boolean> list = context.getAiFlags();
        return list != null && index < list.size() && list.get(index);
    }

    protected boolean hasGuaranteeItem(MultiPlayerDealContext context, int index) {
        List<List<ActiveItem>> list = context.getActiveItemsList();
        if (list == null || index >= list.size()) return false;
        return list.get(index).stream().anyMatch(ActiveItem::isGuarantee);
    }

    // ==================== 可重写方法 ====================

    protected HandGenerator createHandGenerator() {
        return new CompositeHandGenerator(gameType);
    }

    /**
     * 创建默认验证器（需要子类实现）
     * @param context 多玩家上下文，用于动态计算验证参数
     */
    protected abstract DealValidator createDefaultValidator(MultiPlayerDealContext context);

    protected int getDecksCount() {
        return 1;
    }

    // ==================== 抽象方法 ====================

    /** 验证玩家数量 */
    protected abstract void validatePlayerCount(int playerCount);

    /** 获取手牌大小 */
    @Override
    public abstract int getHandSize(int playerIndex, boolean isLandlord);

    /** 获取总牌数 */
    @Override
    public abstract int getTotalCardCount();

    /** 获取地主索引 */
    protected abstract int getLandlordIndex(MultiPlayerDealContext context);

    /** 获取地主底牌 */
    protected abstract List<Card> extractLandlordCards(CardDeck deck, int landlordIndex);

    // ==================== 接口方法 ====================

    @Override
    public GameType getGameType() {
        return gameType;
    }

    @Override
    public int getPlayerCount() {
        return playerCount;
    }
}

