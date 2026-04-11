package com.pokergame.starter.redis.util;

import com.pokergame.starter.redis.config.RedisProperties;
import com.pokergame.starter.redis.enums.RedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 *
 * 封装 Redis Key 的生成和常用操作
 *
 * @author poker-platform
 */
@Slf4j
@RequiredArgsConstructor
public class RedisKeyUtil {

    private final RedissonClient redissonClient;
    private final RedisProperties redisProperties;

    // ========== Key 生成 ==========

    /**
     * 生成完整 Redis Key
     */
    public String getFullKey(RedisKey keyEnum, Object... args) {
        return keyEnum.format(redisProperties.getPrefix(), redisProperties.getEnv(), args);
    }

    // ========== Bucket 操作 ==========

    /**
     * 获取 Bucket（带自动过期）
     */
    public <T> RBucket<T> getBucket(RedisKey keyEnum, Object... args) {
        String key = getFullKey(keyEnum, args);
        RBucket<T> bucket = redissonClient.getBucket(key);

        if (keyEnum.getExpireSeconds() > 0) {
            bucket.expire(keyEnum.getExpireSeconds(), TimeUnit.SECONDS);
        }

        return bucket;
    }

    /**
     * 设置值
     */
    public <T> void set(RedisKey keyEnum, T value, Object... args) {
        getBucket(keyEnum, args).set(value);
        log.debug("Redis SET: key={}, value={}", getFullKey(keyEnum, args), value);
    }

    /**
     * 设置值（自定义过期时间，单位：秒）
     */
    public <T> void setWithExpire(RedisKey keyEnum, T value, long expireSeconds, Object... args) {
        String key = getFullKey(keyEnum, args);
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value, expireSeconds, TimeUnit.SECONDS);
        log.debug("Redis SET: key={}, value={}, expire={}s", key, value, expireSeconds);
    }

    /**
     * 设置值（不覆盖现有值，如果 key 已存在则返回 false）
     */
    public <T> boolean setIfAbsent(RedisKey keyEnum, T value, Object... args) {
        String key = getFullKey(keyEnum, args);
        RBucket<T> bucket = redissonClient.getBucket(key);
        boolean success = bucket.trySet(value);
        if (success && keyEnum.getExpireSeconds() > 0) {
            bucket.expire(keyEnum.getExpireSeconds(), TimeUnit.SECONDS);
        }
        log.debug("Redis SETNX: key={}, value={}, success={}", key, value, success);
        return success;
    }

    /**
     * 获取值
     */
    public <T> T get(RedisKey keyEnum, Object... args) {
        String key = getFullKey(keyEnum, args);
        RBucket<T> bucket = redissonClient.getBucket(key);
        T value = bucket.get();
        log.debug("Redis GET: key={}, value={}", key, value);
        return value;
    }

    /**
     * 设置值并返回旧值
     */
    public <T> T getAndSet(RedisKey keyEnum, T value, Object... args) {
        String key = getFullKey(keyEnum, args);
        RBucket<T> bucket = redissonClient.getBucket(key);
        T oldValue = bucket.getAndSet(value);
        if (keyEnum.getExpireSeconds() > 0) {
            bucket.expire(keyEnum.getExpireSeconds(), TimeUnit.SECONDS);
        }
        log.debug("Redis GETANDSET: key={}, oldValue={}, newValue={}", key, oldValue, value);
        return oldValue;
    }

    /**
     * 获取 Bucket（不自动设置过期时间）
     */
    public <T> RBucket<T> getBucketRaw(RedisKey keyEnum, Object... args) {
        String key = getFullKey(keyEnum, args);
        return redissonClient.getBucket(key);
    }

    /**
     * 删除
     */
    public void delete(RedisKey keyEnum, Object... args) {
        String key = getFullKey(keyEnum, args);
        redissonClient.getBucket(key).delete();
        log.debug("Redis DEL: key={}", key);
    }

    /**
     * 删除多个 Key
     */
    public void deleteBatch(RedisKey keyEnum, List<Object> argsList) {
        for (Object args : argsList) {
            delete(keyEnum, args);
        }
    }

    /**
     * 判断是否存在
     */
    public boolean exists(RedisKey keyEnum, Object... args) {
        String key = getFullKey(keyEnum, args);
        boolean exists = redissonClient.getBucket(key).isExists();
        log.debug("Redis EXISTS: key={}, result={}", key, exists);
        return exists;
    }

    // ========== 分布式锁 ==========

    /**
     * 获取分布式锁
     */
    public RLock getLock(RedisKey keyEnum, Object... args) {
        String key = getFullKey(keyEnum, args);
        return redissonClient.getLock(key);
    }

    /**
     * 执行带锁的操作
     */
    public <T> T executeWithLock(RedisKey keyEnum, java.util.concurrent.Callable<T> callable,
                                 long waitTime, long leaseTime, Object... args) throws Exception {
        RLock lock = getLock(keyEnum, args);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("获取锁失败: " + getFullKey(keyEnum, args));
            }
            return callable.call();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ========== 计数器 ==========

    /**
     * 自增
     */
    public long increment(RedisKey keyEnum, Object... args) {
        String key = getFullKey(keyEnum, args);
        long value = redissonClient.getAtomicLong(key).incrementAndGet();
        log.debug("Redis INCR: key={}, value={}", key, value);
        return value;
    }

    /**
     * 自增指定步长
     */
    public long incrementBy(RedisKey keyEnum, long delta, Object... args) {
        String key = getFullKey(keyEnum, args);
        long value = redissonClient.getAtomicLong(key).addAndGet(delta);
        log.debug("Redis INCRBY: key={}, delta={}, value={}", key, delta, value);
        return value;
    }

    /**
     * 设置过期时间
     */
    public void expire(RedisKey keyEnum, long expireSeconds, Object... args) {
        String key = getFullKey(keyEnum, args);
        redissonClient.getBucket(key).expire(expireSeconds, TimeUnit.SECONDS);
        log.debug("Redis EXPIRE: key={}, expire={}s", key, expireSeconds);
    }


}