package com.pokergame.external.action;

import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.iohao.game.bolt.broker.core.aware.BrokerClientAware;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.pokergame.common.cmd.AuthCmd;
import com.pokergame.common.cmd.ModuleCmd;
import com.pokergame.common.cmd.WSCmd;
import com.pokergame.common.model.auth.VerifyTokenReq;
import com.pokergame.common.model.auth.VerifyTokenResp;
import com.pokergame.common.model.ws.WsAttachment;
import com.pokergame.common.util.RpcInvokeUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@ActionController(ModuleCmd.WS_CMD)
public class WsLoginAction implements BrokerClientAware {

    BrokerClient brokerClient;

    @ActionMethod(WSCmd.LOGIN)
    public void login(FlowContext flowContext) {
        // 获取附件中的 token
        WsAttachment attachment = flowContext.getAttachment(WsAttachment.class);
        if (attachment == null || attachment.getToken() == null) {
            log.warn("WebSocket 登录缺少 token 附件");
            return;
        }
        String token = attachment.getToken();

        // 2. 调用 Auth 服务验证 Token
        VerifyTokenReq req = new VerifyTokenReq();
        req.setToken(token);
        VerifyTokenResp resp;
        try {
            resp = RpcInvokeUtil.invoke(brokerClient,
                    CmdInfo.of(AuthCmd.VERIFY_TOKEN),
                    req,
                    VerifyTokenResp.class);
        } catch (Exception e) {
            log.error("验证 Token 失败: {}", e.getMessage());
            return;
        }

        if (resp != null && resp.getValid()) {
            // 3. 绑定 userId 到连接
            boolean success = flowContext.bindingUserId(resp.getUserId());
            if (success) {
                log.info("WebSocket 登录成功: userId={}", resp.getUserId());
            } else {
                log.warn("WebSocket 登录绑定 userId 失败: userId={}", resp.getUserId());
            }
        } else {
            log.warn("WebSocket Token 无效");
        }
    }
}
