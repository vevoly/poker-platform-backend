package com.pokergame.user.service.impl;

import com.pokergame.user.entity.UserRiskEntity;
import com.pokergame.user.mapper.UserRiskMapper;
import com.pokergame.user.service.UserRiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRiskServiceImpl implements UserRiskService {

    private final UserRiskMapper userRiskMapper;

    @Override
    public void saveRisk(UserRiskEntity risk) {
        userRiskMapper.insert(risk);
        log.debug("风控信息保存成功，userId={}", risk.getUserId());
    }

    @Override
    public void updateFirstLoginInfo(Long userId, String ip, String deviceId) {
        UserRiskEntity risk = userRiskMapper.selectById(userId);
        if (risk != null && risk.getFirstLoginTime() == null) {
            risk.setFirstLoginTime(LocalDateTime.now());
            risk.setFirstLoginIp(ip);
            risk.setFirstLoginDeviceId(deviceId);
            userRiskMapper.updateById(risk);
            log.debug("首次登录信息更新成功，userId={}", userId);
        }
    }
}
