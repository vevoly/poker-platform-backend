package com.pokergame.common.msg;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * 用户信息消息
 * 
 * @author 游戏平台
 * @date 2024-03-26
 */
@ToString
@ProtobufClass
@FieldDefaults(level = AccessLevel.PUBLIC)
public class UserInfo {
    /** 用户ID */
    long userId;
    
    /** 用户名 */
    String username;
    
    /** 昵称 */
    String nickname;
    
    /** 金币数量 */
    int gold;
    
    /** 等级 */
    int level;
    
    /** 经验值 */
    int exp;
}