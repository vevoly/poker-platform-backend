package com.pokergame.common.context;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.iohao.game.core.common.client.Attachment;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 自定义附加信息，存放用户基本信息
 */
@Data
@ProtobufClass
@Accessors(chain = true)
public class MyAttachment implements Attachment {

    /** 用户ID */
    private long userId;
    /** 用户昵称 */
    private String nickname;
    /** 用户头像 */
    private String avatar;

    // 可扩展其他字段
}