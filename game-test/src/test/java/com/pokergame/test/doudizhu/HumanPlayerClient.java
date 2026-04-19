package com.pokergame.test.doudizhu;

import com.iohao.game.external.client.join.ClientRunOne;
import com.iohao.game.external.client.user.ClientUser;
import com.iohao.game.external.client.user.DefaultClientUser;
import com.pokergame.test.util.LoginUtil;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class HumanPlayerClient {

    public static void main(String[] args) {
        // 1. HTTP 登录获取 token 和 userId
        String username = "testuser1";
        String password = "123456";
        LoginUtil.LoginResult loginResult = LoginUtil.login(username, password);
        if (loginResult == null) {
            log.error("登录失败，退出");
            return;
        }

        String token = loginResult.getToken();
        Long userId = loginResult.getUserId();

        // 2. 构建 ClientUser
        ClientUser clientUser = new DefaultClientUser();
        clientUser.setUserId(userId);
        clientUser.setJwt(token);  // jwt 可能用于业务，但 WebSocket 登录会单独使用 token

        // 3. 启动模拟客户端，传入带 token 的命令区域
        new ClientRunOne()
                .setWebsocketVerify("?token=" + token)
                .setInputCommandRegions(List.of(new DoudizhuInputCommandRegion(token)))
                .setClientUser(clientUser)
                .setConnectAddress("127.0.0.1")
                .setConnectPort(10100)
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