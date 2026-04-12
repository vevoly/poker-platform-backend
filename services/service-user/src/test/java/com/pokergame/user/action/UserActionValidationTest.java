package com.pokergame.user.action;

import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.pokergame.common.model.user.*;
import com.pokergame.user.UserServerApplication;
import com.pokergame.user.config.TokenService;
import com.pokergame.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = UserServerApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("用户Action参数校验测试")
class UserActionValidationTest {

    @Autowired
    private UserAction userAction;

    // Mock Service 层，避免真实数据库操作
    @MockBean
    private UserService userService;
    @MockBean
    private TokenService tokenService;

    // ==================== 注册校验测试 ====================

    @Test
    @DisplayName("注册 - 用户名格式错误")
    void register_InvalidUsername() {
        RegisterReq req = new RegisterReq()
                .setUsername("ab")           // 太短
                .setPassword("123456");
        assertThrows(MsgException.class, () -> userAction.register(req));
    }

    @Test
    @DisplayName("注册 - 手机号格式错误（全球格式）")
    void register_InvalidMobile() {
        RegisterReq req = new RegisterReq()
                .setMobile("123")            // 位数不足
                .setPassword("123456");
        assertThrows(MsgException.class, () -> userAction.register(req));
    }

    @Test
    @DisplayName("注册 - 邮箱格式错误")
    void register_InvalidEmail() {
        RegisterReq req = new RegisterReq()
                .setEmail("invalid-email")
                .setPassword("123456");
        assertThrows(MsgException.class, () -> userAction.register(req));
    }

    @Test
    @DisplayName("注册 - 密码为空")
    void register_BlankPassword() {
        RegisterReq req = new RegisterReq()
                .setUsername("validuser")
                .setPassword("");
        assertThrows(MsgException.class, () -> userAction.register(req));
    }

    @Test
    @DisplayName("注册 - 密码长度不足")
    void register_ShortPassword() {
        RegisterReq req = new RegisterReq()
                .setUsername("validuser")
                .setPassword("12345");
        assertThrows(MsgException.class, () -> userAction.register(req));
    }

    @Test
    @DisplayName("注册 - 用户名、手机号、邮箱均为空")
    void register_NoAccountIdentifier() {
        RegisterReq req = new RegisterReq()
                .setPassword("123456");
        assertThrows(MsgException.class, () -> userAction.register(req));
    }

    // ==================== 登录校验测试 ====================

    @Test
    @DisplayName("登录 - 所有账号标识为空")
    void login_NoAccountIdentifier() {
        LoginReq req = new LoginReq()
                .setPassword("123456");
        assertThrows(MsgException.class, () -> userAction.login(req));
    }

    @Test
    @DisplayName("登录 - 密码为空")
    void login_BlankPassword() {
        LoginReq req = new LoginReq()
                .setUsername("testuser")
                .setPassword("");
        assertThrows(MsgException.class, () -> userAction.login(req));
    }

    @Test
    @DisplayName("登录 - 用户名格式错误")
    void login_InvalidUsername() {
        LoginReq req = new LoginReq()
                .setUsername("test@user")
                .setPassword("123456");
        assertThrows(MsgException.class, () -> userAction.login(req));
    }

    // ==================== 登出校验测试 ====================

    @Test
    @DisplayName("登出 - Token为空")
    void logout_BlankToken() {
        LogoutReq req = new LogoutReq()
                .setUserId(1001L)
                .setToken("");
        assertThrows(MsgException.class, () -> userAction.logout(req));
    }

    // ==================== 获取用户信息校验测试 ====================

    @Test
    @DisplayName("获取用户信息 - userId为null（允许，从token获取）")
    void getUserInfo_UserIdNull() {
        GetUserInfoReq req = new GetUserInfoReq()
                .setUserId(null);
        // 不应抛出参数校验异常，因为 userId 非必填
        assertDoesNotThrow(() -> userAction.getUserInfo(req));
    }
}
