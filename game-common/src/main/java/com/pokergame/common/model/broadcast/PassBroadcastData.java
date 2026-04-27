package com.pokergame.common.model.broadcast;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;

/**
 * 玩家过牌广播数据
 */
@Data
@ProtobufClass
public class PassBroadcastData extends BaseBroadcastData {
}
