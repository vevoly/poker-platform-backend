package com.pokergame.common.model.room;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.pokergame.common.card.Card;
import com.pokergame.common.card.CardPattern;
import com.pokergame.common.model.CommonRep;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@ProtobufClass
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class PlayCardResp extends CommonRep {

    /** 出的牌 */
    private List<Card> cards;

    /** 牌型 */
    private CardPattern pattern;

    /** 剩余手牌数 */
    private int remainingCards;

    /** 是否出完 */
    private boolean isFinished;
}
