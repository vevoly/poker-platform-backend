package com.pokergame.common.model.broadcast;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.pokergame.common.card.CardDTO;
import lombok.Data;

import java.util.List;

/**
 * 发牌广播数据
 */
@Data
@ProtobufClass
public class DealCardsBroadcastData extends BaseBroadcastData {

    /** 手牌 */
    private List<CardDTO> handCards;

}
