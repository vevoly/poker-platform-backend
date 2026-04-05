package com.pokergame.common.model.room;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.pokergame.common.card.Card;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * 出牌请求
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@FieldDefaults(level = AccessLevel.PUBLIC)
public class PlayCardReq {

    /** 房间ID */
    private long roomId;

    /** 出的牌 */
    private List<Card> cards;
}
