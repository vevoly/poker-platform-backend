package com.pokergame.starter.mybatis.handler;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.time.LocalDateTime;

/**
 * 自动填充处理器
 */
@Slf4j
public class CustomMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");

        // 创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 逻辑删除默认为0
        this.strictInsertFill(metaObject, "delFlag", Integer.class, 0);

        // 获取当前用户（从请求头或上下文中获取）
        String currentUser = getCurrentUser();

        // 创建人
        if (metaObject.hasSetter("createBy")) {
            this.strictInsertFill(metaObject, "createBy", String.class, currentUser);
        }
        // 更新人
        if (metaObject.hasSetter("updateBy")) {
            this.strictInsertFill(metaObject, "updateBy", String.class, currentUser);
        }
    }

    /**
     * 更新时填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");

        // 更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 更新人
        if (metaObject.hasSetter("updateBy")) {
            this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUser());
        }
    }

    /**
     * 获取当前用户（可根据实际情况调整）
     */
    private String getCurrentUser() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userId = request.getHeader("X-User-Id");
                if (StrUtil.isNotBlank(userId)) {
                    return userId;
                }
                String username = request.getHeader("X-Username");
                if (StrUtil.isNotBlank(username)) {
                    return username;
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户失败", e);
        }
        return "system"; // 默认系统用户
    }
}
