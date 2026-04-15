package com.pokergame.external;

import com.iohao.game.bolt.broker.client.BrokerClientApplication;
import com.iohao.game.bolt.broker.core.client.BrokerAddress;
import com.iohao.game.bolt.broker.core.common.IoGameGlobalConfig;
import com.iohao.game.external.core.ExternalServer;
import com.iohao.game.external.core.config.ExternalGlobalConfig;
import com.iohao.game.external.core.config.ExternalJoinEnum;
import com.iohao.game.external.core.netty.DefaultExternalServer;
import com.iohao.game.external.core.netty.DefaultExternalServerBuilder;
import com.iohao.game.external.core.netty.handler.ws.WebSocketVerifyHandler;
import com.iohao.game.external.core.netty.micro.WebSocketMicroBootstrapFlow;
import com.iohao.game.external.core.netty.simple.NettyRunOne;
import com.pokergame.external.handler.MyWebSocketVerifyHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 游戏对外服 - 客户端 WebSocket 连接的唯一入口
 *
 * 职责：
 * 1. 管理客户端 WebSocket 长连接
 * 2. 协议编解码
 * 3. 转发请求到 Broker 网关
 * 4. 推送消息给客户端
 *
 * 启动方式：
 * 1. 直接运行 main 方法（需要先启动 Broker）
 * 2. 或者通过 AllInOneServer 一体化启动
 */
@Slf4j
public class GameExternalServer {

    public static void main(String[] args) {
        log.info("对外服启动中...");

        // 启动 WS 逻辑服 ，注意这里的顺序不能换，不然Action会注册不上
        BrokerClientApplication.start(new GameExternalStartup());
        // 启动游戏对外服
        ExternalServer externalServer = createExternalServer();
        externalServer.startup();

        log.info("对外服启动完成！端口: {}", ExternalGlobalConfig.externalPort);
    }

    static ExternalServer createExternalServer() {
        int port = ExternalGlobalConfig.externalPort;
        DefaultExternalServerBuilder builder = DefaultExternalServer.newBuilder(port)
                // 连接到已启动的 Broker（默认 127.0.0.1:10200）
                .brokerAddress(new BrokerAddress("127.0.0.1", IoGameGlobalConfig.brokerPort))
                .externalJoinEnum(ExternalJoinEnum.WEBSOCKET);

        // 设置 WebSocket 验证处理器
        builder.setting().setMicroBootstrapFlow(new WebSocketMicroBootstrapFlow() {
            @Override
            protected WebSocketVerifyHandler createVerifyHandler() {
                return new MyWebSocketVerifyHandler();
            }
        });

        return builder.build();
    }
}
