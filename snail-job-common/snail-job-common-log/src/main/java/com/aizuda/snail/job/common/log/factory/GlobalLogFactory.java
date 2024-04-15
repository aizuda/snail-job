package com.aizuda.snail.job.common.log.factory;


import com.aizuda.snail.job.common.log.dialect.commons.ApacheCommonsLogFactory;
import com.aizuda.snail.job.common.log.dialect.console.ConsoleLogFactory;
import com.aizuda.snail.job.common.log.dialect.jdk.JdkLogFactory;
import com.aizuda.snail.job.common.log.dialect.log4j.Log4jLogFactory;
import com.aizuda.snail.job.common.log.dialect.log4j2.Log4j2LogFactory;
import com.aizuda.snail.job.common.log.dialect.slf4j.Slf4jLogFactory;
import org.springframework.core.env.Environment;

import java.util.Objects;

/**
 * 全局日志工厂类<br>
 * 用于减少日志工厂创建，减少日志库探测
 *
 * @author wodeyangzipingpingwuqi
 */
public class GlobalLogFactory {

    private static volatile LogFactory currentLogFactory;
    private static final Object lock = new Object();

    private static Environment environment;

    public static void setEnvironment(final Environment environment) {
        GlobalLogFactory.environment = environment;
    }

    /**
     * 获取单例日志工厂类，如果不存在创建之
     *
     * @return 当前使用的日志工厂
     */
    public static LogFactory get() {
        if (null == currentLogFactory) {
            synchronized (lock) {
                if (null == currentLogFactory) {
                    currentLogFactory = LogFactory.create();
                }
            }
        }
        return currentLogFactory;
    }

    /**
     * 自定义日志实现
     *
     * @param logFactoryClass 日志工厂类
     * @return 自定义的日志工厂类
     * @see Slf4jLogFactory
     * @see Log4jLogFactory
     * @see Log4j2LogFactory
     * @see ApacheCommonsLogFactory
     * @see JdkLogFactory
     * @see ConsoleLogFactory
     */
    public static LogFactory set(Class<? extends LogFactory> logFactoryClass) {
        try {
            return set(logFactoryClass.newInstance());
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not instance LogFactory class!", e);
        }
    }

    /**
     * 自定义日志实现
     *
     * @param logFactory 日志工厂类对象
     * @return 自定义的日志工厂类
     * @see Slf4jLogFactory
     * @see Log4jLogFactory
     * @see Log4j2LogFactory
     * @see ApacheCommonsLogFactory
     * @see JdkLogFactory
     * @see ConsoleLogFactory
     */
    public static LogFactory set(LogFactory logFactory) {
        logFactory.getLog(GlobalLogFactory.class).debug("Custom Use [{}] Logger.", false, logFactory.name);
        currentLogFactory = logFactory;
        return currentLogFactory;
    }

    /**
     * 获取全局的日志开关
     *
     * @return
     */
    public static Boolean logSwitch() {

        if (Objects.nonNull(environment)) {
            return environment.getProperty("easy.retry.log.status", Boolean.class, Boolean.TRUE);
        }

        return Boolean.TRUE;
    }
}
