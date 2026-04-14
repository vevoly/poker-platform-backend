package com.pokergame.gateway.hall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 大厅 HTTP 服务配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "hall.auth")
public class HallConfig {
    /**
     * 白名单路径（不需要 Token 验证）
     */
    private Set<String> whiteList;
}
