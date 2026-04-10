package com.pokergame.starter.mybatis.interceptor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;

/**
 * 自定义分页拦截器（可扩展数据权限、租户过滤等）
 */
@Slf4j
public class CustomPaginationInterceptor implements InnerInterceptor {

    /**
     * 查询前处理（可用于添加额外的查询条件）
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql)
            throws SQLException {

        // 如果是分页查询
        if (parameter instanceof IPage) {
            log.debug("执行分页查询: {}", ms.getId());

            // TODO: 在这里可以添加数据权限过滤
            // addDataPermission(boundSql);

            // TODO: 在这里可以添加租户隔离
            // addTenantCondition(boundSql);
        }
    }

    /**
     * 查询后处理
     */
    @Override
    public void afterQuery(Executor executor, MappedStatement ms, Object parameter,
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql,
                           Object result) throws SQLException {
        if (result instanceof IPage) {
            log.debug("分页查询完成: {}", ms.getId());
            // 可以在这里处理查询结果，如脱敏等
        }
    }
}
