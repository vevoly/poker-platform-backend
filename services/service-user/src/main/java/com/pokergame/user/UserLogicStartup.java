package com.pokergame.user;

import com.iohao.game.action.skeleton.core.BarSkeleton;
import com.iohao.game.action.skeleton.core.BarSkeletonBuilder;
import com.iohao.game.action.skeleton.core.flow.internal.DebugInOut;
import com.iohao.game.bolt.broker.client.AbstractBrokerClientStartup;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.iohao.game.bolt.broker.core.client.BrokerClientBuilder;
import com.iohao.game.action.skeleton.kit.LogicServerCreateKit;
import com.pokergame.user.action.UserAction;

/**
 * 用户逻辑服
 * 
 * @author 游戏平台
 * @date 2024-03-26
 */
public class UserLogicStartup extends AbstractBrokerClientStartup {
    
    @Override
    public BarSkeleton createBarSkeleton() {
        // 业务框架构建器
        BarSkeletonBuilder builder = LogicServerCreateKit.createBuilder(UserAction.class);
        // 添加调试插件
        builder.addInOut(new DebugInOut());
        
        return builder.build();
    }

    @Override
    public BrokerClientBuilder createBrokerClientBuilder() {
        BrokerClientBuilder builder = BrokerClient.newBuilder();
        builder.id("user-logic-1");
        builder.appName("用户逻辑服");
        builder.tag("user-logic");
        return builder;
    }
}