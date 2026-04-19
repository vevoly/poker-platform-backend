package com.pokergame.starter.spring.config;

import com.pokergame.starter.spring.aspect.GameEventPublishAspect;
import com.pokergame.starter.spring.context.SpringContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 游戏事件自动配置
 * <p>
 * 启用 AOP 并注册相关 Bean。
 * 引入本 starter 后会自动生效。
 * </p>
 *
 * @author poker-platform
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class GameEventAutoConfiguration {

    /**
     * 注册 SpringContextHolder（确保被 Spring 管理）
     */
    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }

    /**
     * 注册事件发布切面
     */
    @Bean
    public GameEventPublishAspect gameEventPublishAspect() {
        return new GameEventPublishAspect();
    }
}
