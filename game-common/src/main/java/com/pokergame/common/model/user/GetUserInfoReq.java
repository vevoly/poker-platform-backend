package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * 获取用户信息请求
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class GetUserInfoReq {

    /** 用户ID（可选，不传则查当前登录用户） */
    private Long userId;

    /** 是否包含货币信息 */
    private Boolean includeCurrency;

    /** 是否包含统计信息 */
    private Boolean includeStats;
}
