package com.pokergame.test.ws;

import com.iohao.game.external.client.AbstractInputCommandRegion;
import com.iohao.game.external.client.join.ClientRunOne;
import com.iohao.game.external.client.user.DefaultClientUser;
import com.pokergame.common.cmd.WSCmd;
import com.pokergame.common.model.auth.LoginReq;
import com.pokergame.common.model.ws.WsAttachment;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.restassured.RestAssured.given;

@Slf4j
public class WebSocketLoginTest {

    public static void main(String[] args) {
        String token = httpLogin("test001", "123456");
        log.info("HTTP 登录成功，token: {}", token);

        DefaultClientUser clientUser = new DefaultClientUser();
        clientUser.setUserId(1001L);
        clientUser.setJwt(token);

        new ClientRunOne()
                .setWebsocketVerify("?token=" + token)
                .setInputCommandRegions(List.of(new WsLoginCommandRegion(token)))
                .setClientUser(clientUser)
                .setConnectAddress("127.0.0.1")
                .setConnectPort(10100)
                .startup();

        log.info("WebSocket 测试客户端已启动，输入命令 login 进行 WebSocket 登录");
    }

    private static String httpLogin(String username, String password) {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;

        LoginReq loginReq = new LoginReq();
        loginReq.setUsername(username);
        loginReq.setPassword(password);

        return given()
                .contentType("application/json")
                .body(loginReq)
                .when()
                .post("/api/user/login")
                .then()
                .statusCode(200)
                .extract()
                .path("data.token");
    }

    @Slf4j
    static class WsLoginCommandRegion extends AbstractInputCommandRegion {

        private final String token;

        public WsLoginCommandRegion(String token) {
            this.token = token;
            this.inputCommandCreate.cmd = WSCmd.CMD;
        }

        @Override
        public void initInputCommand() {
            ofCommand(WSCmd.LOGIN)
                    .setTitle("WebSocket登录")
                    .setRequestData(() -> {
                        WsAttachment attachment = new WsAttachment();
                        attachment.setToken(token);
                        return attachment;
                    })
                    .callback(result -> {
                        log.info("WebSocket 登录成功");
                    });
        }
    }
}
