package com.pokergame.game.doudizhu;

import com.iohao.game.action.skeleton.core.BarSkeleton;
import com.iohao.game.action.skeleton.core.BarSkeletonBuilderParamConfig;
import com.iohao.game.action.skeleton.core.flow.internal.DebugInOut;
import com.iohao.game.bolt.broker.client.AbstractBrokerClientStartup;
import com.iohao.game.bolt.broker.core.client.BrokerAddress;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.iohao.game.bolt.broker.core.client.BrokerClientBuilder;
import com.iohao.game.bolt.broker.core.common.IoGameGlobalConfig;
import com.pokergame.game.doudizhu.action.RoomAction;
import com.pokergame.game.doudizhu.config.DoudizhuOperationConfigRunner;
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

    @Override
    public BarSkeleton createBarSkeleton() {
        // 1. 配置 Action 扫描路径
        var config = new BarSkeletonBuilderParamConfig()
                .scanActionPackage(RoomAction.class);

        // 2. 创建构建器
        var builder = config.createBuilder();

        // 3. 添加调试插件（开发环境使用，生产环境可移除）
        builder.addInOut(new DebugInOut());

        // 4. 注册操作配置 Runner
        builder.addRunner(new DoudizhuOperationConfigRunner());

        return builder.build();
    }

    @Override
    public BrokerClientBuilder createBrokerClientBuilder() {
        BrokerClientBuilder builder = BrokerClient.newBuilder();
        builder.appName("doudizhuLogicServer");
        return builder;
    }

    @Override
    public BrokerAddress createBrokerAddress() {
        return new BrokerAddress("127.0.0.1", IoGameGlobalConfig.brokerPort);
    }

    public static void main(String[] args) {
        log.info("========================================");
        log.info("斗地主逻辑服启动中...");
        log.info("========================================");

        // 启动逻辑服
        DoudizhuLogicServer server = new DoudizhuLogicServer();

        // 注意：实际启动需要通过 NettySimpleHelper 或一体化启动
        // 这里只是定义，实际启动在 AllInOneServer 或独立启动类中

        log.info("斗地主逻辑服初始化完成");
    }
}
