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
 * 牛牛发牌器
 *
 * 规则：
 * - 支持2-6人
 * - 每人5张牌
 * - 无地主概念
 *
 * @author poker-platform
 */
@Slf4j
public class BullDealer extends BaseDealer {

    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 6;
    private static final int HAND_SIZE = 5;

    public BullDealer(int playerCount) {
        super(GameType.BULL, playerCount);
    }

    @Override
    protected void validatePlayerCount(int playerCount) {
        if (playerCount < MIN_PLAYERS || playerCount > MAX_PLAYERS) {
            throw new IllegalArgumentException(
                    String.format("牛牛支持%d-%d人，当前%d人", MIN_PLAYERS, MAX_PLAYERS, playerCount));
        }
    }

    @Override
    public int getHandSize(int playerIndex, boolean isLandlord) {
        // 牛牛没有地主概念，所有人手牌相同
        return HAND_SIZE;
    }

    @Override
    public int getTotalCardCount() {
        return playerCount * HAND_SIZE;
    }

    @Override
    protected DealValidator createDefaultValidator(MultiPlayerDealContext context) {
        return CompositeValidator.createBullValidator(playerCount);
    }

    @Override
    protected int getLandlordIndex(MultiPlayerDealContext context) {
        // 牛牛没有地主，返回-1表示无地主
        return -1;
    }

    @Override
    protected List<Card> extractLandlordCards(CardDeck deck, int landlordIndex) {
        // 牛牛没有底牌，返回空列表
        return List.of();
    }
}
