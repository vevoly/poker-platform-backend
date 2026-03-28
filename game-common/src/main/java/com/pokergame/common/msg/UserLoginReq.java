package com.pokergame.common.msg;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * 用户登录请求消息
 * 
 * @author 游戏平台
 * @date 2024-03-26
 */
@ToString
@ProtobufClass
@FieldDefaults(level = AccessLevel.PUBLIC)
public class UserLoginReq {
    /** 用户名 */
    String username;
    
    /** 密码 */
    String password;
}