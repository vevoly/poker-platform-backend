package com.pokergame.user.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 服务
 *
 * 使用 Redisson 管理 Token 存储
 * - 支持分布式部署
 * - 支持自动过期
 * - 支持单点登录（新 Token 踢掉旧 Token）
 *
 * @author poker-platform
 */
@Slf4j
@Service
public class TokenService {

    @Autowired
    private RedissonClient redissonClient;

    @Value("${jwt.secret:your-secret-key-change-it}")
    private String secret;

    @Value("${jwt.expire-hours:24}")
    private long expireHours;

    private static final String TOKEN_KEY_PREFIX = "token:";
    private static final String USER_TOKEN_KEY_PREFIX = "user:token:";

    /**
     * 生成并存储 Token
     *
     * @param userId 用户ID
     * @return JWT Token
     */
    public String createToken(long userId) {
        // 1. 生成 JWT Token
        String token = JWT.create()
                .withClaim("userId", userId)
                .withIssuedAt(new java.util.Date())
                .withExpiresAt(new java.util.Date(System.currentTimeMillis() + expireHours * 3600 * 1000))
                .sign(Algorithm.HMAC256(secret));

        // 2. 单点登录：删除该用户的旧 Token
        String userTokenKey = USER_TOKEN_KEY_PREFIX + userId;
        RBucket<String> userTokenBucket = redissonClient.getBucket(userTokenKey);
        String oldToken = userTokenBucket.get();
        if (oldToken != null) {
            // 删除旧 Token
            redissonClient.getBucket(TOKEN_KEY_PREFIX + oldToken).delete();
            log.debug("删除用户 {} 的旧 Token", userId);
        }

        // 3. 存储新 Token
        RBucket<String> tokenBucket = redissonClient.getBucket(TOKEN_KEY_PREFIX + token);
        tokenBucket.set(String.valueOf(userId), expireHours, TimeUnit.HOURS);

        // 4. 记录用户当前使用的 Token
        userTokenBucket.set(token, expireHours, TimeUnit.HOURS);

        log.debug("生成 Token 成功: userId={}, expireHours={}", userId, expireHours);
        return token;
    }

    /**
     * 验证 Token 有效性
     *
     * @param token JWT Token
     * @return 用户ID，无效返回 0
     */
    public long verifyToken(String token) {
        if (token == null || token.isEmpty()) {
            return 0;
        }

        try {
            // 1. 验证 JWT 签名和过期时间
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);

            long userId = decodedJWT.getClaim("userId").asLong();

            // 2. 验证 Token 是否在 Redis 中存在
            RBucket<String> tokenBucket = redissonClient.getBucket(TOKEN_KEY_PREFIX + token);
            String storedUserId = tokenBucket.get();

            if (storedUserId == null || !storedUserId.equals(String.valueOf(userId))) {
                log.warn("Token 不存在于 Redis: userId={}", userId);
                return 0;
            }

            log.debug("Token 验证成功: userId={}", userId);
            return userId;

        } catch (Exception e) {
            log.warn("Token 验证失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 刷新 Token 过期时间
     *
     * @param token JWT Token
     * @return 是否刷新成功
     */
    public boolean refreshToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            // 1. 验证 Token
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);

            long userId = decodedJWT.getClaim("userId").asLong();

            // 2. 刷新 Redis 中的过期时间
            RBucket<String> tokenBucket = redissonClient.getBucket(TOKEN_KEY_PREFIX + token);
            String storedUserId = tokenBucket.get();

            if (storedUserId != null && storedUserId.equals(String.valueOf(userId))) {
                tokenBucket.expire(expireHours, TimeUnit.HOURS);

                // 同时刷新用户 Token 记录
                String userTokenKey = USER_TOKEN_KEY_PREFIX + userId;
                redissonClient.getBucket(userTokenKey).expire(expireHours, TimeUnit.HOURS);

                log.debug("Token 刷新成功: userId={}", userId);
                return true;
            }

            return false;

        } catch (Exception e) {
            log.warn("Token 刷新失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 注销 Token
     *
     * @param token JWT Token
     */
    public void invalidateToken(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        try {
            // 1. 获取用户ID
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
            long userId = decodedJWT.getClaim("userId").asLong();

            // 2. 删除 Token
            redissonClient.getBucket(TOKEN_KEY_PREFIX + token).delete();

            // 3. 删除用户 Token 记录
            String userTokenKey = USER_TOKEN_KEY_PREFIX + userId;
            redissonClient.getBucket(userTokenKey).delete();

            log.info("Token 注销成功: userId={}", userId);

        } catch (Exception e) {
            log.warn("Token 注销失败: {}", e.getMessage());
        }
    }

    /**
     * 强制踢用户下线（管理员用）
     *
     * @param userId 用户ID
     */
    public void kickUser(long userId) {
        String userTokenKey = USER_TOKEN_KEY_PREFIX + userId;
        RBucket<String> userTokenBucket = redissonClient.getBucket(userTokenKey);
        String token = userTokenBucket.get();

        if (token != null) {
            // 删除 Token
            redissonClient.getBucket(TOKEN_KEY_PREFIX + token).delete();
            // 删除用户 Token 记录
            userTokenBucket.delete();
            log.info("强制踢用户下线: userId={}", userId);
        }
    }

    /**
     * 获取 Token 对应的用户ID（不验证）
     *
     * @param token JWT Token
     * @return 用户ID，无效返回 0
     */
    public long getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return 0;
        }

        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
            return decodedJWT.getClaim("userId").asLong();
        } catch (Exception e) {
            return 0;
        }
    }
}
