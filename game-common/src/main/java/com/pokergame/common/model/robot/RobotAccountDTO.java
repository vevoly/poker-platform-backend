package com.pokergame.common.model.robot;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@ProtobufClass
@Accessors(chain = true)
public class RobotAccountDTO {

    private Long userId;
    private String nickname;
    private String avatar;
    private Integer difficulty; // 1简单 2中等 3困难
    private Integer enabled;
}
