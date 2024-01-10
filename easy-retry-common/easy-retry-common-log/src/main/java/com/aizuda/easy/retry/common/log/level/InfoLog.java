package com.aizuda.easy.retry.common.log.level;

/**
 * INFO级别日志接口
 *
 * @author wodeyangzipingpingwuqi
 */
public interface InfoLog {
    /**
     * @return INFO 等级是否开启
     */
    boolean isInfoEnabled();

    /**
     * 打印 INFO 等级的日志
     *
     * @param t 错误对象
     */
    void info(Throwable t, Boolean remote);

    /**
     * 打印 INFO 等级的日志
     *
     * @param format    消息模板
     * @param arguments 参数
     */
    void info(String format, Boolean remote, Object... arguments);

    /**
     * 打印 INFO 等级的日志
     *
     * @param t         错误对象
     * @param format    消息模板
     * @param arguments 参数
     */
    void info(Throwable t, String format, Boolean remote, Object... arguments);

    /**
     * 打印 INFO 等级的日志
     *
     * @param fqcn      完全限定类名(Fully Qualified Class Name)，用于定位日志位置
     * @param t         错误对象
     * @param format    消息模板
     * @param arguments 参数
     */
    void info(String fqcn, Throwable t, String format, Boolean remote, Object... arguments);
}
