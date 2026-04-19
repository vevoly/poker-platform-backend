package com.pokergame.user.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pokergame.common.enums.ChangeCurrencyType;
import com.pokergame.common.enums.CurrencyType;
import com.pokergame.common.model.auth.LoginReq;
import com.pokergame.common.model.user.RegisterReq;
import com.pokergame.common.exception.GameCode;
import com.pokergame.user.entity.UserEntity;
import com.pokergame.user.entity.UserLoginLogEntity;
import com.pokergame.user.entity.UserRiskEntity;
import com.pokergame.user.mapper.UserMapper;
import com.pokergame.user.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 用户服务实现类
 *
 * <p>异常处理使用 ioGame 规范：直接抛出 GameCode 断言
 *
 * @author poker-platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    private final UserMapper userMapper;
    private final CurrencyService currencyService;
    private final UserRiskService userRiskService;
    private final UserStatsService userStatsService;
    private final UserLoginLogService userLoginLogService;

    private final BCryptPasswordEncoder passwordEncoder;

    // ==================== 注册 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterReq req) {
        log.info("开始用户注册，username={}, mobile={}, email={}", req.getUsername(), req.getMobile(), req.getEmail());

        // 1. 账号信息至少提供一种
        boolean hasUsername = StrUtil.isNotBlank(req.getUsername());
        boolean hasMobile = StrUtil.isNotBlank(req.getMobile());
        boolean hasEmail = StrUtil.isNotBlank(req.getEmail());
        GameCode.PARAM_ERROR.assertTrueThrows(!hasUsername && !hasMobile && !hasEmail,
                "用户名、手机号、邮箱至少填写一项");

        // 2. 业务唯一性校验（格式校验应在 Action 层完成）
        if (hasUsername) {
            GameCode.USERNAME_EXISTS.assertTrueThrows(userMapper.countByUsername(req.getUsername()) > 0);
        }
        if (hasMobile) {
            GameCode.MOBILE_EXISTS.assertTrueThrows(userMapper.countByMobile(req.getMobile()) > 0);
        }
        if (hasEmail) {
            GameCode.EMAIL_EXISTS.assertTrueThrows(userMapper.countByEmail(req.getEmail()) > 0);
        }

        // 3. 生成 user_code
        String userCode = "USC" + IdUtil.getSnowflakeNextIdStr().substring(0, 6);

        // 4. 构建用户实体
        UserEntity user = new UserEntity();
        user.setUserCode(userCode);
        user.setUsername(req.getUsername());
        user.setMobile(req.getMobile());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setBindMobile(hasMobile ? 1 : 0);
        user.setBindEmail(hasEmail ? 1 : 0);
        user.setNickname(StrUtil.isNotBlank(req.getNickname()) ? req.getNickname() :
                (hasUsername ? req.getUsername() : (hasMobile ? req.getMobile() : req.getEmail().split("@")[0])));
        user.setStatus(1);
        user.setExtra("{}");
        userMapper.insert(user);
        Long userId = user.getId();

        // 5. 初始化钱包（赠送 10000 金币）
        currencyService.increaseCurrency(userId, CurrencyType.GOLD, 10000L,
                ChangeCurrencyType.SYSTEM, null, "注册赠送");

        // 6. 初始化统计信息
        userStatsService.initUserStats(userId);

        // 7. 保存风控信息
        UserRiskEntity risk = new UserRiskEntity();
        risk.setUserId(userId);
        risk.setRegisterIp(req.getRegisterIp());
        risk.setRegisterDeviceId(req.getRegisterDeviceId());
        risk.setRegisterUserAgent(req.getRegisterUserAgent());
        risk.setRegisterChannel(req.getRegisterChannel());
        risk.setRegisterLatitude(req.getRegisterLatitude());
        risk.setRegisterLongitude(req.getRegisterLongitude());
        risk.setRiskScore(0);
        userRiskService.saveRisk(risk);

        log.info("用户注册成功，userId={}, userCode={}", userId, userCode);
        return userId;
    }

    // ==================== 登录 ====================

    @Override
    public UserEntity verifyCredential(LoginReq req) {
        log.info("验证用户凭证: username={}, mobile={}, email={}",
                req.getUsername(), req.getMobile(), req.getEmail());

        // 1. 查询用户
        UserEntity user = null;
        if (StrUtil.isNotBlank(req.getUsername())) {
            user = userMapper.selectByUsername(req.getUsername());
        } else if (StrUtil.isNotBlank(req.getMobile())) {
            user = userMapper.selectByMobile(req.getMobile());
        } else if (StrUtil.isNotBlank(req.getEmail())) {
            user = userMapper.selectByEmail(req.getEmail());
        } else {
            GameCode.PARAM_ERROR.assertTrueThrows(true, "用户名/手机号/邮箱不能为空");
        }
        GameCode.USER_NOT_FOUND.assertTrueThrows(user == null);

        // 2. 校验密码
        boolean passwordMatch = passwordEncoder.matches(req.getPassword(), user.getPassword());
        GameCode.PASSWORD_ERROR.assertTrueThrows(!passwordMatch);

        // 3. 校验用户状态
        GameCode.USER_DISABLED.assertTrueThrows(user.getStatus() == null || user.getStatus() != 1);

        // 注意：不更新 last_login_time，不生成 Token，不记录日志
        return user;
    }

    // ==================== 查询方法 ====================

    @Override
    public UserEntity getByUsername(String username) {
        return StrUtil.isBlank(username) ? null : userMapper.selectByUsername(username);
    }

    @Override
    public UserEntity getByMobile(String mobile) {
        return StrUtil.isBlank(mobile) ? null : userMapper.selectByMobile(mobile);
    }

    @Override
    public UserEntity getByEmail(String email) {
        return StrUtil.isBlank(email) ? null : userMapper.selectByEmail(email);
    }

    @Override
    public UserEntity checkAndGetUser(Long userId) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");
        UserEntity user = getById(userId);
        GameCode.USER_NOT_FOUND.assertTrueThrows(user == null);
        GameCode.USER_DISABLED.assertTrueThrows(user.getStatus() == null || user.getStatus() != 1);
        return user;
    }

    @Override
    public void processLoginSuccess(Long userId, LoginReq loginReq) {
        log.info("处理登录成功后续: userId={}", userId);

        // 1. 更新最后登录信息
        UserEntity user = getById(userId);
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginTime(now);
        user.setLastLoginIp(loginReq.getLoginIp());
        user.setLastLoginDeviceId(loginReq.getLoginDeviceId());
        user.setLastLoginLatitude(loginReq.getLoginLatitude());
        user.setLastLoginLongitude(loginReq.getLoginLongitude());
        updateById(user);

        // 2. 更新风控表中的首次登录信息
        userRiskService.updateFirstLoginInfo(userId, loginReq.getLoginIp(), loginReq.getLoginDeviceId());

        // 3. 记录登录日志
        UserLoginLogEntity logEntity = new UserLoginLogEntity();
        logEntity.setUserId(userId);
        logEntity.setLoginTime(now);
        logEntity.setLoginIp(loginReq.getLoginIp());
        logEntity.setLoginDeviceId(loginReq.getLoginDeviceId());
        logEntity.setLoginUserAgent(loginReq.getLoginUserAgent());
        logEntity.setLoginLatitude(loginReq.getLoginLatitude());
        logEntity.setLoginLongitude(loginReq.getLoginLongitude());
        logEntity.setLoginResult(1);
        logEntity.setToken(null); // Token 由 Auth 服务管理，这里不记录
        userLoginLogService.recordLoginLog(logEntity);
    }

    @Override
    public List<UserEntity> getRobotAccounts() {
        return userMapper.selectList(Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getIsRobot, 1)
                .eq(UserEntity::getRobotEnabled, 1));
    }

}
