package com.pokergame.user.service.impl;


import com.pokergame.common.enums.ChangeCurrencyType;
import com.pokergame.common.enums.CurrencyType;
import com.pokergame.user.UserServerApplication;
import com.pokergame.user.entity.UserCurrencyEntity;
import com.pokergame.user.entity.UserEntity;
import com.pokergame.user.mapper.CurrencyChangeLogMapper;
import com.pokergame.user.mapper.UserCurrencyMapper;
import com.pokergame.user.mapper.UserMapper;
import com.pokergame.user.service.CurrencyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = UserServerApplication.class)
@ActiveProfiles("test")
// 移除 @Transactional，让数据真实提交
@DisplayName("货币服务集成测试")
class CurrencyServiceConcurrentTest {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserCurrencyMapper userCurrencyMapper;

    @Autowired
    private CurrencyChangeLogMapper currencyChangeLogMapper;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        // 不在这里创建用户，每个测试独立创建
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        if (testUserId != null) {
            userCurrencyMapper.deleteById(testUserId);
            userMapper.deleteById(testUserId);
        }
    }

    private Long createTestUserWithCurrency(long initialAmount) {
        // 创建测试用户
        UserEntity user = new UserEntity();
        user.setUsername("test_concurrent_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        user.setPassword("$2a$10$test");
        user.setNickname("并发测试用户");
        user.setStatus(1);
        userMapper.insert(user);

        // 初始化货币
        UserCurrencyEntity currency = new UserCurrencyEntity();
        currency.setUserId(user.getId());
        currency.setCurrencyType(CurrencyType.GOLD.getCode());
        currency.setAmount(initialAmount);
        currency.setVersion(0);
        userCurrencyMapper.insert(currency);

        return user.getId();
    }

    @Test
    @DisplayName("并发增加货币 - 验证行锁正确工作")
    void concurrentIncreaseCurrency() throws InterruptedException {
        // 创建独立的测试用户
        testUserId = createTestUserWithCurrency(10000L);

        // 等待数据提交
        Thread.sleep(100);

        // 验证数据存在
        UserCurrencyEntity check = userCurrencyMapper.selectByUserIdAndType(testUserId, CurrencyType.GOLD.getCode());
        assertNotNull(check, "测试数据应该存在");
        assertEquals(10000L, check.getAmount());

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    currencyService.increaseCurrency(testUserId, CurrencyType.GOLD, 100L,
                            ChangeCurrencyType.RECHARGE, null, "并发测试");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("线程执行失败: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);

        executor.shutdown();
        assertTrue(completed, "并发测试超时");

        // 等待一下，确保所有事务提交
        Thread.sleep(500);

        UserCurrencyEntity after = userCurrencyMapper.selectByUserIdAndType(testUserId, CurrencyType.GOLD.getCode());
        assertNotNull(after, "货币记录应该存在");

        System.out.println("成功次数: " + successCount.get());
        System.out.println("最终余额: " + after.getAmount());

        assertEquals(10000L + threadCount * 100L, after.getAmount(),
                String.format("期望: %d, 实际: %d", 10000L + threadCount * 100L, after.getAmount()));
    }
}
