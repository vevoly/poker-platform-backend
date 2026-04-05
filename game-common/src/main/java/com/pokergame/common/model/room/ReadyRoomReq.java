package com.pokergame.common.model.room;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Data
@ToString
@ProtobufClass
@FieldDefaults(level = AccessLevel.PUBLIC)
public class ReadyRoomReq {
    private boolean ready;
}
