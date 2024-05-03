package com.aizuda.snailjob.common.log.level;

/**
 * DEBUG级别日志接口
 *
 * @author wodeyangzipingpingwuqi
 */
public interface DebugLog {

    /**
     * @return DEBUG 等级是否开启
     */
    boolean isDebugEnabled();

    /**
     * 打印 DEBUG 等级的日志
     *
     * @param t 错误对象
     */
    void debug(Boolean remote, Throwable t);

    /**
     * 打印 DEBUG 等级的日志
     *
     * @param format    消息模板
     * @param arguments 参数
     */
    void debug(Boolean remote, String format, Object... arguments);

    /**
     * 打印 DEBUG 等级的日志
     *
     * @param fqcn      完全限定类名(Fully Qualified Class Name)，用于定位日志位置
     * @param format    消息模板
     * @param arguments 参数
     */
    void debug(Boolean remote, String fqcn, String format, Object... arguments);

    /**
     * 打印 DEBUG 等级的日志
     *
     * @param msg 日志信息
     */
    void debug(Boolean remote, String msg);

    /**
     * 打印 DEBUG 等级的日志
     *
     * @param msg 日志信息
     * @param t   错误对象
     */
    void debug(Boolean remote, String msg, Throwable t);
}
