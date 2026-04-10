package com.pokergame.starter.redis.annotation;

import com.pokergame.starter.redis.config.RedisAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 Redis Starter
 *
 * 使用方式：在 Spring Boot 启动类上添加 @EnableRedis
 *
 * @author poker-platform
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RedisAutoConfiguration.class)
public @interface EnableRedis {
}
