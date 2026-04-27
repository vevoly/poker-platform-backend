package com.pokergame.common.model.broadcast;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.pokergame.common.enums.TurnPhase;
import lombok.Data;

/**
 * 轮到玩家出牌广播数据
 */
@Data
@ProtobufClass
public class TurnBroadcastData extends BaseBroadcastData {
    /** 倒计时秒数 */
    private int timeoutSeconds;
    /** 当前阶段 BIDDING / PLAYING */
    private TurnPhase phase;
    /** 叫地主阶段：当前轮数(1-3)；出牌阶段：0 */
    private int biddingRound;

}
