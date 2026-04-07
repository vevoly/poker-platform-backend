package com.pokergame.common.model.room;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * 创建房间请求
 *
 * @author poker-platform
 */
@Data
@ToString
@ProtobufClass
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
public class CreateRoomReq {

    /** 最大玩家数（2-3） */
    private int maxPlayers = 3;

    /** 房间名称 */
    private String roomName;

    /** 玩家昵称 */
    private String playerName;

    /** 是否私人房间 */
    private boolean isPrivate = false;

    /** 房间密码（私人房间） */
    private String password;
}
