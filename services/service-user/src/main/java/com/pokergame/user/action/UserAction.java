package com.pokergame.user.action;

import cn.hutool.core.util.StrUtil;
import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.pokergame.common.cmd.UserCmd;
import com.pokergame.common.exception.GameCode;
import com.pokergame.common.model.auth.*;
import com.pokergame.common.model.user.*;
import com.pokergame.common.util.ValidationUtils;
import com.pokergame.user.converter.UserConverter;
import com.pokergame.user.entity.UserEntity;
import com.pokergame.user.mapper.UserMapper;
import com.pokergame.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Normalized;
import org.springframework.beans.factory.annotation.Autowired;
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
@AllArgsConstructor
@ActionController(UserCmd.CMD)
public class UserAction {

    private final UserService userService;

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
     * 获取用户信息
     */
    @ActionMethod(UserCmd.GET_USER_INFO)
    public GetUserInfoResp getUserInfo(GetUserInfoReq req, FlowContext flowContext) throws MsgException {
        req.setUserId(flowContext.getUserId());
        log.info("RPC 获取用户信息: userId={}", req.getUserId());
        ValidationUtils.validate(req);
        UserEntity user = userService.checkAndGetUser(req.getUserId());
        GetUserInfoResp resp = new GetUserInfoResp();
        resp.setUser(userConverter.toDTO(user));

        return resp;
    }

    /**
     * 验证用户凭证（供 Auth 服务调用）
     * 仅验证用户名/手机/邮箱和密码，不生成 Token，不更新最后登录时间
     */
    @ActionMethod(UserCmd.VERIFY_CREDENTIAL)
    public VerifyUserCredentialResp verifyCredential(VerifyUserCredentialReq req) {
        log.info("RPC 验证用户凭证: username={}, mobile={}, email={}",
                req.getUsername(), req.getMobile(), req.getEmail());

        // 构造 LoginReq 用于 Service 层调用
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername(req.getUsername());
        loginReq.setMobile(req.getMobile());
        loginReq.setEmail(req.getEmail());
        loginReq.setPassword(req.getPassword());

        UserEntity user = userService.verifyCredential(loginReq);

        VerifyUserCredentialResp resp = new VerifyUserCredentialResp();
        resp.setValid(true);
        resp.setUserId(user.getId());
        resp.setUserCode(user.getUserCode());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setStatus(user.getStatus());

        log.info("RPC 验证用户凭证成功: userId={}", user.getId());
        return resp;
    }

    @ActionMethod(UserCmd.PROCESS_LOGIN_SUCCESS)
    public void processLoginSuccess(ProcessLoginSuccessReq req) {
        log.info("RPC 处理登录成功后续: userId={}", req.getUserId());
        // 构造 LoginReq
        LoginReq loginReq = new LoginReq();
        loginReq.setLoginIp(req.getLoginIp());
        loginReq.setLoginDeviceId(req.getLoginDeviceId());
        loginReq.setLoginUserAgent(req.getLoginUserAgent());
        loginReq.setLoginLatitude(req.getLoginLatitude());
        loginReq.setLoginLongitude(req.getLoginLongitude());
        userService.processLoginSuccess(req.getUserId(), loginReq);
    }

}
