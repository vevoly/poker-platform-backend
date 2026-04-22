package com.pokergame.test.doudizhu;

import com.iohao.game.external.client.join.ClientRunOne;
import com.iohao.game.external.client.user.ClientUser;
import com.iohao.game.external.client.user.DefaultClientUser;
import com.pokergame.test.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class HumanPlayerClient1 {

    public static void main(String[] args) {
        // 登录获取 token
        var result = LoginUtil.login("testuser1", "123456");
        if (result == null) {
            log.error("登录失败");
            return;
        }
        String token = result.getToken();
        long userId = result.getUserId();

        ClientUser clientUser = new DefaultClientUser();
        clientUser.setUserId(userId);
        clientUser.setJwt(token);

        // 启动两个命令区域
        RoomInputCommandRegion roomRegion = new RoomInputCommandRegion();
        DoudizhuGameInputCommandRegion gameRegion = new DoudizhuGameInputCommandRegion();

        new ClientRunOne()
                .setWebsocketVerify("?token=" + token)
                .setInputCommandRegions(List.of(roomRegion, gameRegion))
                .setClientUser(clientUser)
                .setConnectAddress("127.0.0.1")
                .setConnectPort(10100)
                .startup();

        log.info("玩家1客户端已启动，命令示例：");
        log.info("  创建房间: 100-1");
        log.info("  准备: 100-10");
        log.info("  开始游戏: 100-11");
        log.info("  抢地主: 101-1");
        log.info("  出牌: 101-3");
        log.info("  过牌: 101-4");

        try { Thread.currentThread().join(); } catch (InterruptedException e) {}
    }
}
