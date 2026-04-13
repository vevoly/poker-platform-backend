package com.pokergame.auth;

import com.iohao.game.bolt.broker.client.BrokerClientApplication;
import com.pokergame.starter.mybatis.annotation.EnableMybatisPlus;
import com.pokergame.starter.redis.annotation.EnableRedis;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRedis
@EnableMybatisPlus
@SpringBootApplication(scanBasePackages = {"com.pokergame.auth", "com.pokergame.common"})
public class AuthServerApplication {
    public static void main(String[] args) {
        // 启动 Spring Boot 容器
        SpringApplication.run(AuthServerApplication.class, args);

        // 启动 ioGame 逻辑服
        BrokerClientApplication.start(new AuthLogicStartup());
    }
}
