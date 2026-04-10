package com.pokergame.starter.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.pokergame.starter.mybatis.handler.CustomMetaObjectHandler;
import com.pokergame.starter.mybatis.interceptor.CustomPaginationInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 自动配置类
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 插件配置
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件（根据数据库类型自动选择）
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(1000L); // 最大单页限制
        paginationInterceptor.setOverflow(true);  // 溢出总页数后是否进行处理
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 2. 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 3. 防全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        // 4. 自定义插件（数据权限、租户等）
        interceptor.addInnerInterceptor(new CustomPaginationInterceptor());

        return interceptor;
    }

    /**
     * 全局配置（自动填充）
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalConfig globalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();

        // 逻辑删除配置
        dbConfig.setLogicDeleteValue("1");  // 逻辑已删除值
        dbConfig.setLogicNotDeleteValue("0"); // 逻辑未删除值

        globalConfig.setDbConfig(dbConfig);
        globalConfig.setMetaObjectHandler(new CustomMetaObjectHandler());

        return globalConfig;
    }
}
