package com.pokergame.user.service;

import com.pokergame.user.entity.UserLoginLogEntity;

/**
 * 用户登录日志服务
 */
public interface UserLoginLogService {

    /**
     * 记录登录日志
     * @param log
     */
    void recordLoginLog(UserLoginLogEntity log);
}
