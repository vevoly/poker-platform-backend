package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 获取用户信息响应
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
public class GetUserInfoResp {

    /** 用户基本信息 */
    private UserDTO user;

    /** 货币列表 */
    private List<UserCurrencyDTO> currencies;

    /** 用户统计信息 */
    private UserStatsDTO stats;
}
