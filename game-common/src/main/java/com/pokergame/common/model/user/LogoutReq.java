package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户登出请求 DTO
 *
 * @author poker-platform
 */
@Data
@ProtobufClass
@Accessors(chain = true)
public class LogoutReq {

    /** 用户ID */
    private Long userId;

    /** 当前Token */
    @NotBlank(message = "Token不能为空")
    private String token;
}
