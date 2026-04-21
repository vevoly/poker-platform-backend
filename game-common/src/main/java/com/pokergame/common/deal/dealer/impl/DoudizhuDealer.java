package com.pokergame.common.deal.dealer.impl;

import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardDeck;
import com.pokergame.common.deal.MultiPlayerDealContext;
import com.pokergame.common.deal.validator.impl.CompositeValidator;
import com.pokergame.common.deal.validator.DealValidator;
import com.pokergame.common.game.GameType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 斗地主发牌器
 *
 * 规则：
 * - 支持2-3人
 * - 农民17张牌，地主20张牌（17张基础 + 3张底牌）
 *
 * @author poker-platform
 */
@Slf4j
public class DoudizhuDealer extends BaseDealer {

    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 3;
    private static final int BASE_HAND_SIZE = 17;      // 基础手牌数（不含底牌）
    private static final int LANDLORD_EXTRA_CARDS = 3;  // 地主额外底牌数

    /** 斗地主地牌 */
    private List<Card> landlordCards;

    public DoudizhuDealer(int playerCount) {
        super(GameType.DOUDIZHU, playerCount);
    }

    @Override
    protected void validatePlayerCount(int playerCount) {
        if (playerCount < MIN_PLAYERS || playerCount > MAX_PLAYERS) {
            throw new IllegalArgumentException(
                    String.format("斗地主支持%d-%d人，当前%d人", MIN_PLAYERS, MAX_PLAYERS, playerCount));
        }
    }

    /**
     * 获取基础手牌大小（不含底牌）
     * 地主和农民都是17张基础手牌
     */
    @Override
    public int getHandSize(int playerIndex, boolean isLandlord) {
        return BASE_HAND_SIZE;
    }

    @Override
    public int getTotalCardCount() {
        if (playerCount == 2) {
            return BASE_HAND_SIZE * 2 + LANDLORD_EXTRA_CARDS;
        } else {
            return BASE_HAND_SIZE * 3 + LANDLORD_EXTRA_CARDS;
        }
    }

    @Override
    protected DealValidator createDefaultValidator(MultiPlayerDealContext context) {
        int landlordIndex = context.getLandlordIndex();
        return CompositeValidator.createDoudizhuValidator(playerCount, landlordIndex);
    }

    @Override
    protected int getLandlordIndex(MultiPlayerDealContext context) {
        return context.getLandlordIndex();
    }

    @Override
    protected List<Card> extractLandlordCards(CardDeck deck, int landlordIndex) {
        landlordCards = new ArrayList<>();
        for (int i = 0; i < LANDLORD_EXTRA_CARDS; i++) {
            Card card = deck.draw();
            if (card != null) {
                landlordCards.add(card);
            }
        }
        log.debug("地主{}底牌: {}", landlordIndex, landlordCards);
        return landlordCards;
    }

    /**
     * 获取地主底牌
     */
    public List<Card> getLandlordCards() {
        return landlordCards != null ? landlordCards : new ArrayList<>();
    }
}
