package com.pokergame.test.doudizhu;

import com.iohao.game.external.client.join.ClientRunOne;
import com.iohao.game.external.client.user.DefaultClientUser;
import com.pokergame.common.model.auth.LoginReq;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * 斗地主客户端测试（含 HTTP 登录 + WebSocket 认证）
 * <p>
 * 启动后自动通过 HTTP 登录获取 token，然后建立 WebSocket 连接。
 * <p>
 * 使用步骤：
 * 1. 首先输入命令 `1-1` 进行 WebSocket 登录（只需执行一次）
 * 2. 然后输入斗地主命令，例如：
 *    - 101-1  创建房间
 *    - 101-2  加入房间
 *    - 101-10 准备
 *    - 101-12 抢地主
 *    - 101-13 出牌
 *    - 101-14 过牌
 *
 * @author poker-platform
 */
//@Slf4j
//public class DoudizhuClientTest {
//
//    public static void main(String[] args) {
//        // 1. HTTP 登录获取 token
//        String token = httpLogin("test001", "123456");
//        log.info("HTTP 登录成功，token: {}", token);
//
//        // 2. 创建模拟玩家
//        DefaultClientUser clientUser = new DefaultClientUser();
//        clientUser.setUserId(1001L);          // 临时 ID，实际登录成功后可从响应获取
//        clientUser.setJwt(token);
//
//        // 3. 启动 WebSocket 客户端，并自动携带 token
//        new ClientRunOne()
//                .setWebsocketVerify("?token=" + token)   // 关键：将 token 添加到 WebSocket URL
//                .setInputCommandRegions(List.of(new DoudizhuInputCommandRegion()))
//                .setClientUser(clientUser)
//                .setConnectAddress("127.0.0.1")
//                .setConnectPort(10100)
//                .startup();
//
//        log.info("斗地主模拟客户端已启动");
//    }
//
//    /**
//     * HTTP 登录，返回 token
//     */
//    private static String httpLogin(String username, String password) {
//        RestAssured.baseURI = "http://localhost";
//        RestAssured.port = 8080;
//
//        LoginReq loginReq = new LoginReq();
//        loginReq.setUsername(username);
//        loginReq.setPassword(password);
//
//        return given()
//                .contentType("application/json")
//                .body(loginReq)
//                .when()
//                .post("/api/user/login")
//                .then()
//                .statusCode(200)
//                .extract()
//                .path("data.token");
//    }
//}
