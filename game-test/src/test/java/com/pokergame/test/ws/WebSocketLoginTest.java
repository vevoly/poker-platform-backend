package com.pokergame.test.ws;

import com.iohao.game.external.client.AbstractInputCommandRegion;
import com.iohao.game.external.client.join.ClientRunOne;
import com.iohao.game.external.client.user.DefaultClientUser;
import com.pokergame.common.cmd.WSCmd;
import com.pokergame.test.input.EmptyInputCommandRegion;
import com.pokergame.test.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.restassured.RestAssured.given;

@Slf4j
public class WebSocketLoginTest {

    public static void main(String[] args) throws UnsupportedEncodingException {
        // 登录获取 token
        var result = LoginUtil.login("testuser1", "123456");
        if (result == null) {
            log.error("登录失败");
            return;
        }
        String token = result.getToken();
        long userId = result.getUserId();
        String nickname = URLEncoder.encode(result.getNickname(), StandardCharsets.UTF_8.name());
        String avatar = result.getAvatar();

        DefaultClientUser clientUser = new DefaultClientUser();
        clientUser.setUserId(userId);
        clientUser.setNickname(nickname);
        clientUser.setJwt(token);

        new ClientRunOne()
                .setWebsocketVerify("?token=" + token + "&nickname=" + nickname + "&avatar=" + avatar)
                .setInputCommandRegions(List.of(new EmptyInputCommandRegion()))
                .setClientUser(clientUser)
                .setConnectAddress("127.0.0.1")
                .setConnectPort(10100)
                .startup();



        log.info("WebSocket 测试客户端已启动，输入命令 login 进行 WebSocket 登录");
    }

}
