package com.pokergame.common.util;

import com.iohao.game.action.skeleton.core.BarMessageKit;
import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.action.skeleton.core.DataCodecKit;
import com.iohao.game.action.skeleton.core.commumication.BrokerClientContext;
import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.iohao.game.action.skeleton.protocol.RequestMessage;
import com.iohao.game.action.skeleton.protocol.ResponseMessage;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
@UtilityClass
public class RpcInvokeUtil {

    /**
     * 调用 RPC Action 并返回响应数据
     *
     * @param brokerClient Broker 客户端
     * @param cmdInfo      路由信息
     * @param req          请求参数
     * @param respClass    响应类型
     * @param <Req>        请求类型
     * @param <Resp>       响应类型
     * @return 响应数据
     */
    public static <Req, Resp> Resp invoke(
            BrokerClient brokerClient,
            CmdInfo cmdInfo,
            Req req,
            Class<Resp> respClass) {

        RequestMessage requestMessage = BarMessageKit.createRequestMessage(cmdInfo, req);
        ResponseMessage response = brokerClient.getInvokeModuleContext()
                .invokeModuleMessage(requestMessage);

        if (response.hasError()) {
            throw new MsgException(response.getResponseStatus(), response.getValidatorMsg());
        }

        return DataCodecKit.decode(response.getData(), respClass);
    }

    public static <Req, Resp> Resp invoke(BrokerClientContext ctx, CmdInfo cmdInfo, Req req, Class<Resp> respClass) {
        RequestMessage requestMessage = BarMessageKit.createRequestMessage(cmdInfo, req);
        ResponseMessage response = ctx.getInvokeModuleContext().invokeModuleMessage(requestMessage);
        if (response.hasError()) {
            throw new MsgException(response.getResponseStatus(), response.getValidatorMsg());
        }
        return DataCodecKit.decode(response.getData(), respClass);
    }

    /**
     * 异步调用 RPC Action（发送并忘记），不等待响应
     * 适用于非关键路径操作，如登录日志记录、风控更新等
     * 使用 CompletableFuture 实现异步，不阻塞主线程
     *
     * @param brokerClient Broker 客户端
     * @param cmdInfo      路由信息
     * @param req          请求参数
     */
    public static void invokeAsync(BrokerClient brokerClient, CmdInfo cmdInfo, Object req) {
        CompletableFuture.runAsync(() -> {
            try {
                RequestMessage requestMessage = BarMessageKit.createRequestMessage(cmdInfo, req);
                // 使用无返回值的方法发送请求
                brokerClient.getInvokeModuleContext().invokeModuleVoidMessage(requestMessage);
            } catch (Exception e) {
                log.warn("异步RPC调用失败: cmd={}, subCmd={}, error={}",
                        cmdInfo.getCmd(), cmdInfo.getSubCmd(), e.getMessage());
            }
        });
    }

    public static void invokeAsync(BrokerClientContext ctx, CmdInfo cmdInfo, Object req) {
        CompletableFuture.runAsync(() -> {
            try {
                RequestMessage requestMessage = BarMessageKit.createRequestMessage(cmdInfo, req);
                // 使用无返回值的方法发送请求
                ctx.getInvokeModuleContext().invokeModuleVoidMessage(requestMessage);
            } catch (Exception e) {
                log.warn("异步RPC调用失败: cmd={}, subCmd={}, error={}",
                        cmdInfo.getCmd(), cmdInfo.getSubCmd(), e.getMessage());
            }
        });
    }

}
