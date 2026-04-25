package com.pokergame.test.util;

import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.external.client.AbstractInputCommandRegion;
import com.iohao.game.external.client.InputCommandRegion;
import com.iohao.game.external.client.join.ClientRunOne;
import com.iohao.game.external.client.user.DefaultClientUser;
import com.pokergame.test.region.EmptyInputCommandRegion;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * WebSocket 测试客户端工具类
 * 封装 HTTP 登录、URL 参数编码、ClientRunOne 启动等流程
 */
@Slf4j
public class WebSocketClient {

    public static boolean start(String username, String password, List<InputCommandRegion> commandRegions) {
        LoginUtil.LoginResult loginResult = LoginUtil.login(username, password);
        if (loginResult == null) {
            log.error("用户 {} 登录失败", username);
            return false;
        }

        String token = loginResult.getToken();
        long userId = loginResult.getUserId();
        String nickname;
        try {
            nickname = URLEncoder.encode(loginResult.getNickname(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.error("昵称编码失败", e);
            return false;
        }
        String avatar = loginResult.getAvatar() != null ? loginResult.getAvatar() : "";

        DefaultClientUser clientUser = new DefaultClientUser();
        clientUser.setUserId(userId);
        clientUser.setNickname(nickname);
        clientUser.setJwt(token);

        String wsVerifyParam = String.format("?token=%s&nickname=%s&avatar=%s", token, nickname, avatar);

        ClientRunOne clientRunOne = new ClientRunOne()
                .setWebsocketVerify(wsVerifyParam)
                .setInputCommandRegions(commandRegions)
                .setClientUser(clientUser)
                .setConnectAddress("127.0.0.1")
                .setConnectPort(10100);

        clientRunOne.startup();

        log.info("WebSocket 客户端启动成功，用户: {} ({})", loginResult.getNickname(), userId);
        return true;
    }

    public static boolean start(String username, String password) {
        return start(username, password, List.of(new EmptyInputCommandRegion()));
    }
}
