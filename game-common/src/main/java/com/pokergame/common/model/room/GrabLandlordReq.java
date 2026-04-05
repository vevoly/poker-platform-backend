package com.pokergame.common.model.room;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * 抢地主请求
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@FieldDefaults(level = AccessLevel.PUBLIC)
public class GrabLandlordReq {

    /** 房间ID */
    private long roomId;

    /** 抢地主倍数（1/2/3） */
    private int multiple;
}
