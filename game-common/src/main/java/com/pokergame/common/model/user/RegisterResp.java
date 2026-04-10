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

    /** 用户基本信息 */
    private UserDTO user;

    /** 初始货币列表 */
    private java.util.List<UserCurrencyDTO> initialCurrencies;
}
