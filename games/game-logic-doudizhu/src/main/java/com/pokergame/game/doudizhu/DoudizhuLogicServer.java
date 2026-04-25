package com.pokergame.game.doudizhu;

import com.iohao.game.action.skeleton.core.BarSkeleton;
import com.iohao.game.action.skeleton.core.BarSkeletonBuilderParamConfig;
import com.iohao.game.action.skeleton.core.flow.internal.DebugInOut;
import com.iohao.game.action.skeleton.eventbus.EventBus;
import com.iohao.game.action.skeleton.eventbus.EventBusRunner;
import com.iohao.game.bolt.broker.client.AbstractBrokerClientStartup;
import com.iohao.game.bolt.broker.client.BrokerClientApplication;
import com.iohao.game.bolt.broker.core.client.BrokerAddress;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.iohao.game.bolt.broker.core.client.BrokerClientBuilder;
import com.iohao.game.bolt.broker.core.common.IoGameGlobalConfig;
import com.iohao.game.common.kit.NetworkKit;
import com.pokergame.common.deal.pool.HandRankPoolManager;
import com.pokergame.common.enums.LogicServer;
import com.pokergame.common.context.MyFlowContext;
import com.pokergame.common.game.GameType;
import com.pokergame.game.doudizhu.action.RoomAction;
import lombok.extern.slf4j.Slf4j;

/**
 * 斗地主逻辑服启动类
 *
 * 继承 AbstractBrokerClientStartup，实现 ioGame 逻辑服的标准启动方式
 *
 * 启动流程：
 * 1. 扫描 Action 类
 * 2. 添加插件（DebugInOut 用于调试）
 * 3. 注册操作配置 Runner
 * 4. 配置 BrokerClient
 * 5. 连接到 Broker
 *
 * @author poker-platform
 */
@Slf4j
public class DoudizhuLogicServer extends AbstractBrokerClientStartup {

    public static void main(String[] args) {
        BrokerClientApplication.start(new DoudizhuLogicServer());
        // 初始化牌型池
        HandRankPoolManager.getInstance().ensureInitialized(GameType.DOUDIZHU);
    }

    @Override
    public BarSkeleton createBarSkeleton() {
        // 1. 配置 Action 扫描路径
        var config = new BarSkeletonBuilderParamConfig()
                // 扫描类所在的包
                .scanActionPackage(RoomAction.class)
                // 开启广播日志
                .setBroadcastLog(true);

        // 2. 创建构建器
        var builder = config.createBuilder();

        // 3. 添加调试插件（开发环境使用，生产环境可移除）
        builder.addInOut(new DebugInOut());

        // 4. 注册操作配置 Runner
//        builder.addRunner(new DoudizhuOperationConfigRunner());

        // 5. 设置自定义 FlowContext
        builder.setFlowContextFactory(MyFlowContext::new);

        // 6. 添加 EventBusRunner，以开启分布式事件总线功能
        // 即使当前逻辑服没有任何订阅者，只是向外发送事件，也必须添加。
        builder.addRunner(new EventBusRunner() {
            @Override
            public void registerEventBus(EventBus eventBus, BarSkeleton skeleton) {
                log.info("斗地主逻辑服的事件总线已启动");
            }
        });

        return builder.build();
    }

    @Override
    public BrokerClientBuilder createBrokerClientBuilder() {
        BrokerClientBuilder builder = BrokerClient.newBuilder();
        builder.appName(LogicServer.GAME_DOUDIZHU.getName());
        return builder;
    }

    @Override
    public BrokerAddress createBrokerAddress() {
        return new BrokerAddress(NetworkKit.LOCAL_IP, IoGameGlobalConfig.brokerPort);
    }

}
