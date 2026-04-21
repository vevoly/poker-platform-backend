package com.pokergame.external.action;

import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.bolt.broker.client.kit.ExternalCommunicationKit;
import com.iohao.game.bolt.broker.core.client.BrokerClientHelper;
import com.pokergame.common.cmd.AuthCmd;
import com.pokergame.common.cmd.main.MainCmd;
import com.pokergame.common.cmd.UserCmd;
import com.pokergame.common.cmd.WSCmd;
import com.pokergame.common.context.MyAttachment;
import com.pokergame.common.context.MyFlowContext;
import com.pokergame.common.exception.GameCode;
import com.pokergame.common.model.auth.VerifyTokenReq;
import com.pokergame.common.model.auth.VerifyTokenResp;
import com.pokergame.common.model.user.GetUserBasicInfoReq;
import com.pokergame.common.model.user.GetUserBasicInfoResp;
import com.pokergame.common.model.ws.WsAttachment;
import com.pokergame.common.util.RpcInvokeUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@ActionController(MainCmd.WS_CMD)
public class WsLoginAction {

    @ActionMethod(WSCmd.LOGIN)
    public void login(MyFlowContext flowContext) {
        // 获取附件中的 token
        WsAttachment attachment = flowContext.getAttachment(WsAttachment.class);
        GameCode.PARAM_ERROR.assertTrueThrows(attachment == null || attachment.getToken() == null,
                "WebSocket 登录缺少 token 附件");
        String token = attachment.getToken();

        // 2. 调用 Auth 服务验证 Token
        VerifyTokenReq req = new VerifyTokenReq();
        req.setToken(token);
        VerifyTokenResp verifyResp = RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(AuthCmd.CMD, AuthCmd.VERIFY_TOKEN), req, VerifyTokenResp.class);

        // 3. 断言 Token 有效
        GameCode.TOKEN_INVALID.assertTrueThrows(verifyResp == null || !verifyResp.getValid());

        // 4. 禁止重复登录（检查是否已有在线连接）
        GameCode.ACCOUNT_ONLINE.assertTrueThrows(ExternalCommunicationKit.existUser(verifyResp.getUserId()));

        // 调用 UserService 获取用户信息
        GetUserBasicInfoReq userReq = new GetUserBasicInfoReq();
        userReq.setUserId(verifyResp.getUserId());
        GetUserBasicInfoResp userResp = RpcInvokeUtil.invoke(
                BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(UserCmd.CMD, UserCmd.GET_USER_BASIC_INFO),
                userReq,
                GetUserBasicInfoResp.class
        );
        GameCode.USER_NOT_FOUND.assertTrueThrows(userResp == null);

        // 6. 设置用户附加信息
        MyAttachment myAttachment = new MyAttachment()
                .setUserId(verifyResp.getUserId())
                .setNickname(userResp.getNickname())
                .setAvatar(userResp.getAvatar());
        flowContext.updateUserInfo(myAttachment); // 同步更新

        // 5. 绑定 userId 到连接
        boolean success = flowContext.bindingUserId(verifyResp.getUserId());
        GameCode.LOGIN_FAILED.assertTrueThrows(!success, "绑定 userId 失败");

        log.info("WebSocket 登录成功: userId={}", verifyResp.getUserId());
    }
}
