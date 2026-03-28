package com.pokergame.common.msg;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * 用户登录响应消息
 * 
 * @author 游戏平台
 * @date 2024-03-26
 */
@ToString
@ProtobufClass
@FieldDefaults(level = AccessLevel.PUBLIC)
public class UserLoginRes {
    /** 是否成功 */
    boolean success;
    
    /** 错误消息 */
    String errorMessage;
    
    /** 用户信息 */
    UserInfo userInfo;
}