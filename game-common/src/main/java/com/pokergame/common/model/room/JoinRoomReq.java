package com.pokergame.common.model.room;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * 加入房间请求
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@FieldDefaults(level = AccessLevel.PUBLIC)
public class JoinRoomReq {

    /** 房间ID */
    private long roomId;

    /** 玩家昵称 */
    private String playerName;

    /** 房间密码（私人房间） */
    private String password;
}
