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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * RPC 调用工具类
 * <p>提供同步和异步两种调用方式，支持传入 {@link BrokerClient} 或 {@link BrokerClientContext}。
 * <p><b>注意：</b>同步调用会检查响应中的业务错误，并抛出 {@link MsgException}。
 *
 * @author poker-platform
 */
@Slf4j
@UtilityClass
public class RpcInvokeUtil {

    // ==================== 基于 BrokerClient 的同步调用 ====================

    /**
     * 同步调用 RPC Action，返回响应数据
     *
     * @param brokerClient Broker 客户端（通常通过 {@link BrokerClientHelper#getBrokerClient()} 获取）
     * @param cmdInfo      路由信息
     * @param req          请求参数
     * @param respClass    响应类型
     * @param <Req>        请求类型
     * @param <Resp>       响应类型
     * @return 解码后的响应数据
     * @throws MsgException RPC 调用失败或业务错误时抛出
     */
    public static <Req, Resp> Resp invoke(BrokerClient brokerClient, CmdInfo cmdInfo, Req req, Class<Resp> respClass) {
        return invoke(brokerClient, cmdInfo, req, null, respClass);
    }

    /**
     * 同步调用 RPC Action（带 userId 元数据），返回响应数据
     *
     * @param brokerClient Broker 客户端
     * @param cmdInfo      路由信息
     * @param req          请求参数
     * @param userId       用户ID（设置到元数据中，可为 null）
     * @param respClass    响应类型
     * @param <Req>        请求类型
     * @param <Resp>       响应类型
     * @return 解码后的响应数据
     * @throws MsgException RPC 调用失败或业务错误时抛出
     */
    public static <Req, Resp> Resp invoke(BrokerClient brokerClient, CmdInfo cmdInfo, Req req, Long userId, Class<Resp> respClass) {
        RequestMessage requestMessage = BarMessageKit.createRequestMessage(cmdInfo, req);
        if (userId != null) {
            requestMessage.getHeadMetadata().setUserId(userId);
        }
        ResponseMessage response = brokerClient.getInvokeModuleContext().invokeModuleMessage(requestMessage);
        if (response.hasError()) {
            throw new MsgException(response.getResponseStatus(), response.getValidatorMsg());
        }
        return DataCodecKit.decode(response.getData(), respClass);
    }

    // ==================== 基于 BrokerClientContext 的同步调用（适用于 Action 内部等场景） ====================

    /**
     * 同步调用 RPC Action（基于 BrokerClientContext），返回响应数据
     * <p>此方法适用于 Action 内部通过 {@code FlowContext} 获取的 {@code BrokerClientContext}。
     *
     * @param ctx        BrokerClientContext 实例（如 {@code FlowContext.getBrokerClientContext()}）
     * @param cmdInfo    路由信息
     * @param req        请求参数
     * @param respClass  响应类型
     * @param <Req>      请求类型
     * @param <Resp>     响应类型
     * @return 解码后的响应数据
     * @throws MsgException RPC 调用失败或业务错误时抛出
     */
    public static <Req, Resp> Resp invoke(BrokerClientContext ctx, CmdInfo cmdInfo, Req req, Class<Resp> respClass) {
        return invoke(ctx, cmdInfo, req, null, respClass);
    }

    /**
     * 同步调用 RPC Action（带 userId 元数据，基于 BrokerClientContext），返回响应数据
     *
     * @param ctx        BrokerClientContext 实例
     * @param cmdInfo    路由信息
     * @param req        请求参数
     * @param userId     用户ID（设置到元数据中，可为 null）
     * @param respClass  响应类型
     * @param <Req>      请求类型
     * @param <Resp>     响应类型
     * @return 解码后的响应数据
     * @throws MsgException RPC 调用失败或业务错误时抛出
     */
    public static <Req, Resp> Resp invoke(BrokerClientContext ctx, CmdInfo cmdInfo, Req req, Long userId, Class<Resp> respClass) {
        RequestMessage requestMessage = Objects.isNull(req) ? BarMessageKit.createRequestMessage(cmdInfo) : BarMessageKit.createRequestMessage(cmdInfo, req);
        if (userId != null) {
            requestMessage.getHeadMetadata().setUserId(userId);
        }
        ResponseMessage response = ctx.getInvokeModuleContext().invokeModuleMessage(requestMessage);
        if (response.hasError()) {
            throw new MsgException(response.getResponseStatus(), response.getValidatorMsg());
        }
        return DataCodecKit.decode(response.getData(), respClass);
    }

    // ==================== 异步调用（无返回值） ====================

    /**
     * 异步调用 RPC Action（发送并忘记），不等待响应
     * <p>适用于非关键路径操作，如登录日志记录、风控更新等。
     *
     * @param brokerClient Broker 客户端
     * @param cmdInfo      路由信息
     * @param req          请求参数
     */
    public static void invokeAsync(BrokerClient brokerClient, CmdInfo cmdInfo, Object req) {
        CompletableFuture.runAsync(() -> {
            try {
                RequestMessage requestMessage = Objects.isNull(req) ? BarMessageKit.createRequestMessage(cmdInfo) : BarMessageKit.createRequestMessage(cmdInfo, req);
                brokerClient.getInvokeModuleContext().invokeModuleVoidMessage(requestMessage);
            } catch (Exception e) {
                log.warn("异步 RPC 调用失败: cmd={}, subCmd={}, error={}",
                        cmdInfo.getCmd(), cmdInfo.getSubCmd(), e.getMessage());
            }
        });
    }

    /**
     * 异步调用 RPC Action（基于 BrokerClientContext），发送并忘记
     *
     * @param ctx    BrokerClientContext 实例
     * @param cmdInfo 路由信息
     * @param req     请求参数
     */
    public static void invokeAsync(BrokerClientContext ctx, CmdInfo cmdInfo, Object req) {
        CompletableFuture.runAsync(() -> {
            try {
                RequestMessage requestMessage = Objects.isNull(req) ? BarMessageKit.createRequestMessage(cmdInfo) : BarMessageKit.createRequestMessage(cmdInfo, req);
                ctx.getInvokeModuleContext().invokeModuleVoidMessage(requestMessage);
            } catch (Exception e) {
                log.warn("异步 RPC 调用失败: cmd={}, subCmd={}, error={}",
                        cmdInfo.getCmd(), cmdInfo.getSubCmd(), e.getMessage());
            }
        });
    }
}
