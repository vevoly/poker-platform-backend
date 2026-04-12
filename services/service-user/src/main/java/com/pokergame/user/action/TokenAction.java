package com.pokergame.user.action;

import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.pokergame.common.cmd.TokenCmd;
import com.pokergame.common.exception.GameCode;
import com.pokergame.common.model.user.*;
import com.pokergame.common.util.ValidationUtils;
import com.pokergame.user.config.TokenService;
import com.pokergame.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Token 模块 RPC Action
 * 负责 Token 刷新、验证、踢人下线等操作
 *
 * @author poker-platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ActionController(TokenCmd.CMD)
public class TokenAction {

    private final TokenService tokenService;
    private final UserService userService;

    /**
     * 刷新 Token
     * <p>如果 Token 即将过期，生成新 Token；否则延长原 Token 有效期
     *
     * @param req 刷新请求（包含原 Token）
     * @return 刷新结果（包含新 Token 或原 Token）
     */
    @ActionMethod(TokenCmd.REFRESH)
    public RefreshTokenResp refresh(RefreshTokenReq req) {
        log.info("RPC 刷新 Token: token={}", req.getToken());
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
     * 验证 Token 有效性
     *
     * @param req 验证请求（包含 Token）
     * @return 验证结果（用户ID，是否有效）
     */
    @ActionMethod(TokenCmd.VERIFY)
    public VerifyTokenResp verify(VerifyTokenReq req) {
        log.info("RPC 验证 Token: token={}", req.getToken());
        ValidationUtils.validate(req);

        Long userId = tokenService.verifyToken(req.getToken());
        VerifyTokenResp resp = new VerifyTokenResp();
        resp.setUserId(userId);
        resp.setValid(userId != null);
        return resp;
    }

    /**
     * 强制踢用户下线（管理员或系统调用）
     *
     * @param req 踢人请求（包含用户ID）
     * @return 操作结果
     */
    @ActionMethod(TokenCmd.KICK)
    public KickUserResp kick(KickUserReq req) {
        log.info("RPC 强制踢用户下线: userId={}", req.getUserId());
        ValidationUtils.validate(req);

        // 校验用户是否存在
        userService.checkAndGetUser(req.getUserId());
        tokenService.kickUser(req.getUserId());

        return new KickUserResp();
    }
}
