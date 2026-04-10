package com.pokergame.starter.redis.enums;

import lombok.Getter;

/**
 * Redis Key 枚举
 *
 * Key 格式：{prefix}:{env}:{业务模块}:{标识}:{ID}
 *
 * @author poker-platform
 */
@Getter
public enum RedisKey {

    // ========== Token 相关 ==========
    /** Token 存储：{prefix}:{env}:token:{token} */
    TOKEN("token:%s", 24 * 3600),

    /** 用户当前 Token：{prefix}:{env}:user:token:{userId} */
    USER_TOKEN("user:token:%d", 24 * 3600),

    // ========== 用户状态 ==========
    /** 用户在线状态：{prefix}:{env}:user:online:{userId} */
    USER_ONLINE("user:online:%d", 60),

    /** 用户所在房间：{prefix}:{env}:user:room:{userId} */
    USER_ROOM("user:room:%d", 3600),

    /** 用户连胜记录：{prefix}:{env}:user:winStreak:{userId} */
    USER_WIN_STREAK("user:winStreak:%d", 3600),

    /** 用户连败记录：{prefix}:{env}:user:loseStreak:{userId} */
    USER_LOSE_STREAK("user:loseStreak:%d", 3600),

    // ========== 房间相关 ==========
    /** 房间信息：{prefix}:{env}:room:info:{roomId} */
    ROOM_INFO("room:info:%d", 3600),

    /** 房间玩家列表：{prefix}:{env}:room:players:{roomId} */
    ROOM_PLAYERS("room:players:%d", 3600),

    /** 房间在线状态：{prefix}:{env}:room:online:{roomId} */
    ROOM_ONLINE("room:online:%d", 60),

    // ========== 匹配相关 ==========
    /** 匹配队列：{prefix}:{env}:match:queue:{gameType} */
    MATCH_QUEUE("match:queue:%s", 300),

    /** 玩家匹配状态：{prefix}:{env}:match:player:{userId} */
    MATCH_PLAYER("match:player:%d", 300),

    // ========== 排行榜 ==========
    /** 排行榜：{prefix}:{env}:rank:{gameType}:{date} */
    RANK("rank:%s:%s", 7 * 24 * 3600),

    // ========== 分布式锁 ==========
    /** 金币操作锁：{prefix}:{env}:lock:gold:{userId} */
    LOCK_GOLD("lock:gold:%d", 10),

    /** 房间操作锁：{prefix}:{env}:lock:room:{roomId} */
    LOCK_ROOM("lock:room:%d", 5),

    /** 联盟操作锁：{prefix}:{env}:lock:alliance:{allianceId} */
    LOCK_ALLIANCE("lock:alliance:%d", 5);

    /** Key 模板 */
    private final String template;

    /** 过期时间（秒），0 表示不过期 */
    private final int expireSeconds;

    RedisKey(String template, int expireSeconds) {
        this.template = template;
        this.expireSeconds = expireSeconds;
    }

    /**
     * 格式化生成完整 Key
     *
     * @param prefix 项目前缀
     * @param env 环境标识
     * @param args 参数
     * @return 完整 Key
     */
    public String format(String prefix, String env, Object... args) {
        String key = String.format(template, args);
        return String.format("%s:%s:%s", prefix, env, key);
    }
}
