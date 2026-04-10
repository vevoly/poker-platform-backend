package com.pokergame.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pokergame.common.model.user.LoginResp;
import com.pokergame.core.exception.GameCode;
import com.pokergame.user.entity.UserCurrencyEntity;
import com.pokergame.user.entity.UserEntity;
import com.pokergame.user.entity.UserStatsEntity;
import com.pokergame.user.mapper.UserCurrencyMapper;
import com.pokergame.user.mapper.UserMapper;
import com.pokergame.user.mapper.UserStatsMapper;
import com.pokergame.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
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
    private final UserCurrencyMapper userCurrencyMapper;
    private final UserStatsMapper userStatsMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 用户注册（事务）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(String username, String password, String nickname) {
        log.info("开始用户注册，username: {}", username);

        // 1. 参数校验
        validateUsername(username);
        validatePassword(password);

        String finalNickname = StrUtil.isNotBlank(nickname) ? nickname : username;
        validateNickname(finalNickname);

        // 2. 校验用户名是否已存在
        int count = userMapper.countByUsername(username);
        GameCode.USERNAME_EXISTS.assertTrueThrows(count > 0);

        // 3. 构建用户实体
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(finalNickname);
        user.setStatus(1);
        user.setExtra("{}");

        // 4. 插入用户表
        int result = userMapper.insert(user);
        GameCode.USER_REGISTER_FAILED.assertTrueThrows(result <= 0);

        Long userId = user.getId();
        log.info("用户注册成功，userId: {}, username: {}", userId, username);

        // 5. 初始化货币（金币10000）
        List<UserCurrencyEntity> currencies = Arrays.asList(
                buildCurrency(userId, "GOLD", 10000L)
        );
        userCurrencyMapper.batchInsert(currencies);

        // 6. 初始化统计
        UserStatsEntity stats = new UserStatsEntity();
        stats.setUserId(userId);
        stats.setTotalGames(0);
        stats.setWinGames(0);
        stats.setConsecutiveWins(0);
        stats.setConsecutiveLosses(0);
        userStatsMapper.insert(stats);

        log.info("用户初始化完成，userId: {}, 金币: {}", userId, 10000L);
        return userId;
    }

    /**
     * 用户登录
     */
    @Override
    public UserEntity login(String username, String password) {
        log.info("用户登录，username: {}", username);

        // 1. 参数校验
        GameCode.PARAM_ERROR.assertTrueThrows(StrUtil.isBlank(username), "用户名不能为空");
        GameCode.PARAM_ERROR.assertTrueThrows(StrUtil.isBlank(password), "密码不能为空");

        // 2. 查询用户
        UserEntity user = userMapper.selectByUsername(username);
        GameCode.USER_NOT_FOUND.assertTrueThrows(user == null);

        // 3. 校验密码
        boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
        GameCode.PASSWORD_ERROR.assertTrueThrows(!passwordMatch);

        // 4. 校验状态
        GameCode.USER_DISABLED.assertTrueThrows(user.getStatus() == null || user.getStatus() != 1);

        // 5. 更新最后登录时间
        recordLoginTime(user.getId(), LocalDateTime.now());

        log.info("用户登录成功，userId: {}, username: {}", user.getId(), username);
        return user;
    }

    /**
     * 根据用户名查询用户
     */
    @Override
    public UserEntity getByUsername(String username) {
        GameCode.PARAM_ERROR.assertTrueThrows(StrUtil.isBlank(username), "用户名不能为空");
        return userMapper.selectByUsername(username);
    }

    /**
     * 更新最后登录时间
     */
    @Override
    public void updateLastLoginTime(Long userId) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");

        int result = userMapper.updateLastLoginTime(userId, LocalDateTime.now());
        if (result <= 0) {
            log.warn("更新最后登录时间失败，userId: {}", userId);
        }
    }

    /**
     * 校验用户是否存在并返回
     */
    @Override
    public UserEntity checkAndGetUser(Long userId) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");

        UserEntity user = getById(userId);
        GameCode.USER_NOT_FOUND.assertTrueThrows(user == null);
        GameCode.USER_DISABLED.assertTrueThrows(user.getStatus() == null || user.getStatus() != 1);

        return user;
    }

    /**
     * 更新用户最后登录时间
     * @param userId
     * @param loginTime
     */
    @Async
    public void recordLoginTime(Long userId, LocalDateTime loginTime) {
        userMapper.updateLastLoginTime(userId, loginTime);
    }

    /**
     * 校验用户名格式
     */
    private void validateUsername(String username) {
        GameCode.PARAM_ERROR.assertTrueThrows(StrUtil.isBlank(username), "用户名不能为空");
        boolean isValid = username.matches("^[a-zA-Z0-9]{4,20}$");
        GameCode.USERNAME_INVALID.assertTrueThrows(!isValid);
    }

    /**
     * 校验密码格式
     */
    private void validatePassword(String password) {
        GameCode.PARAM_ERROR.assertTrueThrows(StrUtil.isBlank(password), "密码不能为空");
        boolean isValid = password.length() >= 6 && password.length() <= 20;
        GameCode.PASSWORD_INVALID.assertTrueThrows(!isValid);
    }

    /**
     * 校验昵称格式
     */
    private void validateNickname(String nickname) {
        boolean isValid = nickname.length() >= 1 && nickname.length() <= 20;
        GameCode.NICKNAME_INVALID.assertTrueThrows(!isValid);
    }

    /**
     * 构建货币实体
     */
    private UserCurrencyEntity buildCurrency(Long userId, String currencyType, Long amount) {
        UserCurrencyEntity currency = new UserCurrencyEntity();
        currency.setUserId(userId);
        currency.setCurrencyType(currencyType);
        currency.setAmount(amount);
        currency.setVersion(0);
        return currency;
    }
}
