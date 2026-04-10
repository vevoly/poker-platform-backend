package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * 用户登录响应
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class LoginResp {

    /** 用户基本信息 */
    private UserDTO user;

    /** 登录Token（JWT） */
    private String token;

    /** Token过期时间（时间戳，毫秒） */
    private Long tokenExpireTime;

    /** 最后登录时间（时间戳，毫秒） */
    private Long lastLoginTime;
}
