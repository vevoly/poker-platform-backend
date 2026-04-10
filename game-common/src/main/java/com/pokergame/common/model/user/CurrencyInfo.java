package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 货币信息
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
public class CurrencyInfo {

    /** 货币类型 */
    private String currencyType;

    /** 货币名称 */
    private String currencyName;

    /** 数量 */
    private long amount;
}
