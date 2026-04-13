package com.pokergame.common.model.auth;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@ProtobufClass
@Accessors(chain = true)
public class CreateTokenResp {
    private String token;
    private Long expireTime;
    private Long userId;
}
