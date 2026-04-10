package com.pokergame.starter.redis.util;

import com.pokergame.starter.redis.config.RedisProperties;
import com.pokergame.starter.redis.enums.RedisKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisKeyUtil 单元测试")
class RedisKeyUtilTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RBucket<String> bucket;

    private RedisKeyUtil redisKeyUtil;

    @BeforeEach
    void setUp() {
        RedisProperties properties = new RedisProperties();
        properties.setPrefix("poker");
        properties.setEnv("test");

        redisKeyUtil = new RedisKeyUtil(redissonClient, properties);
    }

    @Test
    @DisplayName("测试 Key 生成")
    void testKeyGeneration() {
        // 生成 Token Key
        String tokenKey = redisKeyUtil.getFullKey(RedisKey.TOKEN, "abc123");
        assertThat(tokenKey).isEqualTo("poker:test:token:abc123");

        // 生成用户 Token Key
        String userTokenKey = redisKeyUtil.getFullKey(RedisKey.USER_TOKEN, 1001L);
        assertThat(userTokenKey).isEqualTo("poker:test:user:token:1001");

        // 生成房间 Key
        String roomKey = redisKeyUtil.getFullKey(RedisKey.ROOM_INFO, 12345L);
        assertThat(roomKey).isEqualTo("poker:test:room:info:12345");
    }

    @Test
    @DisplayName("测试枚举过期时间")
    void testExpireSeconds() {
        assertThat(RedisKey.TOKEN.getExpireSeconds()).isEqualTo(24 * 3600);
        assertThat(RedisKey.USER_ONLINE.getExpireSeconds()).isEqualTo(60);
        assertThat(RedisKey.LOCK_GOLD.getExpireSeconds()).isEqualTo(10);
    }
}
