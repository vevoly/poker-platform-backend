package com.pokergame.common.model.auth;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.pokergame.common.model.user.UserCurrencyDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.List;

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

    /** 用户ID */
    private Long userId;

    /** 用户业务编码 */
    private String userCode;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 用户状态 */
    private Integer status;

    /** 登录Token */
    private String token;

    /** Token过期时间（毫秒时间戳） */
    private Long tokenExpireTime;

    /** 最后登录时间（毫秒时间戳） */
    private Long lastLoginTime;

    /** 用户货币列表（可选） */
    private List<UserCurrencyDTO> currencies;
}
