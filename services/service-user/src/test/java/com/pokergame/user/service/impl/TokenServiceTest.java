package com.pokergame.user.service.impl;

import com.pokergame.user.UserServerApplication;
import com.pokergame.user.config.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = UserServerApplication.class)
@ActiveProfiles("test")
@DisplayName("TokenService 测试")
class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    private final Long testUserId = 1001L;
    private String testToken;

    @BeforeEach
    void setUp() {
        // 每个测试前清理可能残留的 Token
        if (testToken != null) {
            tokenService.invalidateToken(testToken);
        }
    }

    // ==================== 创建 Token 测试 ====================

    @Test
    @DisplayName("创建 Token - 成功")
    void createToken_Success() {
        // When
        String token = tokenService.createToken(testUserId);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // 验证 Token 可以正常解析
        Long userId = tokenService.getUserIdFromToken(token);
        assertEquals(testUserId, userId);

        // 验证 Token 在 Redis 中存在
        Long verifiedUserId = tokenService.verifyToken(token);
        assertEquals(testUserId, verifiedUserId);
    }

    @Test
    @DisplayName("创建 Token - 参数为 null")
    void createToken_NullUserId() {
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            tokenService.createToken(null);
        });
    }

    // ==================== 验证 Token 测试 ====================

    @Test
    @DisplayName("验证 Token - 有效 Token")
    void verifyToken_Valid() {
        // Given
        String token = tokenService.createToken(testUserId);

        // When
        Long userId = tokenService.verifyToken(token);

        // Then
        assertNotNull(userId);
        assertEquals(testUserId, userId);
    }

    @Test
    @DisplayName("验证 Token - 无效 Token（随机字符串）")
    void verifyToken_InvalidToken() {
        // When
        Long userId = tokenService.verifyToken("invalid_token_string");

        // Then
        assertNull(userId);
    }

    @Test
    @DisplayName("验证 Token - 空 Token")
    void verifyToken_EmptyToken() {
        // When
        Long userId1 = tokenService.verifyToken(null);
        Long userId2 = tokenService.verifyToken("");
        Long userId3 = tokenService.verifyToken("   ");

        // Then
        assertNull(userId1);
        assertNull(userId2);
        assertNull(userId3);
    }

    @Test
    @DisplayName("验证 Token - 已注销的 Token")
    void verifyToken_InvalidatedToken() {
        // Given
        String token = tokenService.createToken(testUserId);
        tokenService.invalidateToken(token);

        // When
        Long userId = tokenService.verifyToken(token);

        // Then
        assertNull(userId);
    }

    @Test
    @DisplayName("验证 Token - 单点登录后旧 Token 失效")
    void verifyToken_SingleLogin_KickOldToken() throws InterruptedException {
        // Given
        String oldToken = tokenService.createToken(testUserId);

        // When - 再次登录，生成新 Token
        String newToken = tokenService.createToken(testUserId);

        // Then - 旧 Token 应该失效
        Long oldUserId = tokenService.verifyToken(oldToken);
        assertNull(oldUserId, "旧 Token 应该被踢下线");

        // 新 Token 应该有效
        Long newUserId = tokenService.verifyToken(newToken);
        assertNotNull(newUserId);
        assertEquals(testUserId, newUserId);
    }

    // ==================== 刷新 Token 测试 ====================

    @Test
    @DisplayName("刷新 Token - 有效 Token 刷新成功")
    void refreshToken_Success() {
        // Given
        String token = tokenService.createToken(testUserId);

        // When
        String refreshedToken = tokenService.refreshToken(token);

        // Then
        assertNotNull(refreshedToken);

        // 验证刷新后的 Token 有效
        Long userId = tokenService.verifyToken(refreshedToken);
        assertEquals(testUserId, userId);
    }

    @Test
    @DisplayName("刷新 Token - 无效 Token 刷新失败")
    void refreshToken_InvalidToken() {
        // When
        String result = tokenService.refreshToken("invalid_token");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("刷新 Token - 空 Token")
    void refreshToken_EmptyToken() {
        // When
        String result1 = tokenService.refreshToken(null);
        String result2 = tokenService.refreshToken("");

        // Then
        assertNull(result1);
        assertNull(result2);
    }

    @Test
    @DisplayName("刷新 Token - 已注销的 Token")
    void refreshToken_InvalidatedToken() {
        // Given
        String token = tokenService.createToken(testUserId);
        tokenService.invalidateToken(token);

        // When
        String result = tokenService.refreshToken(token);

        // Then
        assertNull(result);
    }

    // ==================== 注销 Token 测试 ====================

    @Test
    @DisplayName("注销 Token - 成功")
    void invalidateToken_Success() {
        // Given
        String token = tokenService.createToken(testUserId);

        // When
        tokenService.invalidateToken(token);

        // Then
        Long userId = tokenService.verifyToken(token);
        assertNull(userId);
    }

    @Test
    @DisplayName("注销 Token - 重复注销不报错")
    void invalidateToken_Twice() {
        // Given
        String token = tokenService.createToken(testUserId);

        // When
        tokenService.invalidateToken(token);
        tokenService.invalidateToken(token);

        // Then - 没有抛出异常即为成功
        Long userId = tokenService.verifyToken(token);
        assertNull(userId);
    }

    @Test
    @DisplayName("注销 Token - 空 Token 不报错")
    void invalidateToken_EmptyToken() {
        // When & Then
        assertDoesNotThrow(() -> {
            tokenService.invalidateToken(null);
            tokenService.invalidateToken("");
        });
    }

    // ==================== 踢用户下线测试 ====================

    @Test
    @DisplayName("踢用户下线 - 成功")
    void kickUser_Success() {
        // Given
        String token = tokenService.createToken(testUserId);

        // When
        tokenService.kickUser(testUserId);

        // Then
        Long userId = tokenService.verifyToken(token);
        assertNull(userId);
    }

    @Test
    @DisplayName("踢用户下线 - 用户不存在")
    void kickUser_UserNotExist() {
        // When & Then
        assertDoesNotThrow(() -> {
            tokenService.kickUser(99999L);
        });
    }

    @Test
    @DisplayName("踢用户下线 - 参数为 null")
    void kickUser_NullUserId() {
        // When & Then
        assertDoesNotThrow(() -> {
            tokenService.kickUser(null);
        });
    }

    // ==================== 获取用户ID测试 ====================

    @Test
    @DisplayName("从 Token 获取用户ID - 成功")
    void getUserIdFromToken_Success() {
        // Given
        String token = tokenService.createToken(testUserId);

        // When
        Long userId = tokenService.getUserIdFromToken(token);

        // Then
        assertNotNull(userId);
        assertEquals(testUserId, userId);
    }

    @Test
    @DisplayName("从 Token 获取用户ID - 无效 Token")
    void getUserIdFromToken_InvalidToken() {
        // When
        Long userId = tokenService.getUserIdFromToken("invalid_token");

        // Then
        assertNull(userId);
    }

    @Test
    @DisplayName("从 Token 获取用户ID - 空 Token")
    void getUserIdFromToken_EmptyToken() {
        // When
        Long userId1 = tokenService.getUserIdFromToken(null);
        Long userId2 = tokenService.getUserIdFromToken("");

        // Then
        assertNull(userId1);
        assertNull(userId2);
    }

    // ==================== Token 有效性检查测试 ====================

    @Test
    @DisplayName("检查 Token 有效性 - 有效")
    void isTokenValid_Valid() {
        // Given
        String token = tokenService.createToken(testUserId);

        // When
        boolean valid = tokenService.isTokenValid(token);

        // Then
        assertTrue(valid);
    }

    @Test
    @DisplayName("检查 Token 有效性 - 无效")
    void isTokenValid_Invalid() {
        // When
        boolean valid = tokenService.isTokenValid("invalid_token");

        // Then
        assertFalse(valid);
    }

    @Test
    @DisplayName("检查 Token 有效性 - 空 Token")
    void isTokenValid_Empty() {
        // When
        boolean valid1 = tokenService.isTokenValid(null);
        boolean valid2 = tokenService.isTokenValid("");

        // Then
        assertFalse(valid1);
        assertFalse(valid2);
    }

    @Test
    @DisplayName("检查 Token 有效性 - 已注销的 Token")
    void isTokenValid_Invalidated() {
        // Given
        String token = tokenService.createToken(testUserId);
        tokenService.invalidateToken(token);

        // When
        var userId = tokenService.verifyToken(token);

        // Then
        assertNull(userId);
    }

    // ==================== 多次登录单点登录测试 ====================

    @Test
    @DisplayName("多次登录 - 只有最新 Token 有效")
    void multipleLogin_OnlyLatestTokenValid() {
        // Given - 多次登录
        String token1 = tokenService.createToken(testUserId);
        String token2 = tokenService.createToken(testUserId);
        String token3 = tokenService.createToken(testUserId);

        // When & Then
        assertNull(tokenService.verifyToken(token1), "第1个 Token 应该失效");
        assertNull(tokenService.verifyToken(token2), "第2个 Token 应该失效");
        assertNotNull(tokenService.verifyToken(token3), "第3个 Token 应该有效");
    }

    // ==================== 不同用户 Token 隔离测试 ====================

    @Test
    @DisplayName("不同用户 Token 隔离 - 互不影响")
    void differentUsers_TokensIsolated() {
        // Given
        Long user1 = 1001L;
        Long user2 = 1002L;

        String token1 = tokenService.createToken(user1);
        String token2 = tokenService.createToken(user2);

        // Then
        Long userId1 = tokenService.verifyToken(token1);
        Long userId2 = tokenService.verifyToken(token2);

        assertEquals(user1, userId1);
        assertEquals(user2, userId2);

        // 踢掉 user1，user2 应该不受影响
        tokenService.kickUser(user1);

        assertNull(tokenService.verifyToken(token1));
        assertNotNull(tokenService.verifyToken(token2));
    }
}
