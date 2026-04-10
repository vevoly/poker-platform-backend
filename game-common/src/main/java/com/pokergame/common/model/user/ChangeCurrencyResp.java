package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 修改货币响应
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
public class ChangeCurrencyResp {

    /** 用户货币信息 */
    private UserCurrencyDTO currency;

    /** 是否成功 */
    private Boolean success;

    /** 错误码（失败时返回） */
    private Integer errorCode;

    /** 错误信息（失败时返回） */
    private String errorMsg;
}
