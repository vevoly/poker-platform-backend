package com.pokergame.starter.spring.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文持有者
 * <p>
 * 用于在非 Spring 管理的类中获取 Spring Bean。
 * 注意：该类本身由 Spring 管理，通过实现 ApplicationContextAware 获得上下文。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * RobotManager manager = SpringContextHolder.getBean(RobotManager.class);
 * </pre>
 *
 * @author poker-platform
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * 根据类型获取 Bean
     *
     * @param clazz Bean 类型
     * @param <T>   泛型
     * @return Bean 实例
     */
    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) {
            throw new IllegalStateException("SpringContextHolder 尚未初始化，请确保已标注 @Component 并被 Spring 扫描");
        }
        return applicationContext.getBean(clazz);
    }

    /**
     * 根据名称和类型获取 Bean
     *
     * @param name  Bean 名称
     * @param clazz Bean 类型
     * @param <T>   泛型
     * @return Bean 实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 获取原始 ApplicationContext（谨慎使用）
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
