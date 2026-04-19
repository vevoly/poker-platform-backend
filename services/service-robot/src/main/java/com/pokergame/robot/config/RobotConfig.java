package com.pokergame.robot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "robot")
public class RobotConfig {
    private boolean enabled = true;
    private PoolConfig pool = new PoolConfig();
    private String defaultDifficulty = "NORMAL";

    @Data
    public static class PoolConfig {
        private int initialSize = 10;
        private int maxSize = 100;
    }
}
