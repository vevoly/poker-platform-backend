package com.pokergame.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.pokergame.common.constants.MetadataKeys;
import com.pokergame.common.exception.GameCode;
import com.pokergame.starter.redis.enums.RedisKey;
import com.pokergame.starter.redis.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * Token 服务
 *
 * 使用 RedisKeyUtil 管理 Redis 存储
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
    private RedisKeyUtil redisKeyUtil;

    @Value("${jwt.secret:your-secret-key-change-it}")
    private String secret;

    @Value("${jwt.expire-hours:24}")
    private long expireHours;

    private JWTVerifier jwtVerifier;

    private JWTVerifier getJwtVerifier() {
        if (jwtVerifier == null) {
            jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).build();
        }
        return jwtVerifier;
    }

    /**
     * 生成并存储 Token
     *
     * @param userId 用户ID
     * @return JWT Token
     */
    public String createToken(Long userId) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");

        // 1. 生成 JWT Token
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expireHours * 3600 * 1000);
        // 添加随机数，确保每次生成的 Token 不同
        String randomId = UUID.randomUUID().toString();

        String token = JWT.create()
                .withClaim("userId", userId)
                .withClaim("jti", randomId)  // 添加唯一标识
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .sign(Algorithm.HMAC256(secret));

        // 2. 获取用户的旧 Token
        String oldToken = redisKeyUtil.get(RedisKey.USER_TOKEN, userId);

        // 3. 删除旧 Token（实现单点登录）
        if (oldToken != null && !oldToken.equals(token)) {
            redisKeyUtil.delete(RedisKey.TOKEN, oldToken);
            log.debug("删除用户 {} 的旧 Token", userId);
        }

        // 4. 存储新 Token
        redisKeyUtil.set(RedisKey.TOKEN, String.valueOf(userId), token);
        redisKeyUtil.set(RedisKey.USER_TOKEN, token, userId);

        log.debug("生成 Token 成功: userId={}", userId);
        return token;
    }

    /**
     * 验证 Token 有效性
     *
     * @param token JWT Token
     * @return 用户ID，无效返回 null
     */
    public Long verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            // 1. 验证 JWT 签名和过期时间
            DecodedJWT decodedJWT = getJwtVerifier().verify(token);
            Long userId = decodedJWT.getClaim(MetadataKeys.USER_ID).asLong();

            if (userId == null) {
                return null;
            }

            // 2. 验证 Token 是否在 Redis 中存在且匹配
            String storedUserId = redisKeyUtil.get(RedisKey.TOKEN, token);

            if (storedUserId == null) {
                log.debug("Token 不存在于 Redis: userId={}", userId);
                return null;
            }

            if (!storedUserId.equals(String.valueOf(userId))) {
                log.warn("Token 与用户ID不匹配");
                return null;
            }

            // 3. 验证这个 Token 是否是用户当前使用的 Token（单点登录校验）
            String currentToken = redisKeyUtil.get(RedisKey.USER_TOKEN, userId);

            if (!token.equals(currentToken)) {
                log.debug("Token 不是用户当前使用的 Token（可能已被新登录踢下线）: userId={}", userId);
                return null;
            }

            log.debug("Token 验证成功: userId={}", userId);
            return userId;

        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            log.debug("Token 已过期: {}", e.getMessage());
            return null;
        } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
            log.warn("Token 验证失败: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Token 验证异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 刷新 Token 过期时间
     * 如果距离过期时间小于 1 小时，则生成新 Token
     *
     * @param token JWT Token
     * @return 新的 Token（如果需要刷新），否则返回原 Token；失败返回 null
     */
    public String refreshToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            // 1. 验证 Token
            DecodedJWT decodedJWT = getJwtVerifier().verify(token);
            Long userId = decodedJWT.getClaim(MetadataKeys.USER_ID).asLong();

            if (userId == null) {
                return null;
            }

            // 2. 验证 Token 是否在 Redis 中存在且匹配
            String storedUserId = redisKeyUtil.get(RedisKey.TOKEN, token);
            if (storedUserId == null || !storedUserId.equals(String.valueOf(userId))) {
                log.warn("Token 不存在于 Redis，无法刷新: userId={}", userId);
                return null;
            }

            // 3. 检查是否需要刷新（距离过期时间小于 1 小时）
            Date expiresAt = decodedJWT.getExpiresAt();
            long remainingTime = expiresAt.getTime() - System.currentTimeMillis();
            boolean needRefresh = remainingTime < 3600 * 1000; // 小于1小时

            if (needRefresh) {
                // 生成新 Token
                log.debug("Token 即将过期，生成新 Token: userId={}, remainingTime={}ms", userId, remainingTime);
                return createToken(userId);
            } else {
                // 刷新 Redis 中的过期时间
                redisKeyUtil.expire(RedisKey.TOKEN, expireHours * 3600, token);
                redisKeyUtil.expire(RedisKey.USER_TOKEN, expireHours * 3600, userId);
                log.debug("Token 刷新成功: userId={}", userId);
                return token;
            }

        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            log.debug("Token 已过期，无法刷新: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Token 刷新失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 注销 Token
     *
     * @param token JWT Token
     */
    public void invalidateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        try {
            DecodedJWT decodedJWT = getJwtVerifier().verify(token);
            Long userId = decodedJWT.getClaim(MetadataKeys.USER_ID).asLong();

            if (userId != null) {
                // 删除 Token
                redisKeyUtil.delete(RedisKey.TOKEN, token);

                // 如果是当前使用的 Token，清空用户记录
                String currentToken = redisKeyUtil.get(RedisKey.USER_TOKEN, userId);
                if (token.equals(currentToken)) {
                    redisKeyUtil.delete(RedisKey.USER_TOKEN, userId);
                }

                log.info("Token 注销成功: userId={}", userId);
            }
        } catch (Exception e) {
            log.warn("Token 注销失败: {}", e.getMessage());
        }
    }

    /**
     * 强制踢用户下线（管理员用）
     *
     * @param userId 用户ID
     */
    public void kickUser(Long userId) {
        if (userId == null) {
            return;
        }

        String token = redisKeyUtil.get(RedisKey.USER_TOKEN, userId);

        if (token != null) {
            // 删除 Token
            redisKeyUtil.delete(RedisKey.TOKEN, token);
            // 删除用户 Token 记录
            redisKeyUtil.delete(RedisKey.USER_TOKEN, userId);
            log.info("强制踢用户下线: userId={}", userId);
        }
    }

    /**
     * 获取 Token 对应的用户ID（不验证 Redis）
     *
     * @param token JWT Token
     * @return 用户ID，无效返回 null
     */
    public Long getUserIdFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            DecodedJWT decodedJWT = getJwtVerifier().verify(token);
            return decodedJWT.getClaim(MetadataKeys.USER_ID).asLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查 Token 是否有效（快速检查，不涉及 Redis）
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean isTokenValid(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            getJwtVerifier().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
