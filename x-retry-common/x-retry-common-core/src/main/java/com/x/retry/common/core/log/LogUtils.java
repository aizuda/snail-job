package com.x.retry.common.core.log;

import com.x.retry.common.core.context.SpringContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

/**
 * 全局日志
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 16:27
 */
@Slf4j
public class LogUtils {

    private LogUtils() {
    }

    public static void debug(String format, Object... arguments) {

        if (!getLogStatus()) {
            return;
        }

        log.debug(format, arguments);

    }

    public static void info(String format, Object... arguments) {

        if (!getLogStatus()) {
            return;
        }

        log.info(format, arguments);

    }

    public static void error(String format, Object... arguments) {
        if (!getLogStatus()) {
            return;
        }

        log.error(format, arguments);
    }

    public static void error(String format, Throwable t) {

        if (!getLogStatus()) {
            return;
        }

        log.error(format, t);
    }

    public static void warn(String format, Object... arguments) {
        if (!getLogStatus()) {
            return;
        }

        log.warn(format, arguments);
    }

    /**
     * 获取日志状态
     *
     * @return
     */
    private static Boolean getLogStatus() {

        try {
            Environment environment = SpringContext.applicationContext.getBean(Environment.class);
            return environment.getProperty("flaky.retry.log.status", Boolean.class, Boolean.TRUE);
        } catch (Exception e) {
//            log.error("获取配置失败", e);
        }

        return Boolean.TRUE;
    }
}
