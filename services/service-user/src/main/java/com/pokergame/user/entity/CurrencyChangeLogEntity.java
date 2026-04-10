package com.pokergame.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 货币变更流水实体类
 *
 * @author poker-platform
 * @since 1.0.0
 */
@Data
@TableName("currency_change_log")
public class CurrencyChangeLogEntity {

    /**
     * 流水ID（雪花算法）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 货币类型
     */
    @TableField("currency_type")
    private String currencyType;

    /**
     * 变更数量（正数增加，负数减少）
     */
    @TableField("change_amount")
    private Long changeAmount;

    /**
     * 变更前数量
     */
    @TableField("before_amount")
    private Long beforeAmount;

    /**
     * 变更后数量
     */
    @TableField("after_amount")
    private Long afterAmount;

    /**
     * 变更类型：GAME_WIN, GAME_LOSE, RECHARGE, GIFT
     */
    @TableField("change_type")
    private String changeType;

    /**
     * 关联订单ID
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
