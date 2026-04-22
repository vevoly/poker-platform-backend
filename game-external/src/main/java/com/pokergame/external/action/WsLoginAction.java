package com.pokergame.external.action;

import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.action.skeleton.core.DataCodecKit;
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
import com.pokergame.common.util.RpcInvokeUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@ActionController(MainCmd.WS_CMD)
public class WsLoginAction {

    @ActionMethod(WSCmd.LOGIN)
    public void login(MyFlowContext flowContext) {
        // 获取附件
        MyAttachment attachment = flowContext.getAttachment(MyAttachment.class);
        GameCode.LOGIN_FAILED.assertTrueThrows(!flowContext.bindingUserId(attachment.getUserId()), "绑定 userId 失败");
        flowContext.updateUserInfo(attachment); // 同步更新
        log.info("WebSocket 登录成功: userId={}, nickname={}", flowContext.getUserId(), flowContext.getNickname());
    }
}
