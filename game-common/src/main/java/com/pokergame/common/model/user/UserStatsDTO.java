package com.pokergame.common.model.user;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 用户统计 DTO（用于 RPC 传输）
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
public class UserStatsDTO {

    /** 用户ID */
    private Long userId;

    /** 总局数 */
    private Integer totalGames;

    /** 胜局数 */
    private Integer winGames;

    /** 胜率（百分比，如 65.5） */
    private Double winRate;

    /** 连胜次数 */
    private Integer consecutiveWins;

    /** 连败次数 */
    private Integer consecutiveLosses;
}
