package com.pokergame.robot;

import com.iohao.game.action.skeleton.ext.spring.ActionFactoryBeanForSpring;
import com.iohao.game.bolt.broker.client.BrokerClientApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RobotServerApplication {

    public static void main(String[] args) {

        // 启动 ioGame 逻辑服
        BrokerClientApplication.start(new RobotLogicStartup());
        // 启动 Spring Boot
        SpringApplication.run(RobotServerApplication.class, args);

    }

    /**
     * 将业务框架交给 spring 管理
     */
    @Bean
    public ActionFactoryBeanForSpring actionFactoryBean() {
        return ActionFactoryBeanForSpring.me();
    }
}
