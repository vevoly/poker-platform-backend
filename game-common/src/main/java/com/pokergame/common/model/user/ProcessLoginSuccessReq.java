package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 登录成功请求
 */
@Data
@ProtobufClass
@Accessors(chain = true)
public class ProcessLoginSuccessReq {

    /** 用户ID */
    private Long userId;

    /** 登录IP */
    private String loginIp;

    /** 登录设备ID */
    private String loginDeviceId;

    /** 登录渠道 */
    private String loginUserAgent;

    /** 登录经纬度 */
    private BigDecimal loginLatitude;

    /** 登录经纬度 */
    private BigDecimal loginLongitude;
}
