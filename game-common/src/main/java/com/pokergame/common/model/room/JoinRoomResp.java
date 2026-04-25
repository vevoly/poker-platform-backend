package com.pokergame.common.model.room;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.pokergame.common.model.player.PlayerInfoDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@ProtobufClass
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class JoinRoomResp {

    /** 房间ID */
    private long roomId;

    /** 房主ID */
    private long ownerId;

    /** 最大玩家数 */
    private int maxPlayers;

    /** 当前玩家数 */
    private int playerCount;

    /** 玩家列表 */
    private List<PlayerInfoDTO> players;

    /** 游戏状态 */
    private String gameStatus;
}
