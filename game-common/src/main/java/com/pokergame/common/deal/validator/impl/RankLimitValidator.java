package com.pokergame.common.deal.validator.impl;

import com.pokergame.common.card.Card;
import com.pokergame.common.deal.validator.DealValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 牌值限制验证器
 * 验证每种牌值的数量不超过限制（4张，王牌2张）
 *
 * @author poker-platform
 */
public class RankLimitValidator implements DealValidator {

    @Override
    public void validate(List<List<Card>> hands) {
        Map<Integer, Integer> rankCount = new HashMap<>();

        for (List<Card> hand : hands) {
            for (Card card : hand) {
                int rank = card.getRank().getValue();
                rankCount.merge(rank, 1, Integer::sum);

                int maxAllowed = (rank == 16 || rank == 17) ? 2 : 4;
                if (rankCount.get(rank) > maxAllowed) {
                    throw new IllegalStateException(
                            String.format("牌值%d出现%d次，超过限制%d",
                                    rank, rankCount.get(rank), maxAllowed));
                }
            }
        }
    }

    @Override
    public String getName() {
        return "RankLimitValidator";
    }
}
