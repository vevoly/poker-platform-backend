package com.pokergame.user;

import com.iohao.game.bolt.broker.client.BrokerClientApplication;
import com.pokergame.starter.mybatis.annotation.EnableMybatisPlus;
import com.pokergame.starter.redis.annotation.EnableRedis;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 用户服务启动类
 *
 * @author poker-platform
 */
@EnableAsync
@EnableRetry
@EnableRedis
@EnableMybatisPlus
@SpringBootApplication(scanBasePackages = {"com.pokergame.user", "com.pokergame.common"})
public class UserServerApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 容器
        SpringApplication.run(UserServerApplication.class, args);

        // 启动 ioGame 逻辑服
        BrokerClientApplication.start(new UserLogicStartup());
    }
}
