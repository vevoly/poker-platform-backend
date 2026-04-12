package com.pokergame.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pokergame.starter.mybatis.handler.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户登录日志实体类
 * 记录每次登录的详细轨迹，用于安全审计、风控和活动统计
 *
 * @author poker-platform
 * @since 1.0.0
 */
@Data
@TableName(value = "user_login_log", autoResultMap = true)
public class UserLoginLogEntity {

    /**
     * 主键ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID（登录失败时可能为null）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 登录时间
     */
    @TableField("login_time")
    private LocalDateTime loginTime;

    /**
     * 登录IP
     */
    @TableField("login_ip")
    private String loginIp;

    /**
     * 登录设备ID
     */
    @TableField("login_device_id")
    private String loginDeviceId;

    /**
     * 登录时的UserAgent
     */
    @TableField("login_user_agent")
    private String loginUserAgent;

    /**
     * 登录时的纬度
     */
    @TableField("login_latitude")
    private BigDecimal loginLatitude;

    /**
     * 登录时的经度
     */
    @TableField("login_longitude")
    private BigDecimal loginLongitude;

    /**
     * 登录结果：1-成功，0-失败
     */
    @TableField("login_result")
    private Integer loginResult;

    /**
     * 失败原因（如：密码错误、账号禁用等）
     */
    @TableField("fail_reason")
    private String failReason;

    /**
     * 登录成功后生成的Token
     */
    @TableField("token")
    private String token;

    /**
     * 扩展字段（JSON格式）
     */
    @TableField(value = "extra", typeHandler = JacksonTypeHandler.class)
    private String extra;
}
