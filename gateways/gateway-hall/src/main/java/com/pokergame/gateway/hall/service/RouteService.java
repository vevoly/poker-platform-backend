package com.pokergame.gateway.hall.service;

import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.iohao.game.bolt.broker.core.client.BrokerClientHelper;
import com.pokergame.common.exception.result.Result;
import com.pokergame.common.util.RpcInvokeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 路由服务
 * <p>提供两种调用方式：
 * <ul>
 *     <li>{@link #forwardForResult} - 返回包装后的 {@link Result}，适用于 HTTP Controller</li>
 *     <li>{@link #forwardForData} - 返回原始业务数据，适用于服务间聚合调用</li>
 * </ul>
 *
 * @author poker-platform
 */
@Slf4j
@Service
public class RouteService {

    /**
     * 转发请求，返回包装后的 Result（供 Controller 使用）
     */
    public <Req, Resp> Result<Resp> forwardForResult(int cmd, int subCmd, Req request, Class<Resp> respClass) {
        return forwardForResult(cmd, subCmd, request, null, respClass);
    }

    /**
     * 转发请求（带 userId），返回包装后的 Result（供 Controller 使用）
     */
    public <Req, Resp> Result<Resp> forwardForResult(int cmd, int subCmd, Req request, Long userId, Class<Resp> respClass) {
        log.debug("转发请求(返回Result): cmd={}, subCmd={}, userId={}", cmd, subCmd, userId);
        Resp data = invokeRpc(cmd, subCmd, request, userId, respClass);
        return Result.success(data);
    }

    /**
     * 转发请求，返回原始业务数据（供 AggregateService 等内部调用使用）
     */
    public <Req, Resp> Resp forwardForData(int cmd, int subCmd, Req request, Class<Resp> respClass) {
        return forwardForData(cmd, subCmd, request, null, respClass);
    }

    /**
     * 转发请求（带 userId），返回原始业务数据（供 AggregateService 等内部调用使用）
     */
    public <Req, Resp> Resp forwardForData(int cmd, int subCmd, Req request, Long userId, Class<Resp> respClass) {
        log.debug("转发请求(返回原始数据): cmd={}, subCmd={}, userId={}", cmd, subCmd, userId);
        return invokeRpc(cmd, subCmd, request, userId, respClass);
    }

    /**
     * 底层 RPC 调用，返回原始数据，失败抛异常
     */
    private <Req, Resp> Resp invokeRpc(int cmd, int subCmd, Req request, Long userId, Class<Resp> respClass) {
        if (userId == null) {
            return RpcInvokeUtil.invoke(
                    BrokerClientHelper.getBrokerClient(),
                    CmdInfo.of(cmd, subCmd),
                    request,
                    respClass
            );
        } else {
            return RpcInvokeUtil.invoke(
                    BrokerClientHelper.getBrokerClient(),
                    CmdInfo.of(cmd, subCmd),
                    request,
                    userId,
                    respClass
            );
        }
    }
}
