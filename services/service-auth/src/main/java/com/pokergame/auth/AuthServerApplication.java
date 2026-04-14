package com.pokergame.auth;

import com.iohao.game.action.skeleton.ext.spring.ActionFactoryBeanForSpring;
import com.iohao.game.bolt.broker.client.BrokerClientApplication;
import com.pokergame.starter.redis.annotation.EnableRedis;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableRedis
@SpringBootApplication
public class AuthServerApplication {
    public static void main(String[] args) {
        // 启动 Spring Boot 容器
        SpringApplication.run(AuthServerApplication.class, args);

        // 启动 ioGame 逻辑服
        BrokerClientApplication.start(new AuthLogicStartup());
    }

    /**
     * 将业务框架交给 spring 管理
     */
    @Bean
    public ActionFactoryBeanForSpring actionFactoryBean() {
        return ActionFactoryBeanForSpring.me();
    }
}
