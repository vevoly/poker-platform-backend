package com.pokergame.common.deal.dealer;

import com.pokergame.common.card.Card;
import com.pokergame.common.deal.MultiPlayerDealContext;
import com.pokergame.common.game.GameType;

import java.util.List;

/**
 * 发牌器接口 - 所有游戏共用
 *
 * @author poker-platform
 */
public interface Dealer {

    /**
     * 发牌
     * @param context 多玩家发牌上下文
     * @return 每个玩家的手牌列表
     */
    List<List<Card>> deal(MultiPlayerDealContext context);

    /**
     * 获取游戏类型
     */
    GameType getGameType();

    /**
     * 获取玩家数量
     */
    int getPlayerCount();

    /**
     * 获取手牌大小
     */
    int getHandSize(int playerIndex, boolean isLandlord);

    /**
     * 获取总牌数
     */
    int getTotalCardCount();
}
