package com.pokergame.common.model.broadcast;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.pokergame.common.model.player.PlayerInfoDTO;
import lombok.Data;

import java.util.List;

/**
 * 斗地主游戏开始广播数据
 */
@Data
@ProtobufClass
public class GameStartBroadcastData extends BaseBroadcastData {
    /** 房间ID */
    private long roomId;
    /** 玩家信息列表 */
    private List<PlayerInfoDTO> players;
}
