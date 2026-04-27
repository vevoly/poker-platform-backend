package com.pokergame.common.model.broadcast;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.pokergame.common.card.CardDTO;
import lombok.Data;

import java.util.List;

/**
 * 玩家出牌广播数据
 */
@Data
@ProtobufClass
public class PlayCardBroadcastData extends BaseBroadcastData {

    /** 出牌列表 */
    private List<CardDTO> cards;
    /** 牌型 */
    private int pattern;
    /** 剩余牌数 */
    private int remain;
}
