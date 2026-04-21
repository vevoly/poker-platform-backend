package com.pokergame.user;

import com.iohao.game.action.skeleton.core.BarSkeleton;
import com.iohao.game.action.skeleton.core.BarSkeletonBuilder;
import com.iohao.game.action.skeleton.core.flow.internal.DebugInOut;
import com.iohao.game.action.skeleton.ext.spring.ActionFactoryBeanForSpring;
import com.iohao.game.bolt.broker.client.AbstractBrokerClientStartup;
import com.iohao.game.bolt.broker.core.client.BrokerAddress;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.iohao.game.bolt.broker.core.client.BrokerClientBuilder;
import com.iohao.game.action.skeleton.kit.LogicServerCreateKit;
import com.iohao.game.bolt.broker.core.common.IoGameGlobalConfig;
import com.pokergame.common.context.MyFlowContext;
import com.pokergame.common.enums.LogicServer;
import com.pokergame.user.action.UserAction;
import org.springframework.context.annotation.Bean;

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
        builder.setFlowContextFactory(MyFlowContext::new);
        // 添加调试插件
        builder.addInOut(new DebugInOut());
        
        return builder.build();
    }

    @Override
    public BrokerClientBuilder createBrokerClientBuilder() {
        BrokerClientBuilder builder = BrokerClient.newBuilder();
        builder.id(LogicServer.SERVICE_USER.getId());
        builder.appName(LogicServer.SERVICE_USER.getName());
        builder.tag(LogicServer.SERVICE_USER.getTag());
        return builder;
    }

}