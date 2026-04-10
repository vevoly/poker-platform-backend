package com.pokergame.common.enums;

import lombok.Getter;

/**
 * 货币类型枚举
 *
 * 设计原则：预留扩展，当前只实现金币
 *
 * @author poker-platform
 */
@Getter
public enum CurrencyType {

    /**
     * 金币（基础货币）
     */
    GOLD("GOLD", "金币"),

    /**
     * 钻石（付费货币）
     */
    DIAMOND("DIAMOND", "钻石"),

    /**
     * 联盟币（公会货币）
     */
    ALLIANCE_COIN("ALLIANCE_COIN", "联盟币");

    private final String code;
    private final String desc;

    CurrencyType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CurrencyType fromCode(String code) {
        for (CurrencyType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return GOLD;
    }

}