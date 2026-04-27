package com.pokergame.common.model.room;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.pokergame.common.card.CardDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 房间状态 DTO
 */
@Data
@ProtobufClass
@Accessors(chain = true)
public class RoomStateDTO {

    /**  房间ID */
    private long roomId;

    /** 房主ID */
    private long ownerId;

    /** 玩家数量 */
    private int maxPlayers;

    /** 房间状态 */
    private String gameStatus;   // WAITING, READY, BIDDING, PLAYING, FINISHED

    /** 玩家列表 */
    private List<PlayerStateDTO> players;

    /** 当前回合玩家ID 当前回合玩家ID（若无则为0）*/
    private long currentTurnUserId;
    /** 当前回合剩余超时秒数 （约数，实际由客户端倒计时）*/
    private int timeoutSeconds;
    /** 叫地主当前轮数（1-3），非叫地主阶段为0 */
    private int biddingRound;
    /** 地主ID （未确定时为0）*/
    private long landlordId;
    /** 桌面上的牌 （上一手出的牌）*/
    private List<CardDTO> lastPlayCards;
    /** 上一手牌型代码 */
    private int lastPlayPattern;
    /** 当前倍率 */
    private int multiplier;
    /** 地主底牌（仅当自己是地主时下发，否则为空）*/
    private List<CardDTO> landlordExtraCards;
}
