package com.pokergame.user.action;

import cn.hutool.core.util.StrUtil;
import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.pokergame.common.cmd.UserCmd;
import com.pokergame.common.exception.GameCode;
import com.pokergame.common.model.user.*;
import com.pokergame.common.util.ValidationUtils;
import com.pokergame.user.config.TokenService;
import com.pokergame.user.converter.UserConverter;
import com.pokergame.user.entity.UserEntity;
import com.pokergame.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

/**
 * 用户 Action
 * 处理用户注册、登录、信息查询、登出等 RPC 请求
 *
 * @author 游戏平台
 * @date 2024-03-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ActionController(UserCmd.CMD)
public class UserAction {

    private final UserService userService;
    private final TokenService tokenService;
    private final UserConverter userConverter;

    /**
     * 用户注册
     */
    @ActionMethod(UserCmd.REGISTER)
    public RegisterResp register(RegisterReq req) throws MsgException {
        log.info("RPC 注册请求: username={}, mobile={}, email={}",
                req.getUsername(), req.getMobile(), req.getEmail());

        // 参数校验（声明式注解 + 统一工具类）
        ValidationUtils.validate(req);

        // 额外业务校验：至少提供一种账号标识
        boolean hasUsername = StrUtil.isNotBlank(req.getUsername());
        boolean hasMobile = StrUtil.isNotBlank(req.getMobile());
        boolean hasEmail = StrUtil.isNotBlank(req.getEmail());
        GameCode.PARAM_ERROR.assertTrueThrows(!hasUsername && !hasMobile && !hasEmail,
                "用户名、手机号、邮箱至少填写一项");

        Long userId = userService.register(req);
        UserEntity user = userService.getById(userId);

        RegisterResp resp = new RegisterResp();
        resp.setUserId(userId);
        resp.setUserCode(user.getUserCode());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());

        log.info("RPC 注册成功: userId={}", userId);
        return resp;
    }

    /**
     * 用户登录
     */
    @ActionMethod(UserCmd.LOGIN)
    public LoginResp login(LoginReq req) throws MsgException {
        log.info("RPC 登录请求: username={}, mobile={}, email={}",
                req.getUsername(), req.getMobile(), req.getEmail());

        // 参数校验
        ValidationUtils.validate(req);

        // 业务校验：至少提供一种账号标识
        boolean hasUsername = StrUtil.isNotBlank(req.getUsername());
        boolean hasMobile = StrUtil.isNotBlank(req.getMobile());
        boolean hasEmail = StrUtil.isNotBlank(req.getEmail());
        GameCode.PARAM_ERROR.assertTrueThrows(!hasUsername && !hasMobile && !hasEmail,
                "用户名、手机号、邮箱至少填写一项");

        UserEntity user = userService.login(req);
        String token = tokenService.createToken(user.getId());

        LoginResp resp = new LoginResp();
        resp.setUserId(user.getId());
        resp.setUserCode(user.getUserCode());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setStatus(user.getStatus());
        resp.setToken(token);
        resp.setTokenExpireTime(System.currentTimeMillis() + 24 * 3600 * 1000L);
        if (user.getLastLoginTime() != null) {
            resp.setLastLoginTime(user.getLastLoginTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli());
        }

        log.info("RPC 登录成功: userId={}", user.getId());
        return resp;
    }

    /**
     * 获取用户信息
     */
    @ActionMethod(UserCmd.GET_USER_INFO)
    public GetUserInfoResp getUserInfo(GetUserInfoReq req) throws MsgException {
        log.info("RPC 获取用户信息: userId={}", req.getUserId());

        ValidationUtils.validate(req);

        UserEntity user = userService.checkAndGetUser(req.getUserId());

        GetUserInfoResp resp = new GetUserInfoResp();
        resp.setUser(userConverter.toDTO(user));

        return resp;
    }

    /**
     * 用户登出
     */
    @ActionMethod(UserCmd.LOGOUT)
    public LogoutResp logout(LogoutReq req) throws MsgException {
        log.info("RPC 登出请求: userId={}", req.getUserId());

        ValidationUtils.validate(req);

        tokenService.invalidateToken(req.getToken());

        LogoutResp resp = new LogoutResp();
        log.info("RPC 登出成功: userId={}", req.getUserId());
        return resp;
    }
}
