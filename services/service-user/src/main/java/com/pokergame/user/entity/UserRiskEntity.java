package com.pokergame.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户风控实体类
 * 存储用户注册时的静态风控信息，与user表一对一
 *
 * @author poker-platform
 */
@Data
@TableName("user_risk")
public class UserRiskEntity {

    /**
     * 用户ID（与user表主键一致）
     */
    @TableId(value = "user_id")
    private Long userId;

    /**
     * 注册时的IP地址
     */
    @TableField("register_ip")
    private String registerIp;

    /**
     * 注册时的设备ID
     */
    @TableField("register_device_id")
    private String registerDeviceId;

    /**
     * 注册时的UserAgent
     */
    @TableField("register_user_agent")
    private String registerUserAgent;

    /**
     * 注册渠道（例如：app、h5、wechat）
     */
    @TableField("register_channel")
    private String registerChannel;

    /**
     * 注册时的纬度
     */
    @TableField("register_latitude")
    private BigDecimal registerLatitude;

    /**
     * 注册时的经度
     */
    @TableField("register_longitude")
    private BigDecimal registerLongitude;

    /**
     * 风险评分（0-100，数值越高风险越大）
     */
    @TableField("risk_score")
    private Integer riskScore;

    /**
     * 设备指纹（用于设备唯一标识，防止换设备刷单）
     */
    @TableField("device_fingerprint")
    private String deviceFingerprint;

    /**
     * 首次登录时间
     */
    @TableField("first_login_time")
    private LocalDateTime firstLoginTime;

    /**
     * 首次登录IP
     */
    @TableField("first_login_ip")
    private String firstLoginIp;

    /**
     * 首次登录设备ID
     */
    @TableField("first_login_device_id")
    private String firstLoginDeviceId;

    /**
     * 更新时间（自动维护）
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
