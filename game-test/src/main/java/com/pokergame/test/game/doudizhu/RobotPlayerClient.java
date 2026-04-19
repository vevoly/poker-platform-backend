package com.pokergame.test.game.doudizhu;

import com.iohao.game.external.client.join.ClientRunOne;
import com.iohao.game.external.client.user.ClientUser;
import com.iohao.game.external.client.user.DefaultClientUser;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 机器人玩家模拟客户端
 * 由测试人员根据机器人服务日志的提示手动输入命令
 * （实际游戏中机器人应由机器人服务自动控制，这里为了测试事件订阅，先手动模拟）
 */
@Slf4j
public class RobotPlayerClient {

    public static void main(String[] args) {
        long userId = 1002;  // 修改为数据库中的另一个真人玩家ID
        ClientUser clientUser = new DefaultClientUser();
        clientUser.setUserId(userId);
        clientUser.setJwt("test-token-" + userId);

        new ClientRunOne()
                .setInputCommandRegions(List.of(new DoudizhuInputCommandRegion()))
                .setClientUser(clientUser)
                .setConnectAddress("127.0.0.1")
                .setConnectPort(10100)
                .startup();

        log.info("机器人玩家客户端已启动，请按照机器人服务日志的回合提示输入命令");
        log.info("例如：当机器人服务打印'轮到机器人玩家'时，在机器人客户端输入 101-13 出牌或 101-14 过牌");
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
