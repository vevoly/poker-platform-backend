package com.pokergame.common.model.auth;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@ProtobufClass
@Accessors(chain = true)
public class VerifyUserCredentialResp {
    private Boolean valid;
    private Long userId;
    private String userCode;
    private String username;
    private String nickname;
    private String avatar;
    private Integer status;
}
