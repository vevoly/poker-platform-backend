package com.pokergame.user.action;

import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.pokergame.common.exception.GameCode;
import com.pokergame.common.model.user.*;
import com.pokergame.user.config.TokenService;
import com.pokergame.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("TokenAction 单元测试")
class TokenActionTest {

    @Autowired
    private TokenAction tokenAction;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserService userService;

    // ==================== refresh ====================

    @Test
    @DisplayName("refresh - Token 刷新成功")
    void refresh_Success() {
        String oldToken = "valid_token";
        String newToken = "new_token";
        when(tokenService.refreshToken(oldToken)).thenReturn(newToken);

        RefreshTokenReq req = new RefreshTokenReq().setToken(oldToken);
        RefreshTokenResp resp = tokenAction.refresh(req);

        assertNotNull(resp);
        assertEquals(newToken, resp.getNewToken());
        assertNotNull(resp.getExpireTime());
        verify(tokenService).refreshToken(oldToken);
    }

    @Test
    @DisplayName("refresh - Token 刷新失败（无效或过期）")
    void refresh_Failure() {
        String invalidToken = "invalid_token";
        when(tokenService.refreshToken(invalidToken)).thenReturn(null);

        RefreshTokenReq req = new RefreshTokenReq().setToken(invalidToken);

        MsgException exception = assertThrows(MsgException.class, () -> tokenAction.refresh(req));
        assertEquals(GameCode.REFRESH_TOKEN_FAILED.getCode(), exception.getMsgCode());
        verify(tokenService).refreshToken(invalidToken);
    }

    @Test
    @DisplayName("refresh - Token 为空（参数校验失败）")
    void refresh_BlankToken() {
        RefreshTokenReq req = new RefreshTokenReq().setToken("");
        assertThrows(MsgException.class, () -> tokenAction.refresh(req));
        verify(tokenService, never()).refreshToken(anyString());
    }

    // ==================== verify ====================

    @Test
    @DisplayName("verify - Token 有效")
    void verify_Valid() {
        String token = "valid_token";
        Long userId = 1001L;
        when(tokenService.verifyToken(token)).thenReturn(userId);

        VerifyTokenReq req = new VerifyTokenReq().setToken(token);
        VerifyTokenResp resp = tokenAction.verify(req);

        assertNotNull(resp);
        assertTrue(resp.getValid());
        assertEquals(userId, resp.getUserId());
        verify(tokenService).verifyToken(token);
    }

    @Test
    @DisplayName("verify - Token 无效")
    void verify_Invalid() {
        String token = "invalid_token";
        when(tokenService.verifyToken(token)).thenReturn(null);

        VerifyTokenReq req = new VerifyTokenReq().setToken(token);
        VerifyTokenResp resp = tokenAction.verify(req);

        assertNotNull(resp);
        assertFalse(resp.getValid());
        assertNull(resp.getUserId());
        verify(tokenService).verifyToken(token);
    }

    @Test
    @DisplayName("verify - Token 为空（参数校验失败）")
    void verify_BlankToken() {
        VerifyTokenReq req = new VerifyTokenReq().setToken("");
        assertThrows(MsgException.class, () -> tokenAction.verify(req));
        verify(tokenService, never()).verifyToken(anyString());
    }

    // ==================== kick ====================

    @Test
    @DisplayName("kick - 踢用户成功")
    void kick_Success() {
        Long userId = 1001L;
        when(userService.checkAndGetUser(userId)).thenReturn(null); // 不关心返回值，只验证调用

        KickUserReq req = new KickUserReq().setUserId(userId);
        KickUserResp resp = tokenAction.kick(req);

        assertNotNull(resp);
        verify(userService).checkAndGetUser(userId);
        verify(tokenService).kickUser(userId);
    }

    @Test
    @DisplayName("kick - 用户不存在（业务异常）")
    void kick_UserNotFound() {
        Long userId = 9999L;
        doThrow(new MsgException(GameCode.USER_NOT_FOUND.getCode(), GameCode.USER_NOT_FOUND.getMsg()))
                .when(userService).checkAndGetUser(userId);

        KickUserReq req = new KickUserReq().setUserId(userId);
        MsgException exception = assertThrows(MsgException.class, () -> tokenAction.kick(req));
        assertEquals(GameCode.USER_NOT_FOUND.getCode(), exception.getMsgCode());
        verify(userService).checkAndGetUser(userId);
        verify(tokenService, never()).kickUser(anyLong());
    }

    @Test
    @DisplayName("kick - 用户ID为空（参数校验失败）")
    void kick_NullUserId() {
        KickUserReq req = new KickUserReq().setUserId(null);
        assertThrows(MsgException.class, () -> tokenAction.kick(req));
        verify(userService, never()).checkAndGetUser(anyLong());
        verify(tokenService, never()).kickUser(anyLong());
    }
}
