package com.pokergame.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pokergame.starter.mybatis.base.BaseEntity;
import com.pokergame.starter.mybatis.handler.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author poker-platform
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user", autoResultMap = true)
public class UserEntity extends BaseEntity {

    /**
     * 用户业务编码（对外公开的唯一标识，避免暴露内部ID）
     */
    @TableField("user_code")
    private String userCode;

    /**
     * 用户名（可选，唯一）
     */
    @TableField("username")
    private String username;

    /**
     * 手机号（唯一，用于登录和绑定）
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 邮箱（唯一，用于登录和绑定）
     */
    @TableField("email")
    private String email;

    /**
     * 登录密码（BCrypt加密存储）
     */
    @TableField("password")
    private String password;

    /**
     * 手机号是否已绑定（0-未绑定，1-已绑定）
     */
    @TableField("bind_mobile")
    private Integer bindMobile;

    /**
     * 邮箱是否已绑定（0-未绑定，1-已绑定）
     */
    @TableField("bind_email")
    private Integer bindEmail;

    /**
     * 用户昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 用户状态：0-禁用，1-正常
     */
    @TableField("status")
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 最后登录设备ID
     */
    @TableField("last_login_device_id")
    private String lastLoginDeviceId;

    /**
     * 最后登录纬度（用于风控和地域统计）
     */
    @TableField("last_login_latitude")
    private BigDecimal lastLoginLatitude;

    /**
     * 最后登录经度
     */
    @TableField("last_login_longitude")
    private BigDecimal lastLoginLongitude;

    /**
     * 扩展字段（JSON格式，存储非核心、易变属性）
     */
    @TableField(value = "extra", typeHandler = JacksonTypeHandler.class)
    private String extra;

    /**
     * 是否机器人（0-否，1-是）
     */
    @TableField("is_robot")
    private Integer isRobot;

    /**
     * 机器人难度（1-简单，2-普通，3-困难）
     */
    @TableField("robot_difficulty")
    private int robotDifficulty;

    /**
     * 是否启用机器人（0-否，1-是）
     */
    @TableField("robot_enabled")
    private Integer robotEnabled;
}
