package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 修改货币请求
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
public class ChangeCurrencyReq {

    /** 用户ID */
    private Long userId;

    /** 货币类型 */
    private String currencyType;

    /** 变更数量（正数增加，负数减少） */
    private Long changeAmount;

    /** 变更类型（GAME_WIN, GAME_LOSE, RECHARGE, GIFT） */
    private String changeType;

    /** 关联订单ID（可选） */
    private String orderId;

    /** 备注（可选） */
    private String remark;
}
