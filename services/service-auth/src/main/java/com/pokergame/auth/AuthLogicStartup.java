package com.pokergame.auth;

import com.iohao.game.action.skeleton.core.BarSkeleton;
import com.iohao.game.action.skeleton.core.BarSkeletonBuilder;
import com.iohao.game.action.skeleton.core.flow.internal.DebugInOut;
import com.iohao.game.action.skeleton.kit.LogicServerCreateKit;
import com.iohao.game.bolt.broker.client.AbstractBrokerClientStartup;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.iohao.game.bolt.broker.core.client.BrokerClientBuilder;
import com.pokergame.auth.action.AuthAction;
import com.pokergame.common.enums.LogicServer;

/**
 * 用户逻辑服
 * 
 * @author 游戏平台
 * @date 2024-03-26
 */
public class AuthLogicStartup extends AbstractBrokerClientStartup {
    
    @Override
    public BarSkeleton createBarSkeleton() {
        // 业务框架构建器
        BarSkeletonBuilder builder = LogicServerCreateKit.createBuilder(AuthAction.class);
        // 添加调试插件
        builder.addInOut(new DebugInOut());
        
        return builder.build();
    }

    @Override
    public BrokerClientBuilder createBrokerClientBuilder() {
        BrokerClientBuilder builder = BrokerClient.newBuilder();
        builder.id(LogicServer.SERVICE_AUTH.getId());
        builder.appName(LogicServer.SERVICE_AUTH.getName());
        builder.tag(LogicServer.SERVICE_AUTH.getTag());
        return builder;
    }
}