package com.pokergame.user.service;

import com.pokergame.common.enums.ChangeCurrencyType;
import com.pokergame.common.enums.CurrencyType;
import com.pokergame.user.entity.UserCurrencyEntity;

import java.util.List;

/**
 * 货币服务接口
 *
 * @author poker-platform
 */
public interface CurrencyService {

    /**
     * 获取用户所有货币
     *
     * @param userId 用户ID
     * @return 货币列表
     */
    List<UserCurrencyEntity> getUserCurrencies(Long userId);

    /**
     * 获取用户指定货币
     *
     * @param userId       用户ID
     * @param currencyType 货币类型
     * @return 货币实体
     */
    UserCurrencyEntity getUserCurrency(Long userId, CurrencyType currencyType);

    /**
     * 增加货币（带乐观锁重试）
     *
     * @param userId       用户ID
     * @param currencyType 货币类型
     * @param amount       增加数量
     * @param changeType   变更类型
     * @param orderId      订单ID（可选）
     * @param remark       备注（可选）
     * @return 变更后数量
     */
    Long increaseCurrency(Long userId, CurrencyType currencyType, Long amount,
                          ChangeCurrencyType changeType, String orderId, String remark);

    /**
     * 减少货币（带乐观锁重试）
     *
     * @param userId       用户ID
     * @param currencyType 货币类型
     * @param amount       减少数量
     * @param changeType   变更类型
     * @param orderId      订单ID（可选）
     * @param remark       备注（可选）
     * @return 变更后数量
     */
    Long decreaseCurrency(Long userId, CurrencyType currencyType, Long amount,
                          ChangeCurrencyType changeType, String orderId, String remark);

    /**
     * 检查余额是否充足
     *
     * @param userId       用户ID
     * @param currencyType 货币类型
     * @param amount       需要数量
     * @return true-充足，false-不足
     */
    boolean checkBalance(Long userId, CurrencyType currencyType, Long amount);
}
