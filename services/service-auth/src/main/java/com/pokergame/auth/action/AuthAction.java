package com.pokergame.auth.action;

import cn.hutool.core.util.StrUtil;
import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.pokergame.auth.service.TokenService;
import com.pokergame.common.cmd.AuthCmd;
import com.pokergame.common.cmd.UserCmd;
import com.pokergame.common.exception.GameCode;
import com.pokergame.common.model.auth.*;
import com.pokergame.common.model.auth.LoginReq;
import com.pokergame.common.model.auth.LoginResp;
import com.pokergame.common.model.user.ProcessLoginSuccessReq;
import com.pokergame.common.util.RpcInvokeUtil;
import com.pokergame.common.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 鉴权模块 RPC Action
 * 负责登录认证、Token 管理、登出等
 */
@Slf4j
@RequiredArgsConstructor
@ActionController(AuthCmd.CMD)
public class AuthAction {

    private final TokenService tokenService;
    private final BrokerClient brokerClient;

    /**
     * 用户名密码登录
     *
     * 流程：
     * 1. 接收登录请求
     * 2. RPC 调用用户服务验证用户名密码
     * 3. 验证通过后生成 Token
     * 4. 返回 Token 和用户信息
     */
    @ActionMethod(AuthCmd.PASSWORD_LOGIN)
    public LoginResp passwordLogin(LoginReq req) throws MsgException {
        log.info("RPC 密码登录请求: username={}", req.getUsername());

        // 1. 参数校验
        ValidationUtils.validate(req);
        boolean hasUsername = StrUtil.isNotBlank(req.getUsername());
        boolean hasMobile = StrUtil.isNotBlank(req.getMobile());
        boolean hasEmail = StrUtil.isNotBlank(req.getEmail());
        GameCode.PARAM_ERROR.assertTrueThrows(!hasUsername && !hasMobile && !hasEmail,
                "用户名、手机号、邮箱至少填写一项");

        // 2. RPC 调用用户服务验证用户凭证
        VerifyUserCredentialReq verifyReq = new VerifyUserCredentialReq();
        verifyReq.setUsername(req.getUsername());
        verifyReq.setMobile(req.getMobile());
        verifyReq.setEmail(req.getEmail());
        verifyReq.setPassword(req.getPassword());

        VerifyUserCredentialResp verifyResp = RpcInvokeUtil.invoke(
                brokerClient,
                CmdInfo.of(UserCmd.VERIFY_CREDENTIAL),
                verifyReq,
                VerifyUserCredentialResp.class
        );

        if (!verifyResp.getValid()) {
            GameCode.LOGIN_FAILED.assertTrueThrows(true);
        }

        // 3. 生成 Token
        String token = tokenService.createToken(verifyResp.getUserId());

        // 调用用户服务的 processLoginSuccess
        ProcessLoginSuccessReq processReq = new ProcessLoginSuccessReq();
        processReq.setUserId(verifyResp.getUserId());
        processReq.setLoginIp(req.getLoginIp());
        processReq.setLoginDeviceId(req.getLoginDeviceId());
        processReq.setLoginUserAgent(req.getLoginUserAgent());
        processReq.setLoginLatitude(req.getLoginLatitude());
        processReq.setLoginLongitude(req.getLoginLongitude());

        RpcInvokeUtil.invokeAsync(brokerClient,
                CmdInfo.of(UserCmd.PROCESS_LOGIN_SUCCESS),
                processReq);

        // 4. 构建响应
        LoginResp resp = new LoginResp();
        resp.setUserId(verifyResp.getUserId());
        resp.setUserCode(verifyResp.getUserCode());
        resp.setUsername(verifyResp.getUsername());
        resp.setNickname(verifyResp.getNickname());
        resp.setAvatar(verifyResp.getAvatar());
        resp.setStatus(verifyResp.getStatus());
        resp.setToken(token);
        resp.setTokenExpireTime(System.currentTimeMillis() + 24 * 3600 * 1000L);

        log.info("RPC 登录成功: userId={}", verifyResp.getUserId());
        return resp;
    }

    /**
     * 生成 Token（供用户服务内部调用）
     */
    @ActionMethod(AuthCmd.CREATE_TOKEN)
    public CreateTokenResp createToken(CreateTokenReq req) {
        log.info("RPC 生成 Token: userId={}", req.getUserId());
        String token = tokenService.createToken(req.getUserId());

        CreateTokenResp resp = new CreateTokenResp();
        resp.setToken(token);
        resp.setUserId(req.getUserId());
        resp.setExpireTime(System.currentTimeMillis() + 24 * 3600 * 1000L);
        return resp;
    }

    /**
     * 验证 Token（供网关调用）
     */
    @ActionMethod(AuthCmd.VERIFY_TOKEN)
    public VerifyTokenResp verifyToken(VerifyTokenReq req) {
        log.info("RPC 验证 Token: token={}", req.getToken());
        ValidationUtils.validate(req);

        Long userId = tokenService.verifyToken(req.getToken());
        VerifyTokenResp resp = new VerifyTokenResp();
        resp.setUserId(userId);
        resp.setValid(userId != null);
        return resp;
    }

    /**
     * 刷新 Token
     */
    @ActionMethod(AuthCmd.REFRESH_TOKEN)
    public RefreshTokenResp refreshToken(RefreshTokenReq req) {
        log.info("RPC 刷新 Token");
        ValidationUtils.validate(req);

        String newToken = tokenService.refreshToken(req.getToken());
        RefreshTokenResp resp = new RefreshTokenResp();
        if (newToken != null) {
            resp.setNewToken(newToken);
            resp.setExpireTime(System.currentTimeMillis() + 24 * 3600 * 1000L);
        } else {
            throw new MsgException(GameCode.REFRESH_TOKEN_FAILED.getCode(),
                    GameCode.REFRESH_TOKEN_FAILED.getMsg());
        }
        return resp;
    }

    /**
     * 登出
     */
    @ActionMethod(AuthCmd.LOGOUT)
    public LogoutResp logout(LogoutReq req) {
        log.info("RPC 登出请求");
        ValidationUtils.validate(req);
        tokenService.invalidateToken(req.getToken());
        return new LogoutResp();
    }

    /**
     * 踢用户下线
     */
    @ActionMethod(AuthCmd.KICK_USER)
    public KickUserResp kickUser(KickUserReq req) {
        log.info("RPC 踢用户下线: userId={}", req.getUserId());
        ValidationUtils.validate(req);
        tokenService.kickUser(req.getUserId());
        return new KickUserResp();
    }
}
