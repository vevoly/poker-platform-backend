package com.pokergame.core.base;

import com.iohao.game.widget.light.room.SimplePlayer;
import com.pokergame.common.card.Card;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 玩家基类
 *
 * 继承 ioGame 的 SimplePlayer，复用框架提供的玩家基础能力
 *
 * SimplePlayer 提供的基础能力：
 * - userId: 玩家ID
 * - robot: 是否机器人
 * - 房间映射关系
 *
 * 扩展能力：
 * - 手牌管理
 * - 游戏状态
 *
 * @author poker-platform
 */
@Slf4j
@Getter
@Setter
public class BasePlayer extends SimplePlayer {

    /** 玩家昵称 */
    private String nickname;

    /** 玩家手牌 */
    private List<Card> handCards = new ArrayList<>();

    /** 是否已准备 */
    private boolean ready = false;

    /** 是否地主 */
    private boolean landlord = false;

    /** 出牌顺序（1-3） */
    private int order = 0;

    /** 是否已出完牌 */
    private boolean finished = false;

    // ==================== 构造函数 ====================

    /**
     * 无参构造函数（用于框架反序列化）
     */
    public BasePlayer() {
        super();
    }

    /**
     * 创建玩家
     *
     * @param userId 玩家ID
     */
    public BasePlayer(long userId) {
        super();
        this.setUserId(userId);
    }

    /**
     * 创建玩家
     *
     * @param userId 玩家ID
     * @param nickname 玩家昵称
     */
    public BasePlayer(long userId, String nickname) {
        super();
        this.setUserId(userId);
        this.nickname = nickname;
    }

    // ==================== 手牌管理 ====================

    /**
     * 设置手牌
     */
    public void setHandCards(List<Card> cards) {
        this.handCards = new ArrayList<>(cards);
        log.debug("玩家 {} 设置手牌，共 {} 张", getUserId(), handCards.size());
    }

    /**
     * 添加手牌
     */
    public void addCards(List<Card> cards) {
        this.handCards.addAll(cards);
        log.debug("玩家 {} 添加手牌 {} 张，现有 {} 张", getUserId(), cards.size(), handCards.size());
    }

    /**
     * 移除手牌
     *
     * @return true 如果移除成功
     */
    public boolean removeCards(List<Card> cards) {
        boolean removed = handCards.removeAll(cards);
        if (handCards.isEmpty()) {
            this.finished = true;
            log.info("玩家 {} 手牌已出完", getUserId());
        }
        return removed;
    }

    /**
     * 获取手牌数量
     */
    public int getCardCount() {
        return handCards.size();
    }

    /**
     * 检查手牌是否包含指定牌
     */
    public boolean hasCard(Card card) {
        return handCards.contains(card);
    }

    // ==================== 状态管理 ====================

    /**
     * 重置玩家状态（新的一局）
     */
    public void reset() {
        this.handCards.clear();
        this.ready = false;
        this.landlord = false;
        this.order = 0;
        this.finished = false;
        log.debug("玩家 {} 状态已重置", getUserId());
    }

    // ==================== 便捷方法 ====================

    /**
     * 检查是否已准备
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * 检查是否为地主
     */
    public boolean isLandlord() {
        return landlord;
    }

    /**
     * 检查是否已出完牌
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * 检查是否为机器人
     */
    public boolean isRobot() {
        return super.isRobot();
    }

    // ==================== toString ====================

    @Override
    public String toString() {
        return String.format("BasePlayer{userId=%d, nickname=%s, cardCount=%d, ready=%s, landlord=%s, finished=%s}",
                getUserId(), nickname, getCardCount(), ready, landlord, finished);
    }
}
