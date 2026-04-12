package com.pokergame.user.service;

import com.pokergame.user.entity.UserRiskEntity;

/**
 * 用户风控服务
 */
public interface UserRiskService {

    /**
     * 保存风控信息
     * @param risk
     */
    void saveRisk(UserRiskEntity risk);

    /**
     * 更新用户首次登录信息
     * @param userId
     * @param ip
     * @param deviceId
     */
    void updateFirstLoginInfo(Long userId, String ip, String deviceId);
}
