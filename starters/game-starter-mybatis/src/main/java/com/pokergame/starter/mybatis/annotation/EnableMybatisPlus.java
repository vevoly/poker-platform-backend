package com.pokergame.starter.mybatis.annotation;

import com.pokergame.starter.mybatis.config.MybatisPlusConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 MyBatis-Plus 扩展功能
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MybatisPlusConfig.class)
public @interface EnableMybatisPlus {
}
