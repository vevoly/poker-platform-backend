package com.pokergame.broker;

import com.iohao.game.bolt.broker.core.common.IoGameGlobalConfig;
import com.iohao.game.bolt.broker.server.BrokerServer;
import com.iohao.game.bolt.broker.server.BrokerServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Broker网关启动类
 *
 * @author 游戏平台
 * @date 2024-03-26
 */
public class GameBrokerServer {

    public BrokerServer createBrokerServer() {
        // broker （游戏网关）默认端口 10200
        return createBrokerServer(IoGameGlobalConfig.brokerPort);
    }

    public BrokerServer createBrokerServer(int port) {
        // Broker Server （游戏网关服） 构建器
        BrokerServerBuilder brokerServerBuilder = BrokerServer.newBuilder()
                // broker （游戏网关）端口
                .port(port);

        // 构建游戏网关
        return brokerServerBuilder.build();
    }

    public static void main(String[] args) throws InterruptedException {

        BrokerServer brokerServer = new GameBrokerServer().createBrokerServer();
        // 启动 游戏网关
        brokerServer.startup();

        TimeUnit.SECONDS.sleep(1);
    }

}