package com.pokergame.starter.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 配置属性
 *
 * @author poker-platform
 */
@Data
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {

    /** 项目前缀，默认 poker */
    private String prefix = "poker";

    /** 环境标识，默认 dev */
    private String env = "dev";

    /** Token 过期时间（小时），默认 24 */
    private long tokenExpireHours = 24;

    /** 是否启用单点登录 */
    private boolean singleLogin = true;

    /** Redisson 配置（可选，不配置则使用默认） */
    private RedissonConfig redisson = new RedissonConfig();

    @Data
    public static class RedissonConfig {
        /** Redis 地址 */
        private String address = "redis://localhost:6379";

        /** Redis 密码 */
        private String password = null;

        /** 连接池大小 */
        private int connectionPoolSize = 64;

        /** 最小空闲连接数 */
        private int connectionMinimumIdleSize = 10;
    }
}
