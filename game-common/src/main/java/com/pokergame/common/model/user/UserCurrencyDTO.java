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
public class UserCurrencyDTO {

    /** 用户ID */
    private Long userId;

    /** 货币类型（GOLD, DIAMOND, ALLIANCE_COIN） */
    private String currencyType;

    /** 货币数量 */
    private Long amount;
}
