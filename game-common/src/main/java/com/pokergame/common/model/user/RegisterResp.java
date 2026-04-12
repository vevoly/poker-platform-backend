package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * 用户注册响应
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class RegisterResp {

    /** 用户ID */
    private Long userId;

    /** 用户业务编码 */
    private String userCode;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 初始货币信息（可选） */
    private java.util.List<UserCurrencyDTO> initialCurrencies;
}
