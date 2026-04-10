package com.pokergame.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pokergame.starter.mybatis.handler.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户统计实体类
 *
 * @author poker-platform
 * @since 1.0.0
 */
@Data
@TableName(value = "user_stats", autoResultMap = true)
public class UserStatsEntity {

    /**
     * 用户ID（主键）
     */
    @TableId(value = "user_id")
    private Long userId;

    /**
     * 总局数
     */
    @TableField("total_games")
    private Integer totalGames;

    /**
     * 胜局数
     */
    @TableField("win_games")
    private Integer winGames;

    /**
     * 连胜次数
     */
    @TableField("consecutive_wins")
    private Integer consecutiveWins;

    /**
     * 连败次数
     */
    @TableField("consecutive_losses")
    private Integer consecutiveLosses;

    /**
     * 扩展字段（JSON格式）
     */
    @TableField(value = "extra", typeHandler = JacksonTypeHandler.class)
    private String extra;

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
