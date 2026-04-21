package com.pokergame.external;

import com.iohao.game.action.skeleton.core.BarSkeleton;
import com.iohao.game.action.skeleton.core.BarSkeletonBuilder;
import com.iohao.game.action.skeleton.core.flow.internal.DebugInOut;
import com.iohao.game.action.skeleton.kit.LogicServerCreateKit;
import com.iohao.game.bolt.broker.client.AbstractBrokerClientStartup;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.iohao.game.bolt.broker.core.client.BrokerClientBuilder;
import com.pokergame.common.context.MyFlowContext;
import com.pokergame.common.enums.LogicServer;
import com.pokergame.external.action.WsLoginAction;

public class GameExternalStartup extends AbstractBrokerClientStartup {
    @Override
    public BarSkeleton createBarSkeleton() {
        // 业务框架构建器 配置
        BarSkeletonBuilder builder = LogicServerCreateKit.createBuilder(WsLoginAction.class);
        builder.setFlowContextFactory(MyFlowContext::new);
        // 添加控制台输出插件
        builder.addInOut(new DebugInOut());
        return builder.build();
    }

    @Override
    public BrokerClientBuilder createBrokerClientBuilder() {
        return BrokerClient.newBuilder()
                .id(LogicServer.SERVICE_WS.getId())
                .appName(LogicServer.SERVICE_WS.getName())
                .tag(LogicServer.SERVICE_WS.getTag());
    }

}