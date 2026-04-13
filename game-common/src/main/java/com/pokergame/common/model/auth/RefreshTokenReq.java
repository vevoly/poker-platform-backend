package com.pokergame.common.model.auth;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@ProtobufClass
@Accessors(chain = true)
public class RefreshTokenReq {
    @NotBlank(message = "Token不能为空")
    private String token;
}
