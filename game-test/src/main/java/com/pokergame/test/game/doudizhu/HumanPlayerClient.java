package com.pokergame.test.game.doudizhu;

import com.iohao.game.external.client.join.ClientRunOne;
import com.iohao.game.external.client.user.ClientUser;
import com.iohao.game.external.client.user.DefaultClientUser;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 真人玩家模拟客户端
 * 手动输入命令进行游戏
 */
@Slf4j
public class HumanPlayerClient {

    public static void main(String[] args) {
        long userId = 1001;  // 修改为数据库中的真人玩家ID
        ClientUser clientUser = new DefaultClientUser();
        clientUser.setUserId(userId);
        clientUser.setJwt("test-token-" + userId);

        new ClientRunOne()
                .setInputCommandRegions(List.of(new DoudizhuInputCommandRegion()))
                .setClientUser(clientUser)
                .setConnectAddress("127.0.0.1")
                .setConnectPort(10100)   // 对外服端口
                .startup();

        log.info("人类玩家客户端已启动，可在控制台输入命令进行操作");
        log.info("命令示例: 101-1 创建房间, 101-2 加入房间, 101-10 准备, 101-11 开始游戏, 101-12 抢地主, 101-13 出牌, 101-14 过牌");
        // 保持主线程运行
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
