package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@ProtobufClass
@Accessors(chain = true)
public class GetCurrencyLogReq {

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 货币类型 */
    private String currencyType;  // 可选，为空则查所有类型

    /** 分页参数 */
    @Min(1)
    private Integer page = 1;

    /** 每页数量 */
    @Min(1)
    @Max(100)
    private Integer size = 10;
}
