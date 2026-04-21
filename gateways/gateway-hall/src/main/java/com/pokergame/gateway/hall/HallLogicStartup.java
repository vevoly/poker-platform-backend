package com.pokergame.gateway.hall;

import com.iohao.game.action.skeleton.core.BarSkeleton;
import com.iohao.game.action.skeleton.core.BarSkeletonBuilder;
import com.iohao.game.action.skeleton.core.flow.internal.DebugInOut;
import com.iohao.game.bolt.broker.client.AbstractBrokerClientStartup;
import com.iohao.game.bolt.broker.core.client.BrokerAddress;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.iohao.game.bolt.broker.core.client.BrokerClientBuilder;
import com.iohao.game.bolt.broker.core.common.IoGameGlobalConfig;
import com.pokergame.common.context.MyFlowContext;
import com.pokergame.common.enums.LogicServer;

public class HallLogicStartup extends AbstractBrokerClientStartup {

    @Override
    public BarSkeleton createBarSkeleton() {
        // 业务框架构建器
        BarSkeletonBuilder builder = BarSkeleton.newBuilder();
        builder.setFlowContextFactory(MyFlowContext::new);
        // 添加调试插件
        builder.addInOut(new DebugInOut());
        return builder.build();
    }

    @Override
    public BrokerClientBuilder createBrokerClientBuilder() {
        BrokerClientBuilder builder = BrokerClient.newBuilder();
        builder.id(LogicServer.GATEWAY_HALL.getId());
        builder.appName(LogicServer.GATEWAY_HALL.getName());
        builder.tag(LogicServer.GATEWAY_HALL.getTag());
        // 显式指定 Broker 地址
//        builder.brokerAddress(new BrokerAddress("127.0.0.1", IoGameGlobalConfig.brokerPort));
        return builder;
    }
}
