package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@ProtobufClass
@Accessors(chain = true)
public class RefreshTokenResp {

    /** 新的 Token */
    private String newToken;

    /** 过期时间 */
    private Long expireTime;
}
