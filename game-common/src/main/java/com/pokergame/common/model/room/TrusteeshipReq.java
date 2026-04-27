package com.pokergame.common.model.room;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * 房间托管请求
 */
@Data
@ProtobufClass
@FieldDefaults(level = AccessLevel.PUBLIC)
public class TrusteeshipReq {
    private long roomId;       // 房间ID（可选，服务端可根据 userId 获取）
    private boolean trusteeship; // true: 开启托管, false: 取消托管
}
