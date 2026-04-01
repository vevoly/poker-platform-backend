package com.pokergame.common.event;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 生效中的活动 - 纯数据结构
 * 数据由 service-activity 填充
 *
 * @author poker-platform
 */
@Data
@Builder
public class ActiveEvent {

    /** 活动ID */
    private String eventId;

    /** 活动名称 */
    private String eventName;

    /** 是否生效 */
    private boolean isActive;

    /** 开始时间 */
    private long startTime;

    /** 结束时间 */
    private long endTime;

    /** 加成系数 */
    private double boostRate;

    /** 目标牌型列表 */
    private List<String> targetRanks;

    /** 扩展属性 */
    private Map<String, Object> extra;

    /**
     * 检查活动是否在有效期内
     */
    public boolean isInPeriod() {
        long now = System.currentTimeMillis();
        return isActive && now >= startTime && now <= endTime;
    }

    /**
     * 检查是否包含指定目标牌型
     */
    public boolean hasTargetRank(String rankName) {
        return targetRanks != null && targetRanks.contains(rankName);
    }
}
