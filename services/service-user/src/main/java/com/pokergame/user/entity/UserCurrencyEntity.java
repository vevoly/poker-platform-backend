package com.pokergame.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户货币实体类
 *
 * @author poker-platform
 * @since 1.0.0
 */
@Data
@TableName("user_currency")
public class UserCurrencyEntity {

    /**
     * 用户ID（联合主键）
     */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    /**
     * 货币类型：GOLD, DIAMOND, ALLIANCE_COIN（联合主键）
     */
    @TableField("currency_type")
    private String currencyType;

    /**
     * 数量
     */
    @TableField("amount")
    private Long amount;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
