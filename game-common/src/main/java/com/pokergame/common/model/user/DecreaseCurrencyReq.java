package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@ProtobufClass
@Accessors(chain = true)
public class DecreaseCurrencyReq {

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 货币类型 */
    @NotBlank(message = "货币类型不能为空")
    private String currencyType;

    /** 增加数量 */
    @NotNull(message = "增加数量不能为空")
    @Positive(message = "增加数量必须大于0")
    private Long amount;

    /** 变更类型 */
    @NotBlank(message = "变更类型不能为空")
    private String changeType;  // 对应 ChangeCurrencyType 的 code

    /** 订单ID */
    private String orderId;

    /** 备注 */
    private String remark;
}
