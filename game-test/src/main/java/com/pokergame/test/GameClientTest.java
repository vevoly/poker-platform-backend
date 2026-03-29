package com.pokergame.test;

import com.iohao.game.external.client.join.ClientRunOne;
import com.iohao.game.external.client.user.ClientUser;
import com.iohao.game.external.client.user.DefaultClientUser;
import com.pokergame.test.user.UserInputCommandRegion;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * ioGame 模拟客户端测试启动类
 *
 * 启动后可在控制台输入命令：
 * - 输入 .       查看所有请求命令
 * - 输入 2-1  执行登录请求（主路由-子路由）
 * - 输入 ..      查看所有广播命令
 * - 输入 ...     查看所有请求和广播命令
 */
@Slf4j
public class GameClientTest {

    public static void main(String[] args) {
        // 可选：关闭模拟请求相关日志，减少控制台输出
        // ClientUserConfigs.closeLog();

        // 创建模拟玩家
        ClientUser clientUser = new DefaultClientUser();
        clientUser.setUserId(1001L);
        clientUser.setJwt("test-token");

        // 启动模拟客户端
        new ClientRunOne()
                .setInputCommandRegions(List.of(
                        new UserInputCommandRegion()
                        // 后续添加其他测试区域
                ))
                .setClientUser(clientUser)
                .setConnectAddress("127.0.0.1")
                .setConnectPort(10100)
                .startup();

        log.info("模拟客户端已启动，可在控制台输入命令进行测试");
    }
}