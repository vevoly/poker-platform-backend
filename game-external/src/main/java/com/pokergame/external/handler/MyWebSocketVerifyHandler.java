package com.pokergame.external.handler;

import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.action.skeleton.core.DataCodecKit;
import com.iohao.game.action.skeleton.protocol.RequestMessage;
import com.iohao.game.bolt.broker.core.aware.BrokerClientAware;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.iohao.game.external.core.netty.handler.ws.WebSocketVerifyHandler;
import com.iohao.game.external.core.netty.session.SocketUserSession;
import com.iohao.game.external.core.session.UserSessionOption;
import com.pokergame.common.cmd.main.MainCmd;
import com.pokergame.common.cmd.WSCmd;
import com.pokergame.common.constants.AuthConstants;
import com.pokergame.common.model.ws.WsAttachment;
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

        // 构造附件对象并编码存入
        WsAttachment attachment = new WsAttachment();
        attachment.setToken(token);
        byte[] encode = DataCodecKit.encode(attachment);
        userSession.option(UserSessionOption.attachment, encode);

        // 发送登录请求到业务 Action（异步，不等待响应）
        CmdInfo cmdInfo = CmdInfo.of(MainCmd.WS_CMD, WSCmd.LOGIN);
        RequestMessage requestMessage = userSession.ofRequestMessage(cmdInfo);
        try {
            brokerClient.oneway(requestMessage);
        } catch (Exception e) {
            log.error("发送 WebSocket 登录请求失败: {}", e.getMessage(), e);
            return false;
        }

        // 允许连接建立
        return true;
    }

}
