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
    private long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 用户状态 */
    private int status;

    /** 注册时间 */
    private long registerTime;

    /** 最后登录时间 */
    private long lastLoginTime;
}
