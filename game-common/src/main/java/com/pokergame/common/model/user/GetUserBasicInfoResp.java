package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@ProtobufClass
@Accessors(chain = true)
public class GetUserBasicInfoResp {
    private long userId;
    private String nickname;
    private String avatar;
}
