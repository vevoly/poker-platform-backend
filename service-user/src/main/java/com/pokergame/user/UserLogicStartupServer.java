package com.pokergame.user;

import com.iohao.game.bolt.broker.client.BrokerClientApplication;

public class UserLogicStartupServer {

    public static void main(String[] args) {
        UserLogicStartup userLogicStartup = new UserLogicStartup();
        // 启动游戏逻辑服
        BrokerClientApplication.start(userLogicStartup);
    }
}
