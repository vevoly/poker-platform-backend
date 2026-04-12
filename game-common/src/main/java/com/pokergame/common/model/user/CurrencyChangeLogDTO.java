package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
public class CurrencyChangeLogDTO {

    /** 主键 */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 货币类型 */
    private String currencyType;

    /** 变动金额 */
    private Long changeAmount;

    /** 变动前金额 */
    private Long beforeAmount;

    /** 变动后金额 */
    private Long afterAmount;

    /** 变动类型 */
    private String changeType;

    /** 订单ID */
    private String orderId;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private Long createTime;  // 毫秒时间戳
}
