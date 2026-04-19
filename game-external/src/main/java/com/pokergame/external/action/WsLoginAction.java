package com.pokergame.external.action;

import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.iohao.game.bolt.broker.client.kit.ExternalCommunicationKit;
import com.iohao.game.bolt.broker.core.client.BrokerClientHelper;
import com.pokergame.common.cmd.AuthCmd;
import com.pokergame.common.cmd.ModuleCmd;
import com.pokergame.common.cmd.WSCmd;
import com.pokergame.common.exception.GameCode;
import com.pokergame.common.model.auth.VerifyTokenReq;
import com.pokergame.common.model.auth.VerifyTokenResp;
import com.pokergame.common.model.ws.WsAttachment;
import com.pokergame.common.util.RpcInvokeUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@ActionController(ModuleCmd.WS_CMD)
public class WsLoginAction {

    @ActionMethod(WSCmd.LOGIN)
    public void login(FlowContext flowContext) {
        // 获取附件中的 token
        WsAttachment attachment = flowContext.getAttachment(WsAttachment.class);
        GameCode.PARAM_ERROR.assertTrueThrows(attachment == null || attachment.getToken() == null,
                "WebSocket 登录缺少 token 附件");
        String token = attachment.getToken();

        // 2. 调用 Auth 服务验证 Token
        VerifyTokenReq req = new VerifyTokenReq();
        req.setToken(token);
        VerifyTokenResp resp;
        resp = RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(), CmdInfo.of(AuthCmd.CMD, AuthCmd.VERIFY_TOKEN), req, VerifyTokenResp.class);

        // 3. 断言 Token 有效
        GameCode.TOKEN_INVALID.assertTrueThrows(resp == null || !resp.getValid());

        // 4. 禁止重复登录（检查是否已有在线连接）
        GameCode.ACCOUNT_ONLINE.assertTrueThrows(ExternalCommunicationKit.existUser(resp.getUserId()));

        // 5. 绑定 userId 到连接
        boolean success = flowContext.bindingUserId(resp.getUserId());
        GameCode.LOGIN_FAILED.assertTrueThrows(!success, "绑定 userId 失败");

        log.info("WebSocket 登录成功: userId={}", resp.getUserId());
    }
}
