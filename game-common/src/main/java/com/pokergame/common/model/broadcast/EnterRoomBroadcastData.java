package com.pokergame.common.model.broadcast;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;

/**
 * 玩家进入房间广播数据
 */
@Data
@ProtobufClass
public class EnterRoomBroadcastData extends BaseBroadcastData {

    /** 玩家昵称 */
    private String nickname;
    /** 玩家人数 */
    private int playerCount;
    /** 最大玩家数 */
    private int maxPlayers;
}
