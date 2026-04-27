package com.pokergame.common.model.broadcast;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;

/**
 * 玩家离开房间广播数据
 */
@Data
@ProtobufClass
public class QuitRoomBroadcastData extends BaseBroadcastData {
    /** 玩家数量 */
    private int playerCount;
}
