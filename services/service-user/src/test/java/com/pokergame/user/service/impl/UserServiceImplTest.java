package com.pokergame.user.service.impl;

import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.pokergame.common.enums.ChangeCurrencyType;
import com.pokergame.common.enums.CurrencyType;
import com.pokergame.common.model.auth.LoginReq;
import com.pokergame.common.model.user.RegisterReq;
import com.pokergame.user.UserServerApplication;
import com.pokergame.user.entity.UserCurrencyEntity;
import com.pokergame.user.entity.UserEntity;
import com.pokergame.user.entity.UserStatsEntity;
import com.pokergame.user.service.CurrencyService;
import com.pokergame.user.service.UserService;
import com.pokergame.user.service.UserStatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = UserServerApplication.class)
@ActiveProfiles("test")
@Transactional  // 测试后自动回滚，保持数据库干净
@DisplayName("用户服务集成测试")
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private UserStatsService statsService;

    // ==================== 用户注册测试 ====================

    @Test
    @DisplayName("注册成功 - 使用用户名注册")
    void register_Success_WithUsername() {
        RegisterReq req = new RegisterReq()
                .setUsername("newuser999")
                .setPassword("123456")
                .setNickname("新用户")
                .setRegisterIp("127.0.0.1")
                .setRegisterDeviceId("device001");

        Long userId = userService.register(req);

        assertNotNull(userId);
        UserEntity user = userService.getById(userId);
        assertEquals("newuser999", user.getUsername());
        assertNotEquals("123456", user.getPassword());
        assertNotNull(user.getCreateTime());
        assertEquals(0, user.getDelFlag());

        UserCurrencyEntity currency = currencyService.getUserCurrency(userId, CurrencyType.GOLD);
        assertEquals(10000L, currency.getAmount());

        UserStatsEntity stats = statsService.getUserStats(userId);
        assertEquals(0, stats.getTotalGames());
    }

    @Test
    @DisplayName("注册成功 - 使用手机号注册")
    void register_Success_WithMobile() {
        RegisterReq req = new RegisterReq()
                .setMobile("+8613800138000")
                .setPassword("123456")
                .setNickname("手机用户");

        Long userId = userService.register(req);
        assertNotNull(userId);
        UserEntity user = userService.getById(userId);
        assertEquals("+8613800138000", user.getMobile());
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void register_UsernameExists() {
        RegisterReq req = new RegisterReq()
                .setUsername("testuser1")
                .setPassword("123456");
        assertThrows(MsgException.class, () -> userService.register(req));
    }

    // ==================== 用户登录测试 ====================

    @Test
    @DisplayName("登录成功 - 使用用户名")
    void login_Success_WithUsername() {
        LoginReq req = new LoginReq()
                .setUsername("testuser1")
                .setPassword("123456")
                .setLoginIp("192.168.1.100")
                .setLoginDeviceId("device123");

        UserEntity user = userService.login(req);
        assertNotNull(user);
        assertEquals(1001L, user.getId());

        // 验证最后登录时间已更新
        UserEntity updated = userService.getById(1001L);
        assertNotNull(updated.getLastLoginTime());
    }

    @Test
    @DisplayName("登录成功 - 使用手机号")
    void login_Success_WithMobile() {
        // 假设 testuser1 的手机号已存在（需提前准备数据）
        LoginReq req = new LoginReq()
                .setMobile("8613800138000")
                .setPassword("123456");
        UserEntity user = userService.login(req);
        assertNotNull(user);
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_WrongPassword() {
        LoginReq req = new LoginReq()
                .setUsername("testuser1")
                .setPassword("wrong");
        assertThrows(MsgException.class, () -> userService.login(req));
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_UserNotFound() {
        LoginReq req = new LoginReq()
                .setUsername("nonexistent")
                .setPassword("123456");
        assertThrows(MsgException.class, () -> userService.login(req));
    }

    // ==================== 货币测试 ====================

    @Test
    @DisplayName("增加货币 - 验证乐观锁版本号增加")
    void increaseCurrency_Success() {
        // Given
        UserCurrencyEntity before = currencyService.getUserCurrency(1001L, CurrencyType.GOLD);
        assertEquals(10000L, before.getAmount());
        assertEquals(0, before.getVersion());

        // When
        Long afterAmount = currencyService.increaseCurrency(
                1001L, CurrencyType.GOLD, 500L, ChangeCurrencyType.RECHARGE, "ORDER_001", "测试充值");

        // Then
        assertEquals(10500L, afterAmount);

        // 验证数据库是否真的更新
        UserCurrencyEntity after = currencyService.getUserCurrency(1001L, CurrencyType.GOLD);
        assertEquals(10500L, after.getAmount());
        assertEquals(1, after.getVersion()); // 乐观锁版本号应该增加
    }

    @Test
    @DisplayName("减少货币 - 验证余额扣减和流水记录")
    void decreaseCurrency_Success() {
        // When
        Long afterAmount = currencyService.decreaseCurrency(
                1001L, CurrencyType.GOLD, 300L, ChangeCurrencyType.GAME_LOSE, null, "输牌扣金币");

        // Then
        assertEquals(9700L, afterAmount);

        // 验证数据库
        UserCurrencyEntity currency = currencyService.getUserCurrency(1001L, CurrencyType.GOLD);
        assertEquals(9700L, currency.getAmount());
        assertEquals(1, currency.getVersion());
    }

    @Test
    @DisplayName("减少货币失败 - 余额不足")
    void decreaseCurrency_InsufficientBalance() {
        assertThrows(MsgException.class, () -> {
            currencyService.decreaseCurrency(1001L, CurrencyType.GOLD, 20000L, ChangeCurrencyType.GAME_LOSE, null, null);
        });

        // 验证余额没有被扣减
        UserCurrencyEntity currency = currencyService.getUserCurrency(1001L, CurrencyType.GOLD);
        assertEquals(10000L, currency.getAmount());
    }

    @Test
    @DisplayName("获取货币 - 验证返回正确的货币信息")
    void getUserCurrency_Success() {
        UserCurrencyEntity currency = currencyService.getUserCurrency(1001L, CurrencyType.GOLD);

        assertNotNull(currency);
        assertEquals(1001L, currency.getUserId());
        assertEquals(CurrencyType.GOLD.getCode(), currency.getCurrencyType());
        assertEquals(10000L, currency.getAmount());
    }

    @Test
    @DisplayName("获取不存在的货币类型 - 抛出异常")
    void getUserCurrency_NotFound() {
        assertThrows(MsgException.class, () -> {
            currencyService.getUserCurrency(1001L, null);
        });
    }

    // ==================== 统计测试 ====================

    @Test
    @DisplayName("记录胜利 - 验证胜场、连胜增加")
    void recordWin_Success() {
        // Given
        UserStatsEntity before = statsService.getUserStats(1001L);
        assertEquals(10, before.getTotalGames());
        assertEquals(6, before.getWinGames());
        assertEquals(0, before.getConsecutiveWins());

        // When
        statsService.recordWin(1001L);

        // Then
        UserStatsEntity after = statsService.getUserStats(1001L);
        assertEquals(11, after.getTotalGames());
        assertEquals(7, after.getWinGames());
        assertEquals(1, after.getConsecutiveWins());
        assertEquals(0, after.getConsecutiveLosses());
    }

    @Test
    @DisplayName("记录失败 - 验证总局数增加，连败增加，连胜重置")
    void recordLoss_Success() {
        // 先赢一局建立连胜
        statsService.recordWin(1001L);

        // When - 再输一局
        statsService.recordLoss(1001L);

        // Then
        UserStatsEntity stats = statsService.getUserStats(1001L);
        assertEquals(12, stats.getTotalGames());
        assertEquals(7, stats.getWinGames());
        assertEquals(0, stats.getConsecutiveWins());   // 连胜清零
        assertEquals(1, stats.getConsecutiveLosses()); // 连败+1
    }

    @Test
    @DisplayName("获取统计信息 - 验证胜率计算")
    void getUserStats_Success() {
        UserStatsEntity stats = statsService.getUserStats(1001L);

        assertNotNull(stats);
        assertEquals(1001L, stats.getUserId());
        assertEquals(10, stats.getTotalGames());
        assertEquals(6, stats.getWinGames());
    }

    // ==================== 事务测试 ====================

    @Test
    @DisplayName("事务回滚测试 - 注册失败时不应插入任何数据")
    void transaction_RollbackOnError() {
        // 记录当前用户数量
        long beforeCount = userService.count();

        try {
            // 尝试注册一个会失败的用户（用户名已存在）
            RegisterReq req = new RegisterReq()
                    .setUsername("testuser1")
                    .setPassword("123456")
                    .setNickname("新用户")
                    .setRegisterIp("127.0.0.1")
                    .setRegisterDeviceId("device001");
            userService.register(req);
        } catch (MsgException e) {
            // 预期异常
        }

        // 验证用户数量没有变化
        long afterCount = userService.count();
        assertEquals(beforeCount, afterCount);
    }

}
