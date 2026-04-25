package com.pokergame.test.doudizhu;

import com.iohao.game.external.client.join.ClientRunOne;
import com.iohao.game.external.client.user.ClientUser;
import com.iohao.game.external.client.user.DefaultClientUser;
import com.pokergame.test.region.DoudizhuGameInputCommandRegion;
import com.pokergame.test.region.RoomInputCommandRegion;
import com.pokergame.test.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

@Slf4j
public class HumanPlayerClient2 {

    public static void main(String[] args) throws UnsupportedEncodingException {
        var result = LoginUtil.login("test003", "123456");
        if (result == null) {
            log.error("登录失败");
            return;
        }
        String token = result.getToken();
        long userId = result.getUserId();
        String nickname = URLEncoder.encode(result.getNickname(), "UTF-8");
        String avatar = result.getAvatar() != null ? result.getAvatar() : "";

        DefaultClientUser clientUser = new DefaultClientUser();
        clientUser.setUserId(userId);
        clientUser.setNickname(nickname);
        clientUser.setJwt(token);

        RoomInputCommandRegion roomRegion = new RoomInputCommandRegion();
        DoudizhuGameInputCommandRegion gameRegion = new DoudizhuGameInputCommandRegion();

        new ClientRunOne()
                .setWebsocketVerify("?token=" + token + "&nickname=" + nickname + "&avatar=" + avatar)
                .setInputCommandRegions(List.of(roomRegion, gameRegion))
                .setClientUser(clientUser)
                .setConnectAddress("127.0.0.1")
                .setConnectPort(10100)
                .startup();

        log.info("玩家2客户端已启动，命令示例：");
        log.info("  加入房间: 100-2");
        log.info("  准备: 100-10");
        log.info("  不抢地主: 101-2");
        log.info("  出牌: 101-3");
        log.info("  过牌: 101-4");

        try { Thread.currentThread().join(); } catch (InterruptedException e) {}
    }
}