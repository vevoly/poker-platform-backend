package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 查询货币请求
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
public class GetCurrencyReq {

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 货币类型 */
    private String currencyType; // 可选，不传则查询所有
}
