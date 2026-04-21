package com.pokergame.common.model.player;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@ProtobufClass
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class PlayerInfo {

    /** 玩家ID */
    private long userId;

    /** 玩家昵称 */
    private String nickname;

    /** 是否地主 */
    private boolean isLandlord;

    /** 手牌数量 */
    private int cardCount;

    /** 是否准备 */
   private boolean ready;
}
