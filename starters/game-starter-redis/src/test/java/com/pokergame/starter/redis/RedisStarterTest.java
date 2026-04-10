package com.pokergame.starter.redis;

import com.pokergame.starter.redis.annotation.EnableRedis;
import com.pokergame.starter.redis.enums.RedisKey;
import com.pokergame.starter.redis.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import redis.embedded.RedisServer;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(classes = RedisStarterTest.TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Redis Starter 集成测试")
class RedisStarterTest {

    @Autowired
    private RedisKeyUtil redisKeyUtil;

    private static RedisServer redisServer;

    @Configuration
    @EnableRedis
    static class TestConfig {
        // 空配置，让 Spring Boot 扫描
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("redis.prefix", () -> "poker");
        registry.add("redis.env", () -> "test");
        registry.add("redis.redisson.address", () -> "redis://localhost:6379");
    }

    @BeforeEach
    void setUp() {
        log.info("========== 开始测试 ==========");
    }

    @AfterEach
    void tearDown() {
        log.info("========== 测试结束 ==========");
    }

    // ==================== 基础操作测试 ====================

    @Test
    @DisplayName("测试 SET/GET 操作")
    void testSetAndGet() {
        String testValue = "test-user-1001";

        // SET
        redisKeyUtil.set(RedisKey.USER_TOKEN, testValue, 1001L);

        // GET
        String value = redisKeyUtil.get(RedisKey.USER_TOKEN, 1001L);

        assertThat(value).isEqualTo(testValue);
        log.info("SET/GET 测试通过: key={}, value={}",
                redisKeyUtil.getFullKey(RedisKey.USER_TOKEN, 1001L), value);
    }

    @Test
    @DisplayName("测试 EXISTS 操作")
    void testExists() {
        // 先删除确保不存在
        redisKeyUtil.delete(RedisKey.TOKEN, "test-token");

        // 验证不存在
        boolean notExists = redisKeyUtil.exists(RedisKey.TOKEN, "test-token");
        assertThat(notExists).isFalse();

        // 设置值
        redisKeyUtil.set(RedisKey.TOKEN, "1001", "test-token");

        // 验证存在
        boolean exists = redisKeyUtil.exists(RedisKey.TOKEN, "test-token");
        assertThat(exists).isTrue();

        log.info("EXISTS 测试通过");
    }

    @Test
    @DisplayName("测试 DELETE 操作")
    void testDelete() {
        // 设置值
        redisKeyUtil.set(RedisKey.USER_ONLINE, "online", 1001L);
        assertThat(redisKeyUtil.exists(RedisKey.USER_ONLINE, 1001L)).isTrue();

        // 删除
        redisKeyUtil.delete(RedisKey.USER_ONLINE, 1001L);

        // 验证已删除
        assertThat(redisKeyUtil.exists(RedisKey.USER_ONLINE, 1001L)).isFalse();

        log.info("DELETE 测试通过");
    }

    @Test
    @DisplayName("测试自定义过期时间")
    void testCustomExpire() throws Exception {
        // 设置 2 秒过期
        redisKeyUtil.set(RedisKey.TOKEN, "test-value", 2, "expire-test");

        // 立即检查存在
        assertThat(redisKeyUtil.exists(RedisKey.TOKEN, "expire-test")).isTrue();

        // 等待 3 秒
        Thread.sleep(3000);

        // 验证已过期
        assertThat(redisKeyUtil.exists(RedisKey.TOKEN, "expire-test")).isFalse();

        log.info("自定义过期时间测试通过");
    }

    // ==================== 分布式锁测试 ====================

    @Test
    @DisplayName("测试分布式锁")
    void testDistributedLock() throws Exception {
        RLock lock = redisKeyUtil.getLock(RedisKey.LOCK_GOLD, 1001L);

        boolean locked = lock.tryLock(1, 5, TimeUnit.SECONDS);
        assertThat(locked).isTrue();

        try {
            // 执行需要锁保护的操作
            log.info("获取锁成功，执行受保护的操作");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("释放锁成功");
            }
        }
    }

    @Test
    @DisplayName("测试 executeWithLock 方法")
    void testExecuteWithLock() throws Exception {
        String result = redisKeyUtil.executeWithLock(
                RedisKey.LOCK_GOLD,
                () -> {
                    // 模拟业务操作
                    Thread.sleep(100);
                    return "success";
                },
                5, 10, 1002L
        );

        assertThat(result).isEqualTo("success");
        log.info("executeWithLock 测试通过: result={}", result);
    }

    // ==================== 计数器测试 ====================

    @Test
    @DisplayName("测试计数器自增")
    void testIncrement() {
        // 先删除
        redisKeyUtil.delete(RedisKey.USER_ONLINE, "counter");

        // 自增
        long v1 = redisKeyUtil.increment(RedisKey.USER_ONLINE, "counter");
        assertThat(v1).isEqualTo(1);

        long v2 = redisKeyUtil.increment(RedisKey.USER_ONLINE, "counter");
        assertThat(v2).isEqualTo(2);

        long v3 = redisKeyUtil.incrementBy(RedisKey.USER_ONLINE, 5, "counter");
        assertThat(v3).isEqualTo(7);

        log.info("计数器测试通过: v1={}, v2={}, v3={}", v1, v2, v3);
    }

    // ==================== 并发测试 ====================

    @Test
    @DisplayName("测试并发场景 - 分布式锁")
    void testConcurrentWithLock() throws InterruptedException {
        int threadCount = 10;
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);

        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            new Thread(() -> {
                try {
                    redisKeyUtil.executeWithLock(
                            RedisKey.LOCK_GOLD,
                            () -> {
                                log.debug("线程 {} 获得锁", idx);
                                Thread.sleep(50);
                                successCount.incrementAndGet();
                                return null;
                            },
                            5, 10, 2000L
                    );
                } catch (Exception e) {
                    log.error("线程 {} 执行失败: {}", idx, e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(30, TimeUnit.SECONDS);

        // 所有线程都应该成功（排队执行）
        assertThat(successCount.get()).isEqualTo(threadCount);
        log.info("并发测试通过: 成功数={}", successCount.get());
    }
}
