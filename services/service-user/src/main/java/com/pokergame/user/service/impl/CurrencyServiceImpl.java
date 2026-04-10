package com.pokergame.user.service.impl;

import com.pokergame.core.exception.GameCode;
import com.pokergame.user.entity.CurrencyChangeLogEntity;
import com.pokergame.user.entity.UserCurrencyEntity;
import com.pokergame.user.mapper.CurrencyChangeLogMapper;
import com.pokergame.user.mapper.UserCurrencyMapper;
import com.pokergame.user.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 货币服务实现类
 *
 * <p>使用乐观锁 + 重试机制保证并发安全
 *
 * @author poker-platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final UserCurrencyMapper userCurrencyMapper;
    private final CurrencyChangeLogMapper currencyChangeLogMapper;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Override
    public List<UserCurrencyEntity> getUserCurrencies(Long userId) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");
        return userCurrencyMapper.selectByUserId(userId);
    }

    @Override
    public UserCurrencyEntity getUserCurrency(Long userId, String currencyType) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(currencyType == null || currencyType.isEmpty(), "货币类型不能为空");

        UserCurrencyEntity currency = userCurrencyMapper.selectByUserIdAndType(userId, currencyType);
        GameCode.CURRENCY_NOT_FOUND.assertTrueThrows(currency == null);

        return currency;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Retryable(maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 100, multiplier = 2))
    public Long increaseCurrency(Long userId, String currencyType, Long amount,
                                 String changeType, String orderId, String remark) {
        log.debug("增加货币，userId: {}, currencyType: {}, amount: {}", userId, currencyType, amount);

        // 1. 参数校验
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(currencyType == null || currencyType.isEmpty(), "货币类型不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(amount == null || amount <= 0, "增加数量必须大于0");
        GameCode.PARAM_ERROR.assertTrueThrows(changeType == null || changeType.isEmpty(), "变更类型不能为空");

        // 2. 获取当前货币信息
        UserCurrencyEntity currency = userCurrencyMapper.selectByUserIdAndType(userId, currencyType);
        GameCode.CURRENCY_NOT_FOUND.assertTrueThrows(currency == null);

        Long beforeAmount = currency.getAmount();
        Integer version = currency.getVersion();

        // 3. 更新货币（乐观锁）
        int result = userCurrencyMapper.increaseAmount(userId, currencyType, amount, version);

        // 乐观锁冲突，抛出异常触发重试
        if (result <= 0) {
            log.warn("增加货币失败，乐观锁冲突，将重试，userId: {}, currencyType: {}, amount: {}",
                    userId, currencyType, amount);
            GameCode.CURRENCY_OPERATION_CONFLICT.assertTrueThrows(true);
        }

        Long afterAmount = beforeAmount + amount;

        // 4. 记录流水
        saveChangeLog(userId, currencyType, amount, beforeAmount, afterAmount,
                changeType, orderId, remark);

        log.info("增加货币成功，userId: {}, currencyType: {}, afterAmount: {}",
                userId, currencyType, afterAmount);
        return afterAmount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Retryable(maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 100, multiplier = 2))
    public Long decreaseCurrency(Long userId, String currencyType, Long amount,
                                 String changeType, String orderId, String remark) {
        log.debug("减少货币，userId: {}, currencyType: {}, amount: {}", userId, currencyType, amount);

        // 1. 参数校验
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(currencyType == null || currencyType.isEmpty(), "货币类型不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(amount == null || amount <= 0, "减少数量必须大于0");
        GameCode.PARAM_ERROR.assertTrueThrows(changeType == null || changeType.isEmpty(), "变更类型不能为空");

        // 2. 获取当前货币信息
        UserCurrencyEntity currency = userCurrencyMapper.selectByUserIdAndType(userId, currencyType);
        GameCode.CURRENCY_NOT_FOUND.assertTrueThrows(currency == null);

        // 3. 余额检查
        if (currency.getAmount() < amount) {
            log.warn("余额不足，userId: {}, currencyType: {}, balance: {}, need: {}",
                    userId, currencyType, currency.getAmount(), amount);
            GameCode.CURRENCY_NOT_ENOUGH.assertTrueThrows(true);
        }

        Long beforeAmount = currency.getAmount();
        Integer version = currency.getVersion();

        // 4. 更新货币（乐观锁 + 余额检查）
        int result = userCurrencyMapper.decreaseAmount(userId, currencyType, amount, version);

        // 乐观锁冲突或余额不足，抛出异常触发重试
        if (result <= 0) {
            log.warn("减少货币失败，乐观锁冲突或余额不足，将重试，userId: {}, currencyType: {}, amount: {}",
                    userId, currencyType, amount);
            GameCode.CURRENCY_OPERATION_CONFLICT.assertTrueThrows(true);
        }

        Long afterAmount = beforeAmount - amount;

        // 5. 记录流水
        saveChangeLog(userId, currencyType, -amount, beforeAmount, afterAmount,
                changeType, orderId, remark);

        log.info("减少货币成功，userId: {}, currencyType: {}, afterAmount: {}",
                userId, currencyType, afterAmount);
        return afterAmount;
    }

    @Override
    public boolean checkBalance(Long userId, String currencyType, Long amount) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(currencyType == null || currencyType.isEmpty(), "货币类型不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(amount == null || amount <= 0, "检查数量必须大于0");

        UserCurrencyEntity currency = userCurrencyMapper.selectByUserIdAndType(userId, currencyType);
        if (currency == null) {
            return false;
        }
        return currency.getAmount() >= amount;
    }

    /**
     * 保存货币变更流水
     */
    private void saveChangeLog(Long userId, String currencyType, Long changeAmount,
                               Long beforeAmount, Long afterAmount,
                               String changeType, String orderId, String remark) {
        CurrencyChangeLogEntity logEntity = new CurrencyChangeLogEntity();
        logEntity.setUserId(userId);
        logEntity.setCurrencyType(currencyType);
        logEntity.setChangeAmount(changeAmount);
        logEntity.setBeforeAmount(beforeAmount);
        logEntity.setAfterAmount(afterAmount);
        logEntity.setChangeType(changeType);
        logEntity.setOrderId(orderId);
        logEntity.setRemark(remark);

        currencyChangeLogMapper.insert(logEntity);
    }
}
