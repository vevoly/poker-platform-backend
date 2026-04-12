package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 用户信息
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
public class UserDTO {

    /** 用户ID */
    private Long userId;

    /** 用户业务编码 */
    private String userCode;

    /** 用户名 */
    private String username;

    /** 手机号（脱敏） */
    private String mobile;

    /** 邮箱（脱敏） */
    private String email;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 状态 */
    private Integer status;

    /** 最后登录时间（时间戳） */
    private Long lastLoginTime;
}
