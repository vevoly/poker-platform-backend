package com.pokergame.common.item;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 生效中的道具 - 纯数据结构
 * 数据由 service-item 填充，本模块只定义结构
 *
 * @author poker-platform
 */
@Data
@Builder
public class ActiveItem {

    /** 道具ID */
    private String itemId;

    /** 道具名称 */
    private String name;

    /** 剩余有效局数 */
    private int remainingGames;

    /** 过期时间（毫秒，0表示永久） */
    private long expireTime;

    /** 效果配置（JSON格式，由 service-item 解析后传入） */
    private Map<String, Object> effects;

    /**
     * 检查是否还有效 - 纯计算，无外部依赖
     */
    public boolean isValid() {
        if (expireTime > 0 && System.currentTimeMillis() > expireTime) {
            return false;
        }
        return remainingGames > 0;
    }

    /**
     * 获取加成系数 - 纯计算
     */
    public double getBoostRate() {
        if (effects == null) return 0;
        Object boost = effects.get("boostRate");
        return boost instanceof Number ? ((Number) boost).doubleValue() : 0;
    }

    /**
     * 获取保底目标 - 纯计算
     */
    public String getGuaranteeTarget() {
        if (effects == null) return null;
        Object target = effects.get("guaranteeTarget");
        return target instanceof String ? (String) target : null;
    }

    /**
     * 消耗一局 - 修改自身状态
     */
    public void consume() {
        if (remainingGames > 0) {
            remainingGames--;
        }
    }
}