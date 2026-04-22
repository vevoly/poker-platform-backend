package com.pokergame.test.util;

import com.iohao.game.external.client.AbstractInputCommandRegion;
import com.iohao.game.external.client.InputCommandRegion;
import com.iohao.game.external.client.join.ClientRunOne;
import com.iohao.game.external.client.user.DefaultClientUser;
import com.pokergame.test.input.EmptyInputCommandRegion;
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
    /**
     * 启动 WebSocket 客户端（自动登录并建立连接）
     *
     * @param username       用户名
     * @param password       密码
     * @param commandRegions 命令区域列表（可为空，至少传一个或传 null）
     * @return 是否启动成功
     */
    public static boolean start(String username, String password, List<InputCommandRegion> commandRegions) {
        // 1. HTTP 登录获取 token 和用户信息
        LoginUtil.LoginResult loginResult = LoginUtil.login(username, password);
        if (loginResult == null) {
            log.error("用户 {} 登录失败，无法启动 WebSocket 客户端", username);
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
        String avatar = loginResult.getAvatar();
        if (avatar == null) avatar = "";

        // 2. 构建 ClientUser
        DefaultClientUser clientUser = new DefaultClientUser();
        clientUser.setUserId(userId);
        clientUser.setNickname(nickname);
        clientUser.setJwt(token);

        // 3. 构建 WebSocket 连接参数
        String wsVerifyParam = String.format("?token=%s&nickname=%s&avatar=%s", token, nickname, avatar);

        // 4. 启动 ClientRunOne
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

    /**
     * 启动 WebSocket 客户端（无业务命令区域）
     */
    public static boolean start(String username, String password) {
        return start(username, password, List.of(new EmptyInputCommandRegion()));
    }
}
