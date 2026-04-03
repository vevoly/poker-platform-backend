package com.pokergame.common.deal.dealer.impl;


import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardDeck;
import com.pokergame.common.deal.MultiPlayerDealContext;
import com.pokergame.common.deal.validator.impl.CompositeValidator;
import com.pokergame.common.deal.validator.DealValidator;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 德州扑克发牌器
 *
 * 规则：
 * - 支持2-9人
 * - 每人2张手牌
 * - 另有5张公共牌（由游戏逻辑服单独处理）
 * - 无地主概念
 *
 * @author poker-platform
 */
@Slf4j
public class TexasDealer extends BaseDealer {

    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 9;
    private static final int HAND_SIZE = 2;

    public TexasDealer(int playerCount) {
        super(GameType.TEXAS, playerCount);
    }

    @Override
    protected void validatePlayerCount(int playerCount) {
        if (playerCount < MIN_PLAYERS || playerCount > MAX_PLAYERS) {
            throw new IllegalArgumentException(
                    String.format("德州扑克支持%d-%d人，当前%d人", MIN_PLAYERS, MAX_PLAYERS, playerCount));
        }
    }

    @Override
    public int getHandSize(int playerIndex, boolean isLandlord) {
        // 德州扑克没有地主概念，所有人手牌相同
        return HAND_SIZE;
    }

    @Override
    public int getTotalCardCount() {
        // 只计算手牌，公共牌由游戏逻辑服单独处理
        return playerCount * HAND_SIZE;
    }

    @Override
    protected DealValidator createDefaultValidator(MultiPlayerDealContext context) {
        return CompositeValidator.createTexasValidator(playerCount);
    }

    @Override
    protected int getLandlordIndex(MultiPlayerDealContext context) {
        // 德州没有地主，返回-1表示无地主
        return -1;
    }

    @Override
    protected List<Card> extractLandlordCards(CardDeck deck, int landlordIndex) {
        // 德州没有底牌，返回空列表
        return List.of();
    }

    /**
     * 发公共牌（德州扑克专用）
     * 注意：此方法不在 Dealer 接口中，由游戏逻辑服单独调用
     *
     * @param deck 牌堆
     * @param count 公共牌数量（通常为5）
     * @return 公共牌列表
     */
    public List<Card> dealCommunityCards(CardDeck deck, int count) {
        List<Card> communityCards = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            Card card = deck.draw();
            if (card != null) {
                communityCards.add(card);
            }
        }
        log.debug("公共牌: {}", communityCards);
        return communityCards;
    }
}
