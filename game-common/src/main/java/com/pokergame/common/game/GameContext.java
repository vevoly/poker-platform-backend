package com.pokergame.common.game;

import com.pokergame.common.card.Card;
import com.pokergame.common.pattern.PatternResult;
import lombok.Data;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏上下文
 * 包含当前游戏的状态信息
 */
@Data
public class GameContext {
    /** 游戏类型 */
    private GameType gameType;

    /** 房间ID */
    private String roomId;

    /** 当前玩家ID */
    private long currentPlayerId;

    /** 玩家列表 */
    private List<Long> playerIds;

    /** 玩家手牌 */
    private Map<Long, List<Card>> playerCards;

    /** 上一手出的牌 */
    private List<Card> lastPlayedCards;

    /** 上一手出牌的玩家ID */
    private Long lastPlayedPlayerId;

    /** 当前牌型结果 */
    private PatternResult currentPattern;

    /** 局数 */
    private int round;

    /** 额外数据 */
    private Map<String, Object> extra = new ConcurrentHashMap<>();

    public void addExtra(String key, Object value) {
        extra.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtra(String key) {
        return (T) extra.get(key);
    }
}
