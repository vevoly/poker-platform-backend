package com.pokergame.common.model.room;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@ProtobufClass
@Accessors(chain = true)
public class PlayerStateDTO {

    /** 玩家ID */
    private long userId;

    /** 玩家昵称 */
    private String nickname;

    /** 是否准备 */
    private boolean ready;

    /** 是否是地主 */
    private boolean landlord;

    /** 是否托管 */
    private boolean trusteeship;

    /** 手牌数量 */
    private int cardCount;

    /** 座位号 */
    private int seat;
}
