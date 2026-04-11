package com.pokergame.user.service.impl;

import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.pokergame.common.enums.ChangeCurrencyType;
import com.pokergame.common.enums.CurrencyType;
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

    private static final int MAX_RETRY_ATTEMPTS = 10;  // 增加到 10 次
    private static final long RETRY_DELAY_MS = 100;    // 增加初始延迟
    private static final double RETRY_MULTIPLIER = 1.5;    // 增加重试延迟倍数
    private static final int RETRY_MAX_DELAY_MS = 1000; // 增加最大延迟

    @Override
    public List<UserCurrencyEntity> getUserCurrencies(Long userId) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");
        return userCurrencyMapper.selectByUserId(userId);
    }

    @Override
    public UserCurrencyEntity getUserCurrency(Long userId, CurrencyType currencyType) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(currencyType == null, "货币类型不能为空");

        UserCurrencyEntity currency = userCurrencyMapper.selectByUserIdAndType(userId, currencyType.getCode());
        GameCode.CURRENCY_NOT_FOUND.assertTrueThrows(currency == null);

        return currency;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Retryable(
            value = {MsgException.class},  // 明确指定重试的异常类型
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = RETRY_DELAY_MS, multiplier = RETRY_MULTIPLIER, random = true, maxDelay = RETRY_MAX_DELAY_MS)
    )
    public Long increaseCurrency(Long userId, CurrencyType currencyType, Long amount,
                                 ChangeCurrencyType changeType, String orderId, String remark) {
        log.debug("增加货币，userId: {}, currencyType: {}, amount: {}", userId, currencyType.getCode(), amount);

        // 1. 参数校验
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(currencyType == null, "货币类型不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(amount == null || amount <= 0, "增加数量必须大于0");
        GameCode.PARAM_ERROR.assertTrueThrows(changeType == null, "变更类型不能为空");

        // 2. 获取当前货币信息
        UserCurrencyEntity currency = userCurrencyMapper.selectByUserIdAndType(userId, currencyType.getCode());
        GameCode.CURRENCY_NOT_FOUND.assertTrueThrows(currency == null);

        Long beforeAmount = currency.getAmount();
        Integer version = currency.getVersion();

        // 3. 更新货币（乐观锁）
        int result = userCurrencyMapper.increaseAmount(userId, currencyType.getCode(), amount, version);

        // 乐观锁冲突，抛出 MsgException 触发重试
        if (result <= 0) {
            log.warn("增加货币失败，乐观锁冲突，将重试，userId: {}, currencyType: {}, amount: {}, version: {}",
                    userId, currencyType.getCode(), amount, version);
            // 直接抛出 MsgException，而不是使用 GameCode 的断言
            throw new MsgException(GameCode.CURRENCY_OPERATION_CONFLICT.getCode(),
                    GameCode.CURRENCY_OPERATION_CONFLICT.getMsg());
        }

        Long afterAmount = beforeAmount + amount;

        // 4. 记录流水
        saveChangeLog(userId, currencyType.getCode(), amount, beforeAmount, afterAmount,
                changeType.getCode(), orderId, remark);

        log.info("增加货币成功，userId: {}, currencyType: {}, afterAmount: {}, version: {} -> {}",
                userId, currencyType.getCode(), afterAmount, version, version + 1);
        return afterAmount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Retryable(
            value = {MsgException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = RETRY_DELAY_MS, multiplier = RETRY_MULTIPLIER, random = true, maxDelay = RETRY_MAX_DELAY_MS)
    )
    public Long decreaseCurrency(Long userId, CurrencyType currencyType, Long amount,
                                 ChangeCurrencyType changeType, String orderId, String remark) {
        log.debug("减少货币，userId: {}, currencyType: {}, amount: {}", userId, currencyType.getCode(), amount);

        // 1. 参数校验
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(currencyType == null, "货币类型不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(amount == null || amount <= 0, "减少数量必须大于0");
        GameCode.PARAM_ERROR.assertTrueThrows(changeType == null, "变更类型不能为空");

        // 2. 获取当前货币信息
        UserCurrencyEntity currency = userCurrencyMapper.selectByUserIdAndType(userId, currencyType.getCode());
        GameCode.CURRENCY_NOT_FOUND.assertTrueThrows(currency == null);

        // 3. 余额检查
        if (currency.getAmount() < amount) {
            log.warn("余额不足，userId: {}, currencyType: {}, balance: {}, need: {}",
                    userId, currencyType.getCode(), currency.getAmount(), amount);
            GameCode.CURRENCY_NOT_ENOUGH.assertTrueThrows(true);
        }

        Long beforeAmount = currency.getAmount();
        Integer version = currency.getVersion();

        // 4. 更新货币（乐观锁 + 余额检查）
        int result = userCurrencyMapper.decreaseAmount(userId, currencyType.getCode(), amount, version);

        // 乐观锁冲突，抛出 MsgException 触发重试
        if (result <= 0) {
            log.warn("减少货币失败，乐观锁冲突，将重试，userId: {}, currencyType: {}, amount: {}, version: {}",
                    userId, currencyType.getCode(), amount, version);
            throw new MsgException(GameCode.CURRENCY_OPERATION_CONFLICT.getCode(),
                    GameCode.CURRENCY_OPERATION_CONFLICT.getMsg());
        }

        Long afterAmount = beforeAmount - amount;

        // 5. 记录流水
        saveChangeLog(userId, currencyType.getCode(), -amount, beforeAmount, afterAmount,
                changeType.getCode(), orderId, remark);

        log.info("减少货币成功，userId: {}, currencyType: {}, afterAmount: {}, version: {} -> {}",
                userId, currencyType.getCode(), afterAmount, version, version + 1);
        return afterAmount;
    }

    @Override
    public boolean checkBalance(Long userId, CurrencyType currencyType, Long amount) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(currencyType == null, "货币类型不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(amount == null || amount <= 0, "检查数量必须大于0");

        UserCurrencyEntity currency = userCurrencyMapper.selectByUserIdAndType(userId, currencyType.getCode());
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
