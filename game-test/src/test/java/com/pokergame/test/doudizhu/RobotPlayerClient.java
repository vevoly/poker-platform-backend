package com.pokergame.test.doudizhu;

import com.iohao.game.external.client.join.ClientRunOne;
import com.iohao.game.external.client.user.ClientUser;
import com.iohao.game.external.client.user.DefaultClientUser;
import com.pokergame.test.util.LoginUtil;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

//@Slf4j
//public class RobotPlayerClient {
//
//    public static void main(String[] args) {
//        String username = "testuser2";
//        String password = "123456";
//        LoginUtil.LoginResult loginResult = LoginUtil.login(username, password);
//        if (loginResult == null) {
//            log.error("登录失败，退出");
//            return;
//        }
//
//        String token = loginResult.getToken();
//        Long userId = loginResult.getUserId();
//
//        ClientUser clientUser = new DefaultClientUser();
//        clientUser.setUserId(userId);
//        clientUser.setJwt(token);
//
//        new ClientRunOne()
//                .setWebsocketVerify("?token=" + token)
//                .setInputCommandRegions(List.of(new DoudizhuInputCommandRegion(token)))
//                .setClientUser(clientUser)
//                .setConnectAddress("127.0.0.1")
//                .setConnectPort(10100)
//                .startup();
//
//        log.info("机器人玩家客户端已启动，请按照机器人服务日志的回合提示输入命令");
//        log.info("例如：当机器人服务打印'轮到机器人玩家'时，在机器人客户端输入 101-13 出牌或 101-14 过牌");
//
//        try {
//            Thread.currentThread().join();
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//}
