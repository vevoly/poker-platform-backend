package com.pokergame.starter.mybatis.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * MyBatis-Plus 代码生成器
 *
 * 运行 main 方法即可生成 Entity、Mapper、Service、Controller
 *
 * @author poker-platform
 */
public class CodeGenerator {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/poker_game?useSSL=false&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "root";
        String outputDir = System.getProperty("user.dir") + "/service-user/src/main/java";

        FastAutoGenerator.create(url, username, password)
                // 全局配置
                .globalConfig(builder -> builder
                        .author("poker-platform")
                        .outputDir(outputDir)
                        .disableOpenDir()
                        .enableSwagger()
                )
                // 包配置
                .packageConfig(builder -> builder
                        .parent("com.pokergame.user")
                        .entity("entity")
                        .mapper("dao")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .pathInfo(Collections.singletonMap(OutputFile.xml,
                                System.getProperty("user.dir") + "/service-user/src/main/resources/mapper"))
                )
                // 策略配置
                .strategyConfig(builder -> builder
                        .addInclude("user", "user_currency", "user_stats")
                        .entityBuilder()
                        .enableLombok()
                        .enableTableFieldAnnotation()
                        .logicDeleteColumnName("deleted")
                        .versionColumnName("version")
                        .mapperBuilder()
                        .enableBaseResultMap()
                        .enableBaseColumnList()
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
