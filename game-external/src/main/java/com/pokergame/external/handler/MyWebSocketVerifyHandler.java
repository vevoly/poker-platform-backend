package com.pokergame.external.handler;

import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.action.skeleton.core.DataCodecKit;
import com.iohao.game.action.skeleton.protocol.RequestMessage;
import com.iohao.game.bolt.broker.client.kit.ExternalCommunicationKit;
import com.iohao.game.bolt.broker.core.aware.BrokerClientAware;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.iohao.game.bolt.broker.core.client.BrokerClientHelper;
import com.iohao.game.external.core.netty.handler.ws.WebSocketVerifyHandler;
import com.iohao.game.external.core.netty.session.SocketUserSession;
import com.iohao.game.external.core.session.UserSessionOption;
import com.pokergame.common.cmd.AuthCmd;
import com.pokergame.common.cmd.main.MainCmd;
import com.pokergame.common.cmd.WSCmd;
import com.pokergame.common.constants.AuthConstants;
import com.pokergame.common.constants.MetadataKeys;
import com.pokergame.common.context.MyAttachment;
import com.pokergame.common.exception.GameCode;
import com.pokergame.common.model.auth.VerifyTokenReq;
import com.pokergame.common.model.auth.VerifyTokenResp;
import com.pokergame.common.util.RpcInvokeUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * WebSocket 连接验证器
 * 在握手阶段提取 Token 并调用 Auth 服务验证
 *
 * @author poker-platform
 */
@Slf4j
@Setter
public class MyWebSocketVerifyHandler extends WebSocketVerifyHandler implements BrokerClientAware {

    BrokerClient brokerClient;

    @Override
    public boolean verify(SocketUserSession userSession, Map<String, String> params) {
        String token = params.get(AuthConstants.TOKEN_PARAM);
        if (token == null || token.isEmpty()) {
            log.warn("WebSocket 握手缺少 token 参数");
            return false;
        }

        // 1. 调用 Auth 服务验证 Token
        VerifyTokenReq req = new VerifyTokenReq();
        req.setToken(token);
        VerifyTokenResp verifyResp = RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(AuthCmd.CMD, AuthCmd.VERIFY_TOKEN), req, VerifyTokenResp.class);
        try {
            // 断言 Token 有效
            GameCode.TOKEN_INVALID.assertTrueThrows(verifyResp == null || !verifyResp.getValid());

            // 禁止重复登录（检查是否已有在线连接）
            GameCode.ACCOUNT_ONLINE.assertTrueThrows(ExternalCommunicationKit.existUser(verifyResp.getUserId()));

            // 2. 设置用户附加信息
            MyAttachment attachment = new MyAttachment()
                    .setUserId(verifyResp.getUserId())
                    .setNickname(params.get(MetadataKeys.NICKNAME))
                    .setAvatar(params.get(MetadataKeys.AVATAR));

            byte[] encode = DataCodecKit.encode(attachment);
            userSession.option(UserSessionOption.attachment, encode);

            CmdInfo cmdInfo = CmdInfo.of(MainCmd.WS_CMD, WSCmd.LOGIN);
            RequestMessage requestMessage = userSession.ofRequestMessage(cmdInfo);
            brokerClient.oneway(requestMessage);
        } catch (Exception e) {
            log.error("发送 WebSocket 登录请求失败: {}", e.getMessage(), e);
            return false;
        }

        // 返回 true 表示验证通过，返回 false 框架会关闭连接。
        return verifyResp.getValid();
    }

}
