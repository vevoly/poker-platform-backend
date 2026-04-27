package com.pokergame.common.model.broadcast;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import lombok.Data;

/**
 * 广播数据基类，所有游戏广播数据都应继承此类。
 * 包含游戏类型字段，便于客户端区分不同游戏的广播。
 */
@Data
@ProtobufClass
public class BaseBroadcastData {
    /** 毫秒时间戳 */
    protected long ts;
    /** 消息序列ID */
    protected int seqId;
    /** 签名 */
    protected String sign;
    /** 游戏类型（例如斗地主、德州等），见 GameType 枚举 */
    protected int gameType;
    /** 操作者用户ID，0 无操作者*/
    protected long userId;
}
