package com.pokergame.common.model.auth;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@ProtobufClass
@Accessors(chain = true)
public class KickUserReq {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
