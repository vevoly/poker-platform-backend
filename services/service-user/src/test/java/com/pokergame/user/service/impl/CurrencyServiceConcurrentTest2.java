package com.pokergame.user.service.impl;

import com.pokergame.common.enums.ChangeCurrencyType;
import com.pokergame.common.enums.CurrencyType;
import com.pokergame.user.UserServerApplication;
import com.pokergame.user.entity.UserCurrencyEntity;
import com.pokergame.user.entity.UserEntity;
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
@DisplayName("货币服务并发测试")
public class CurrencyServiceConcurrentTest2 {
    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserCurrencyMapper userCurrencyMapper;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        // 每个测试前创建独立的测试用户
        testUserId = createTestUserWithCurrency(10000L);
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

    // ==================== 并发减少货币测试 ====================

    @Test
    @DisplayName("并发减少货币 - 5个线程各扣100，余额充足")
    void concurrentDecreaseCurrency_Success() throws InterruptedException {
        int threadCount = 5;
        long decrementAmount = 100L;
        long initialAmount = 10000L;
        long expectedAmount = initialAmount - threadCount * decrementAmount;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    currencyService.decreaseCurrency(testUserId, CurrencyType.GOLD, decrementAmount,
                            ChangeCurrencyType.GAME_LOSE, null, "并发扣减测试");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.err.println("扣减失败: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);

        executor.shutdown();
        assertTrue(completed, "并发测试超时");

        Thread.sleep(500);

        UserCurrencyEntity after = userCurrencyMapper.selectByUserIdAndType(testUserId, CurrencyType.GOLD.getCode());

        System.out.println("成功次数: " + successCount.get());
        System.out.println("失败次数: " + failureCount.get());
        System.out.println("期望余额: " + expectedAmount);
        System.out.println("实际余额: " + after.getAmount());

        assertEquals(threadCount, successCount.get(), "所有线程都应该成功");
        assertEquals(0, failureCount.get(), "不应该有失败");
        assertEquals(expectedAmount, after.getAmount(), "余额应该正确扣减");
    }

    @Test
    @DisplayName("并发减少货币 - 10个线程各扣100，余额充足")
    void concurrentDecreaseCurrency_10Threads() throws InterruptedException {
        int threadCount = 10;
        long decrementAmount = 100L;
        long initialAmount = 10000L;
        long expectedAmount = initialAmount - threadCount * decrementAmount;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    currencyService.decreaseCurrency(testUserId, CurrencyType.GOLD, decrementAmount,
                            ChangeCurrencyType.GAME_LOSE, null, "并发扣减测试");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("扣减失败: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        Thread.sleep(500);

        UserCurrencyEntity after = userCurrencyMapper.selectByUserIdAndType(testUserId, CurrencyType.GOLD.getCode());

        System.out.println("成功次数: " + successCount.get());
        System.out.println("最终余额: " + after.getAmount());

        assertEquals(threadCount, successCount.get(), "所有线程都应该成功");
        assertEquals(expectedAmount, after.getAmount(), "余额应该正确扣减");
    }

    // ==================== 余额不足场景测试 ====================

    @Test
    @DisplayName("并发减少货币 - 余额不足，部分成功")
    void concurrentDecreaseCurrency_InsufficientBalance() throws InterruptedException {
        int threadCount = 5;
        long decrementAmount = 300L;  // 每次扣300
        long initialAmount = 1000L;    // 初始只有1000，只能扣3次
        long maxSuccessCount = initialAmount / decrementAmount;  // 最多成功3次

        // 重新创建余额较少的用户
        userCurrencyMapper.deleteById(testUserId);
        userMapper.deleteById(testUserId);
        testUserId = createTestUserWithCurrency(initialAmount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    currencyService.decreaseCurrency(testUserId, CurrencyType.GOLD, decrementAmount,
                            ChangeCurrencyType.GAME_LOSE, null, "余额不足测试");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    // 预期会有余额不足的异常
                    System.out.println("扣减失败(预期): " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        Thread.sleep(500);

        UserCurrencyEntity after = userCurrencyMapper.selectByUserIdAndType(testUserId, CurrencyType.GOLD.getCode());
        long expectedAmount = initialAmount - successCount.get() * decrementAmount;

        System.out.println("成功次数: " + successCount.get());
        System.out.println("失败次数: " + failureCount.get());
        System.out.println("期望余额: " + expectedAmount);
        System.out.println("实际余额: " + after.getAmount());

        assertTrue(successCount.get() <= maxSuccessCount, "成功次数不能超过余额允许的次数");
        assertTrue(failureCount.get() > 0, "应该有失败的线程");
        assertEquals(expectedAmount, after.getAmount(), "余额应该正确扣减");
    }

    // ==================== 混合并发测试 ====================

    @Test
    @DisplayName("混合并发 - 同时增加和减少货币")
    void concurrentIncreaseAndDecrease() throws InterruptedException {
        int threadCount = 10;
        long increaseAmount = 100L;
        long decreaseAmount = 50L;
        long initialAmount = 10000L;
        long expectedAmount = initialAmount + threadCount * increaseAmount - threadCount * decreaseAmount;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount * 2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount * 2);
        AtomicInteger increaseSuccess = new AtomicInteger(0);
        AtomicInteger decreaseSuccess = new AtomicInteger(0);

        // 增加货币线程
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    currencyService.increaseCurrency(testUserId, CurrencyType.GOLD, increaseAmount,
                            ChangeCurrencyType.RECHARGE, null, "并发增加");
                    increaseSuccess.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("增加失败: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 减少货币线程
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    currencyService.decreaseCurrency(testUserId, CurrencyType.GOLD, decreaseAmount,
                            ChangeCurrencyType.GAME_LOSE, null, "并发减少");
                    decreaseSuccess.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("减少失败: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        Thread.sleep(500);

        UserCurrencyEntity after = userCurrencyMapper.selectByUserIdAndType(testUserId, CurrencyType.GOLD.getCode());

        System.out.println("增加成功次数: " + increaseSuccess.get());
        System.out.println("减少成功次数: " + decreaseSuccess.get());
        System.out.println("期望余额: " + expectedAmount);
        System.out.println("实际余额: " + after.getAmount());

        assertEquals(threadCount, increaseSuccess.get(), "所有增加都应该成功");
        assertEquals(threadCount, decreaseSuccess.get(), "所有减少都应该成功");
        assertEquals(expectedAmount, after.getAmount(), "余额应该正确计算");
    }

    // ==================== 高并发压力测试 ====================

    @Test
    @DisplayName("高并发压力测试 - 50个线程各扣10")
    void highConcurrencyDecreaseCurrency() throws InterruptedException {
        int threadCount = 50;
        long decrementAmount = 10L;
        long initialAmount = 10000L;
        long expectedAmount = initialAmount - threadCount * decrementAmount;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    currencyService.decreaseCurrency(testUserId, CurrencyType.GOLD, decrementAmount,
                            ChangeCurrencyType.GAME_LOSE, null, "压力测试");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("扣减失败: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        long startTime = System.currentTimeMillis();
        startLatch.countDown();
        endLatch.await(60, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        executor.shutdown();

        Thread.sleep(500);

        UserCurrencyEntity after = userCurrencyMapper.selectByUserIdAndType(testUserId, CurrencyType.GOLD.getCode());

        System.out.println("总线程数: " + threadCount);
        System.out.println("成功次数: " + successCount.get());
        System.out.println("耗时: " + (endTime - startTime) + "ms");
        System.out.println("最终余额: " + after.getAmount());

        assertEquals(threadCount, successCount.get(), "所有线程都应该成功");
        assertEquals(expectedAmount, after.getAmount(), "余额应该正确扣减");
        assertTrue((endTime - startTime) < 30000, "应该在30秒内完成");
    }
}

