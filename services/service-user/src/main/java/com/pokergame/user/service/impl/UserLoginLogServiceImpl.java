package com.pokergame.user.service.impl;

import com.pokergame.user.entity.UserLoginLogEntity;
import com.pokergame.user.mapper.UserLoginLogMapper;
import com.pokergame.user.service.UserLoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLoginLogServiceImpl implements UserLoginLogService {

    private final UserLoginLogMapper userLoginLogMapper;

    @Override
    public void recordLoginLog(UserLoginLogEntity userLoginLog) {
        userLoginLogMapper.insert(userLoginLog);
        log.debug("登录日志记录成功，userId={}", userLoginLog.getUserId());
    }
}
