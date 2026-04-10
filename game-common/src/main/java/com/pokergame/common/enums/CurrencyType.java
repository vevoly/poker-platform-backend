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

    /** 金币 */
    GOLD("GOLD", "金币"),

    // ========== 未来扩展（暂不实现） ==========
    // DIAMOND("DIAMOND", "钻石"),
    // ALLIANCE_COIN("ALLIANCE_COIN", "联盟币"),
    // CREDIT("CREDIT", "信用额度"),

    ;

    private final String code;
    private final String name;

    CurrencyType(String code, String name) {
        this.code = code;
        this.name = name;
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