package com.pokergame.robot;

import com.iohao.game.action.skeleton.core.BarSkeleton;
import com.iohao.game.action.skeleton.core.BarSkeletonBuilder;
import com.iohao.game.action.skeleton.core.flow.internal.DebugInOut;
import com.iohao.game.action.skeleton.eventbus.EventBus;
import com.iohao.game.action.skeleton.eventbus.EventBusRunner;
import com.iohao.game.bolt.broker.client.AbstractBrokerClientStartup;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.iohao.game.bolt.broker.core.client.BrokerClientBuilder;
import com.pokergame.common.enums.LogicServer;
import com.pokergame.robot.event.RobotEventBusSubscriber;
import com.pokergame.robot.manager.RobotManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j

@Component
public class RobotLogicStartup extends AbstractBrokerClientStartup {

    @Autowired
    private RobotManager robotManager;

    @Override
    public BarSkeleton createBarSkeleton() {
        BarSkeletonBuilder builder = BarSkeleton.newBuilder();
        // 添加调试插件（开发环境）
        builder.addInOut(new DebugInOut());
        // 添加 EventBusRunner 以启用事件总线功能
        builder.addRunner(new EventBusRunner() {
            @Override
            public void registerEventBus(EventBus eventBus, BarSkeleton skeleton) {
                // 注册机器人事件订阅者
                eventBus.register(new RobotEventBusSubscriber());
                log.info("已注册机器人事件订阅者到事件总线");
            }
        });

        // 可在此添加其他 Runner（如定时任务、游戏业务处理器等）
        return builder.build();
    }

    @Override
    public BrokerClientBuilder createBrokerClientBuilder() {
        BrokerClientBuilder builder = BrokerClient.newBuilder();
        builder.id(LogicServer.SERVICE_ROBOT.getId());
        builder.appName(LogicServer.SERVICE_ROBOT.getName());
        builder.tag(LogicServer.SERVICE_ROBOT.getTag());
        return builder;
    }

    /**
     * 在 Spring 容器启动完成后执行
     * 加载机器人账号池
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("机器人逻辑服启动完成，开始加载机器人账号池...");
        robotManager.loadRobotAccounts();
        // 可选：后续可添加定时刷新任务，例如每5分钟刷新一次
        // scheduleRefresh();
    }

    // 示例：定时刷新（需配合 @Scheduled 或自定义线程池）
     @Scheduled(fixedDelay = 300000)
     public void scheduleRefresh() {
         robotManager.refresh();
     }
}
