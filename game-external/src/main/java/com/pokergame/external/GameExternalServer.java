package com.pokergame.external;

import com.iohao.game.bolt.broker.core.client.BrokerAddress;
import com.iohao.game.bolt.broker.core.common.IoGameGlobalConfig;
import com.iohao.game.external.core.ExternalServer;
import com.iohao.game.external.core.config.ExternalGlobalConfig;
import com.iohao.game.external.core.config.ExternalJoinEnum;
import com.iohao.game.external.core.netty.DefaultExternalServer;
import com.iohao.game.external.core.netty.DefaultExternalServerBuilder;
import lombok.extern.slf4j.Slf4j;

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

        // 游戏对外服 - 构建器
        DefaultExternalServerBuilder builder = DefaultExternalServer.newBuilder(ExternalGlobalConfig.externalPort)
                // websocket 方式连接；如果不设置，默认也是这个配置
                .externalJoinEnum(ExternalJoinEnum.WEBSOCKET)
                // Broker （游戏网关）的连接地址；如果不设置，默认也是这个配置
                .brokerAddress(new BrokerAddress("127.0.0.1", IoGameGlobalConfig.brokerPort));

        // 构建游戏对外服
        ExternalServer externalServer = builder.build();

        // 启动对外服
        externalServer.startup();

        log.info("对外服启动完成！端口: {}", ExternalGlobalConfig.externalPort);
    }
}
