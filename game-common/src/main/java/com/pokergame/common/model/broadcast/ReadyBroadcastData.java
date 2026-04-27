package com.pokergame.common.model.broadcast;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * 玩家准备状态广播数据
 */
@Data
@ProtobufClass
@FieldDefaults(level = AccessLevel.PUBLIC)
public class ReadyBroadcastData extends BaseBroadcastData {
    /** 玩家ID */
    private boolean ready;
    /** 玩家昵称 */
    private String nickname;
}
