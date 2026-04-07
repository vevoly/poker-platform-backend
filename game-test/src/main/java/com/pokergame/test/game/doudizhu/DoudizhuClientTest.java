package com.pokergame.test.game.doudizhu;

import com.iohao.game.external.client.join.ClientRunOne;
import com.iohao.game.external.client.user.ClientUser;
import com.iohao.game.external.client.user.DefaultClientUser;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 斗地主客户端测试
 *
 * 启动后可在控制台输入命令进行测试
 *
 * 命令格式：
 * - 输入 .       查看所有请求命令
 * - 输入 101-1   创建房间（主路由101，子路由1）
 * - 输入 101-2   加入房间
 * - 输入 101-10  准备
 * - 输入 101-12  抢地主
 * - 输入 101-13  出牌
 * - 输入 101-14  过牌
 *
 * @author poker-platform
 */
@Slf4j
public class DoudizhuClientTest {

    public static void main(String[] args) {
        // 创建模拟玩家
        long userId = 1001;
        ClientUser clientUser = new DefaultClientUser();
        clientUser.setUserId(1001L);
        clientUser.setJwt("test-token-" + userId);

        // 启动模拟客户端
        new ClientRunOne()
                .setInputCommandRegions(List.of(
                        new DoudizhuInputCommandRegion()
                ))
                .setClientUser(clientUser)
                .setConnectAddress("127.0.0.1")
                .setConnectPort(10100)
                .startup();

        log.info("斗地主模拟客户端已启动，可在控制台输入命令进行测试");
        log.info("命令示例: 101-1 创建房间, 101-2 加入房间, 101-10 准备, 101-12 抢地主, 101-13 出牌");
    }
}
