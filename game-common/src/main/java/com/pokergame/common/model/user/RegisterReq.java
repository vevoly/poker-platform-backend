package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * 用户注册请求
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class RegisterReq {

    // ========== 账号信息（三选一） ==========
    /** 用户名（可选，4-20位字母数字） */
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "用户名必须为4-20位字母或数字")
    private String username;

    /** 手机号（可选，11位数字） */
    @Pattern(regexp = "^(\\+?\\d{5,15})$", message = "手机号格式不正确（允许加号，5-15位数字）")
    private String mobile;

    /** 邮箱（可选，符合邮箱格式） */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 密码（明文，服务端BCrypt加密） */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须为6-20位")
    private String password;

    /** 昵称（可选，默认为用户名/手机号/邮箱前缀） */
    @Size(min = 1, max = 20, message = "昵称长度必须为1-20位")
    private String nickname;

    // ========== 风控字段（客户端上报） ==========
    /** 注册IP（客户端上报，服务端也可获取） */
    private String registerIp;

    /** 注册设备ID */
    private String registerDeviceId;

    /** 注册UserAgent */
    private String registerUserAgent;

    /** 注册渠道（app、h5、wechat等） */
    private String registerChannel;

    /** 注册纬度（用户授权时获取） */
    private BigDecimal registerLatitude;

    /** 注册经度 */
    private BigDecimal registerLongitude;
}
