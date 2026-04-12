package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@ProtobufClass
@Accessors(chain = true)
public class VerifyTokenResp {

    /** 用户ID */
    private Long userId;

    /** 是否有效 */
    private Boolean valid;
}
