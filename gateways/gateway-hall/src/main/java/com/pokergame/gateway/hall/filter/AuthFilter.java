package com.pokergame.gateway.hall.filter;

import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.action.skeleton.core.commumication.BrokerClientContext;
import com.iohao.game.bolt.broker.core.aware.BrokerClientAware;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.iohao.game.bolt.broker.core.client.BrokerClientHelper;
import com.pokergame.common.cmd.AuthCmd;
import com.pokergame.common.constants.AuthConstants;
import com.pokergame.common.constants.MetadataKeys;
import com.pokergame.common.model.auth.VerifyTokenReq;
import com.pokergame.common.model.auth.VerifyTokenResp;
import com.pokergame.common.util.RpcInvokeUtil;
import com.pokergame.gateway.hall.config.HallConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 统一鉴权过滤器
 * 对所有需要登录的请求进行 Token 验证，并将 userId 存入 request 上下文
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private final HallConfig hallConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. 白名单放行
        if (hallConfig.getWhiteList().contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. 提取 Token
        String token = extractToken(request);
        if (token == null) {
            sendUnauthorized(response, "Missing token");
            return;
        }

        // 3. 调用 Auth 服务验证 Token
        VerifyTokenResp verifyResp;
        BrokerClientContext brokerClientContext = BrokerClientHelper.getBrokerClient();
        try {
            verifyResp = RpcInvokeUtil.invoke(
                    brokerClientContext,
                    CmdInfo.of(AuthCmd.CMD, AuthCmd.VERIFY_TOKEN),
                    new VerifyTokenReq().setToken(token),
                    VerifyTokenResp.class
            );
        } catch (Exception e) {
            log.error("调用 Auth 服务失败: {}", e.getMessage());
            sendUnauthorized(response, "Auth service error");
            return;
        }

        if (verifyResp == null || !verifyResp.getValid()) {
            log.warn("Token 无效: path={}", path);
            sendUnauthorized(response, "Invalid token");
            return;
        }

        // 4. 将 userId 存入请求上下文
        request.setAttribute(MetadataKeys.USER_ID, verifyResp.getUserId());

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AuthConstants.AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(AuthConstants.BEARER_PREFIX)) {
            return header.substring(7);
        }
        return request.getParameter(AuthConstants.TOKEN_PARAM);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setContentType(AuthConstants.CONTENT_TYPE_JSON);
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
    }
}
