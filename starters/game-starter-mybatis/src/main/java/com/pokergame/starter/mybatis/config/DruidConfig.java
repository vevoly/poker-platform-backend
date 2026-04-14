package com.pokergame.starter.mybatis.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceBuilder;
import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Druid 连接池配置
 *
 * @author poker-platform
 */
@Configuration
@ConditionalOnClass(DruidDataSource.class)
public class DruidConfig {

    /**
     * Druid 数据源
     */
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties("spring.datasource.druid")
    public DataSource dataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * Druid 监控页面 Servlet
     * 访问地址：http://localhost:8080/druid/index.html
     */
    @Bean
    @ConditionalOnMissingBean(name = "statViewServlet")
    public ServletRegistrationBean<StatViewServlet> statViewServlet() {
        ServletRegistrationBean<StatViewServlet> servlet = new ServletRegistrationBean<>();
        servlet.setServlet(new StatViewServlet());
        servlet.addUrlMappings("/druid/*");

        // 监控页面参数配置
        servlet.addInitParameter("loginUsername", "admin");
        servlet.addInitParameter("loginPassword", "admin");
        servlet.addInitParameter("resetEnable", "false");

        return servlet;
    }

    /**
     * Druid 监控过滤器
     */
    @Bean
    @ConditionalOnMissingBean(name = "statFilter")
    public FilterRegistrationBean<WebStatFilter> statFilter() {
        FilterRegistrationBean<WebStatFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new WebStatFilter());
        filter.addUrlPatterns("/*");
        filter.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        filter.addInitParameter("profileEnable", "true");
        filter.addInitParameter("sessionStatEnable", "true");
        return filter;
    }
}
