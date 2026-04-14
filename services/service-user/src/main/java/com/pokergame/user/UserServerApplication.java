package com.pokergame.user;

import com.iohao.game.action.skeleton.ext.spring.ActionFactoryBeanForSpring;
import com.iohao.game.bolt.broker.client.BrokerClientApplication;
import com.pokergame.starter.mybatis.annotation.EnableMybatisPlus;
import com.pokergame.starter.redis.annotation.EnableRedis;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;

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
        ConfigurableApplicationContext context = SpringApplication.run(UserServerApplication.class, args);

        // 启动 ioGame 逻辑服
        BrokerClientApplication.start(new UserLogicStartup());
    }

    /**
     * 将业务框架交给 spring 管理
     */
    @Bean
    public ActionFactoryBeanForSpring actionFactoryBean() {
        return ActionFactoryBeanForSpring.me();
    }
}
