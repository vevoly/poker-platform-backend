package com.pokergame.common.model.ws;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.iohao.game.core.common.client.Attachment;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@ProtobufClass
@FieldDefaults(level = AccessLevel.PUBLIC)
public class WsAttachment implements Attachment {

    private long userId;

    private String token;
}
