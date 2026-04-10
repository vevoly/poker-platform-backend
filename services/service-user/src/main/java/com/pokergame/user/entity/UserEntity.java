package com.pokergame.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pokergame.starter.mybatis.base.BaseEntity;
import com.pokergame.starter.mybatis.handler.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
     * 用户名（唯一）
     */
    @TableField("username")
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    @TableField("password")
    private String password;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 状态：0禁用，1正常
     */
    @TableField("status")
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 扩展字段（JSON格式）
     * 使用 JacksonTypeHandler 自动转换 Map/JSON
     */
    @TableField(value = "extra", typeHandler = JacksonTypeHandler.class)
    private String extra;
}
