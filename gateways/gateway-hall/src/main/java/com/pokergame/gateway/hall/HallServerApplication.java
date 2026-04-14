package com.pokergame.gateway.hall;

import com.iohao.game.bolt.broker.client.BrokerClientApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.pokergame.gateway.hall", "com.pokergame.common"})
public class HallServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HallServerApplication.class, args);
        // 启动 ioGame Broker 客户端（用于 RPC 调用）
        BrokerClientApplication.start(new HallLogicStartup());

    }
}
