package com.pokergame.game.doudizhu.trustee;

import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.iohao.game.action.skeleton.core.flow.FlowContextKit;
import com.pokergame.common.card.Card;
import com.pokergame.core.trustee.TrusteeshipDecision;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.enums.InternalOperation;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 斗地主托管决策
 */
@Slf4j
public class DoudizhuTrusteeshipDecision implements TrusteeshipDecision<DoudizhuRoom, DoudizhuPlayer> {

    @Override
    public void act(DoudizhuRoom room, DoudizhuPlayer player) {
        long userId = player.getUserId();
        // 获取游戏状态
        var gameStateManager = room.getStateManager();
        var lastPlayCards = gameStateManager.getLastPlayCards();
        var handCards = player.getHandCards();

        if (handCards == null || handCards.isEmpty()) {
            log.warn("托管玩家 {} 手牌为空", userId);
            return;
        }

        DoudizhuGameStatus status = (DoudizhuGameStatus) gameStateManager.getStatus();

        // 抢地主
        if (status == DoudizhuGameStatus.BIDDING) {
            // 托管玩家在叫地主阶段：永远不抢
            log.info("托管玩家 {} 自动不抢地主", player.getUserId());
            room.operation(InternalOperation.NOT_GRAB, FlowContextKit.ofFlowContext(player.getUserId()));
            return;
        } else if (status == DoudizhuGameStatus.PLAYING) {
            // 1. 首出：出最小单张
            if (lastPlayCards == null || lastPlayCards.isEmpty()) {
                Card minCard = getMinSingleCard(handCards);
                if (minCard != null) {
                    playCards(room, userId, List.of(minCard));
                    log.info("托管玩家 {} 首出最小单张: {}", userId, minCard);
                } else {
                    // 无单张？理论上总有单张，如有特殊牌型可扩展
                    pass(room, userId);
                }
                return;
            }

            // 2. 跟牌：尝试压过上家的最小单张（简化，只处理单张情况）
            // 获取上家单张牌（简化：如果上家出的不是单张，直接过牌）
            if (lastPlayCards.size() == 1) {
                Card lastCard = lastPlayCards.get(0);
                Card biggerCard = findBiggerSingleCard(handCards, lastCard);
                if (biggerCard != null) {
                    playCards(room, userId, List.of(biggerCard));
                    log.info("托管玩家 {} 出单张 {} 压过 {}", userId, biggerCard, lastCard);
                } else {
                    pass(room, userId);
                }
            } else {
                // 上家出的不是单张（对子、顺子等），暂时简化：过牌
                // 后续可按需扩展复杂牌型比较
                pass(room, userId);
            }
        }
    }

    /** 获取手牌中的最小单张 */
    private Card getMinSingleCard(List<Card> hand) {
        // 按排序值升序取最小
        return hand.stream()
                .min((c1, c2) -> Integer.compare(c1.getSortValue(), c2.getSortValue()))
                .orElse(null);
    }

    /** 寻找比目标大的最小单张 */
    private Card findBiggerSingleCard(List<Card> hand, Card target) {
        return hand.stream()
                .filter(c -> c.getSortValue() > target.getSortValue())
                .min((c1, c2) -> Integer.compare(c1.getSortValue(), c2.getSortValue()))
                .orElse(null);
    }

    /** 执行出牌（通过 RPC 或直接调用房间方法） */
    private void playCards(DoudizhuRoom room, long userId, List<Card> cards) {
        // 注意：这里需要构造 FlowContext 并调用 GameAction.playCard
        // 方式一：直接调用房间方法，但需绕过规则检查（因为托管出的牌已经过决策，但仍需验证）
        // 为简化，这里使用 operation 方式（需定义 InternalOperation.PLAY_CARD）
        // 建议：在 DoudizhuRoom 中增加一个 autoPlay(userId, cards) 方法，内部直接调用规则检查和出牌逻辑。
        // 提供简单实现：调用 GameAction 的静态方法或通过 RPC。
        // 临时方案：构造 FlowContext 并通过 RpcInvokeUtil 调用自己的 Action，但可能涉及循环依赖。
        // 最佳：在 DoudizhuRoom 中暴露一个内部方法触发自动出牌。
        room.autoPlay(userId, cards); // 需实现该方法
    }

    /** 过牌 */
    private void pass(DoudizhuRoom room, long userId) {
        FlowContext flowContext = FlowContextKit.ofFlowContext(userId);
        room.operation(InternalOperation.PASS, flowContext);
    }
}
