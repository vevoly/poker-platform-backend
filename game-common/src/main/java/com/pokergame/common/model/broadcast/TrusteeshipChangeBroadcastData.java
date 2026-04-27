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
public class TrusteeshipChangeBroadcastData extends BaseBroadcastData{
    /** 托管状态 */
    private boolean trusteeship;
}
