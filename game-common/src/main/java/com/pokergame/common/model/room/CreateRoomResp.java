package com.pokergame.common.model.room;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@ProtobufClass
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class CreateRoomResp {

    /** 房间ID */
    private long roomId;

    /** 房主ID */
    private long ownerId;

    /** 最大玩家数 */
    private int maxPlayers;

    /** 当前玩家数 */
    private int playerCount;

    /** 游戏状态 */
    private String gameStatus;

    /** 房间名称 */
    private String roomName;
}
