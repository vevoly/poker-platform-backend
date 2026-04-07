package com.pokergame.common.model.game.doudizhu;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@ToString
@ProtobufClass
@FieldDefaults(level = AccessLevel.PUBLIC)
public class NotGrabLandlordReq {
    /** 房间ID */
    private long roomId;
}
