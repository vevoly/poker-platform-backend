package com.pokergame.starter.redis.config;

import com.pokergame.starter.redis.util.RedisKeyUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 自动配置类
 *
 * @author poker-platform
 */
@Configuration
@ConditionalOnClass(RedissonClient.class)
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        RedisProperties.RedissonConfig config = redisProperties.getRedisson();

        Config redissonConfig = new Config();
        redissonConfig.useSingleServer()
                .setAddress(config.getAddress())
                .setPassword(config.getPassword())
                .setConnectionPoolSize(config.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(config.getConnectionMinimumIdleSize());

        return Redisson.create(redissonConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisKeyUtil redisKeyUtil(RedissonClient redissonClient, RedisProperties redisProperties) {
        return new RedisKeyUtil(redissonClient, redisProperties);
    }
}
