package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;

@Data
@ProtobufClass
public class IncreaseCurrencyResp {

    /** 帐变后金额 */
    private Long afterAmount;
}
