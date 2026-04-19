package com.pokergame.robot.manager;

import com.pokergame.common.game.GameType;
import com.pokergame.common.card.CardDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 房间状态
 */
@Data
@Accessors(chain = true)
public class RoomState {
    /** 房间ID */
    private String roomId;
    /** 游戏类型 */
    private GameType gameType;
    /** 玩家ID列表, 座位次序 */
    private List<Long> playerIds;
    /** 当前玩家ID */
    private Long currentPlayerId;
    /** 回合超时时间（秒），0表示无超时 */
    private Integer timeoutSeconds;
    /** 当前牌局状态 */
    private Map<Long, List<CardDTO>> handCards;      // 机器人自己的手牌
    /** 已出牌列表 */
    private Map<Long, List<CardDTO>> playedCards;    // 所有玩家已出的牌
    /** 玩家积分 */
    private Map<Long, Integer> scores;
    /** 最后一次操作的玩家ID */
    private Long lastActionPlayerId;
    /** 最后一次出牌的牌型 */
    private List<CardDTO> lastPlayedCards;
    /** 最后一次出牌的牌型 */
    private String lastPlayPattern;
    /** 当前叫地主倍数 */
    private Integer currentBidMultiple;           // 叫地主倍数
    /** 当前地主ID */
    private long lastEventTime;
}
