package com.pokergame.common.model.robot;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
public class GetRobotAccountsReq {
    // 空请求或分页参数，此处简化为获取所有启用的机器人
}
