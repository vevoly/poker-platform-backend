package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@ProtobufClass
@Accessors(chain = true)
public class GetCurrencyResp {
    private List<UserCurrencyDTO> currencies;
}
