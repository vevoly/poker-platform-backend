package com.pokergame.common.model.auth;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * 用户登录请求 DTO
 * 支持用户名、手机号、邮箱三种登录方式，任选其一
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class LoginReq {

    // ========== 账号信息（三选一） ==========
    /** 用户名 */
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "用户名格式不正确")
    private String username;

    /** 手机号 */
    @Pattern(regexp = "^(\\+?\\d{5,15})$", message = "手机号格式不正确（允许加号，5-15位数字）")
    private String mobile;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    // ========== 风控字段 ==========
    /** 登录IP */
    private String loginIp;

    /** 登录设备ID */
    private String loginDeviceId;

    /** 登录UserAgent */
    private String loginUserAgent;

    /** 登录纬度 */
    private BigDecimal loginLatitude;

    /** 登录经度 */
    private BigDecimal loginLongitude;
}
