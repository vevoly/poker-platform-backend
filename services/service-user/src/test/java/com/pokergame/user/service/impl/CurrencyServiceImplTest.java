package com.pokergame.user.service.impl;

import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.pokergame.common.enums.ChangeCurrencyType;
import com.pokergame.common.enums.CurrencyType;
import com.pokergame.user.UserServerApplication;
import com.pokergame.user.entity.CurrencyChangeLogEntity;
import com.pokergame.user.entity.UserCurrencyEntity;
import com.pokergame.user.entity.UserEntity;
import com.pokergame.user.mapper.CurrencyChangeLogMapper;
import com.pokergame.user.mapper.UserCurrencyMapper;
import com.pokergame.user.mapper.UserMapper;
import com.pokergame.user.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 货币服务集成测试
 *
 * <p>使用真实数据库和 Redis 进行测试
 *
 * @author poker-platform
 */
@SpringBootTest(classes = UserServerApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("货币服务集成测试")
class CurrencyServiceImplTest {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserCurrencyMapper userCurrencyMapper;

    @Autowired
    private CurrencyChangeLogMapper currencyChangeLogMapper;

    // 为每个测试方法创建独立的测试用户ID
    private Long testUserId = 1001L;
    private final CurrencyType goldCurrency = CurrencyType.GOLD;

    @BeforeEach
    void setUp() {
        // 确保测试数据存在且状态正确
        UserCurrencyEntity currency = userCurrencyMapper.selectByUserIdAndType(testUserId, goldCurrency.getCode());
        if (currency != null) {
            currency.setAmount(10000L);
            currency.setVersion(0);
            userCurrencyMapper.updateById(currency);
        }
    }

    // ==================== 查询货币测试 ====================

    @Test
    @DisplayName("获取用户所有货币 - 成功")
    void getUserCurrencies_Success() {
        // When
        List<UserCurrencyEntity> currencies = currencyService.getUserCurrencies(testUserId);

        // Then
        assertNotNull(currencies);
        assertFalse(currencies.isEmpty());

        // 验证金币存在
        UserCurrencyEntity gold = currencies.stream()
                .filter(c -> goldCurrency.equals(c.getCurrencyType()))
                .findFirst()
                .orElse(null);
        assertNotNull(gold);
        assertEquals(10000L, gold.getAmount());
    }

    @Test
    @DisplayName("获取用户指定货币 - 成功")
    void getUserCurrency_Success() {
        // When
        UserCurrencyEntity currency = currencyService.getUserCurrency(testUserId, goldCurrency);

        // Then
        assertNotNull(currency);
        assertEquals(testUserId, currency.getUserId());
        assertEquals(goldCurrency.getCode(), currency.getCurrencyType());
        assertEquals(10000L, currency.getAmount());
    }

    @Test
    @DisplayName("获取用户指定货币 - 货币类型不存在")
    void getUserCurrency_NotFound() {
        // When & Then
        assertThrows(MsgException.class, () -> {
            currencyService.getUserCurrency(testUserId, CurrencyType.ALLIANCE_COIN);
        });
    }

    // ==================== 增加货币测试 ====================

    @Test
    @DisplayName("增加货币 - 验证余额增加和乐观锁版本号变化")
    void increaseCurrency_Success() {
        // Given
        Long incrementAmount = 500L;

        // 记录增加前的状态
        UserCurrencyEntity before = currencyService.getUserCurrency(testUserId, goldCurrency);
        assertEquals(10000L, before.getAmount());
        assertEquals(1, before.getVersion());

        // When
        Long afterAmount = currencyService.increaseCurrency(
                testUserId, goldCurrency, incrementAmount,
                ChangeCurrencyType.RECHARGE, "ORDER_001", "测试充值");

        // Then
        assertEquals(10500L, afterAmount);

        // 验证数据库更新
        UserCurrencyEntity after = currencyService.getUserCurrency(testUserId, goldCurrency);
        assertEquals(10500L, after.getAmount());
        assertEquals(2, after.getVersion());  // 乐观锁版本号应该增加

        // 验证流水记录
        List<CurrencyChangeLogEntity> logs = currencyChangeLogMapper.selectByTimeRange(
                testUserId,
                java.time.LocalDateTime.now().minusMinutes(1),
                java.time.LocalDateTime.now().plusMinutes(1));
        assertFalse(logs.isEmpty());

        CurrencyChangeLogEntity log = logs.stream()
                .filter(l -> l.getChangeType().equals("RECHARGE"))
                .findFirst()
                .orElse(null);
        assertNotNull(log);
        assertEquals(incrementAmount, log.getChangeAmount());
        assertEquals(10000L, log.getBeforeAmount());
        assertEquals(10500L, log.getAfterAmount());
    }

    @Test
    @DisplayName("增加货币 - 多次增加验证版本号累加")
    void increaseCurrency_MultipleTimes() {
        // When
        currencyService.increaseCurrency(testUserId, goldCurrency, 100L, ChangeCurrencyType.RECHARGE, null, null);
        currencyService.increaseCurrency(testUserId, goldCurrency, 200L, ChangeCurrencyType.RECHARGE, null, null);
        currencyService.increaseCurrency(testUserId, goldCurrency, 300L, ChangeCurrencyType.RECHARGE, null, null);

        // Then
        UserCurrencyEntity after = currencyService.getUserCurrency(testUserId, goldCurrency);
        assertEquals(10600L, after.getAmount());  // 10000 + 100 + 200 + 300
        assertEquals(3, after.getVersion());       // 版本号应该增加到 3
    }

    @Test
    @DisplayName("增加货币失败 - 参数错误（增加数量为负数）")
    void increaseCurrency_InvalidAmount() {
        // When & Then
        assertThrows(MsgException.class, () -> {
            currencyService.increaseCurrency(testUserId, goldCurrency, -100L, ChangeCurrencyType.RECHARGE, null, null);
        });
    }

    @Test
    @DisplayName("增加货币失败 - 参数错误（数量为 null）")
    void increaseCurrency_NullAmount() {
        // When & Then
        assertThrows(MsgException.class, () -> {
            currencyService.increaseCurrency(testUserId, goldCurrency, null, ChangeCurrencyType.RECHARGE, null, null);
        });
    }

    // ==================== 减少货币测试 ====================

    @Test
    @DisplayName("减少货币 - 验证余额扣减和流水记录")
    void decreaseCurrency_Success() {
        // Given
        Long decrementAmount = 300L;

        // When
        Long afterAmount = currencyService.decreaseCurrency(
                testUserId, goldCurrency, decrementAmount,
                ChangeCurrencyType.GAME_LOSE, null, "斗地主输牌");

        // Then
        assertEquals(9700L, afterAmount);

        // 验证数据库更新
        UserCurrencyEntity after = currencyService.getUserCurrency(testUserId, goldCurrency);
        assertEquals(9700L, after.getAmount());
        assertEquals(2, after.getVersion());

        // 验证流水记录（负数）
        List<CurrencyChangeLogEntity> logs = currencyChangeLogMapper.selectByTimeRange(
                testUserId,
                java.time.LocalDateTime.now().minusMinutes(1),
                java.time.LocalDateTime.now().plusMinutes(1));
        assertFalse(logs.isEmpty());

        CurrencyChangeLogEntity log = logs.stream()
                .filter(l -> l.getChangeType().equals("GAME_LOSE"))
                .findFirst()
                .orElse(null);
        assertNotNull(log);
        assertEquals(-decrementAmount, log.getChangeAmount());
        assertEquals(10000L, log.getBeforeAmount());
        assertEquals(9700L, log.getAfterAmount());
    }

    @Test
    @DisplayName("减少货币失败 - 余额不足")
    void decreaseCurrency_InsufficientBalance() {
        // When & Then
        assertThrows(MsgException.class, () -> {
            currencyService.decreaseCurrency(testUserId, goldCurrency, 20000L, ChangeCurrencyType.GAME_LOSE, null, null);
        });

        // 验证余额没有被扣减
        UserCurrencyEntity after = currencyService.getUserCurrency(testUserId, goldCurrency);
        assertEquals(10000L, after.getAmount());
    }

    @Test
    @DisplayName("减少货币 - 多次扣减验证余额和版本号")
    void decreaseCurrency_MultipleTimes() {
        // When
        currencyService.decreaseCurrency(testUserId, goldCurrency, 100L, ChangeCurrencyType.GAME_LOSE, null, null);
        currencyService.decreaseCurrency(testUserId, goldCurrency, 200L, ChangeCurrencyType.GAME_LOSE, null, null);
        currencyService.decreaseCurrency(testUserId, goldCurrency, 300L, ChangeCurrencyType.GAME_LOSE, null, null);

        // Then
        UserCurrencyEntity after = currencyService.getUserCurrency(testUserId, goldCurrency);
        assertEquals(9400L, after.getAmount());  // 10000 - 100 - 200 - 300
        assertEquals(3, after.getVersion());
    }

    // ==================== 余额检查测试 ====================

    @Test
    @DisplayName("余额检查 - 余额充足")
    void checkBalance_Sufficient() {
        // When
        boolean result = currencyService.checkBalance(testUserId, goldCurrency, 500L);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("余额检查 - 余额不足")
    void checkBalance_Insufficient() {
        // When
        boolean result = currencyService.checkBalance(testUserId, goldCurrency, 20000L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("余额检查 - 边界值测试（刚好等于余额）")
    void checkBalance_ExactlyEqual() {
        // When
        boolean result = currencyService.checkBalance(testUserId, goldCurrency, 10000L);

        // Then
        assertTrue(result);
    }

    // ==================== 流水记录测试 ====================

    @Test
    @DisplayName("流水记录 - 验证流水包含正确的变更信息")
    void currencyChangeLog_ContainsCorrectInfo() {
        // When
        Long beforeAmount = currencyService.getUserCurrency(testUserId, goldCurrency).getAmount();
        currencyService.decreaseCurrency(testUserId, goldCurrency, 500L, ChangeCurrencyType.GAME_LOSE, "ORDER_123", "测试备注");
        Long afterAmount = currencyService.getUserCurrency(testUserId, goldCurrency).getAmount();

        // Then
        List<CurrencyChangeLogEntity> logs = currencyChangeLogMapper.selectByTimeRange(
                testUserId,
                java.time.LocalDateTime.now().minusMinutes(1),
                java.time.LocalDateTime.now().plusMinutes(1));

        CurrencyChangeLogEntity log = logs.stream()
                .filter(l -> "ORDER_123".equals(l.getOrderId()))
                .findFirst()
                .orElse(null);

        assertNotNull(log);
        assertEquals(testUserId, log.getUserId());
        assertEquals(goldCurrency, log.getCurrencyType());
        assertEquals(-500L, log.getChangeAmount());
        assertEquals(beforeAmount, log.getBeforeAmount());
        assertEquals(afterAmount, log.getAfterAmount());
        assertEquals("GAME_LOSE", log.getChangeType());
        assertEquals("ORDER_123", log.getOrderId());
        assertEquals("测试备注", log.getRemark());
        assertNotNull(log.getCreateTime());
    }
}
