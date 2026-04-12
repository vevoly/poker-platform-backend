package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;

@Data
@ProtobufClass
public class DecreaseCurrencyResp {

    /** 剩余金额 */
    private Long afterAmount;
}
