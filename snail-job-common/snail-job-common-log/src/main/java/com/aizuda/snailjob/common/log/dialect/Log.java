package com.aizuda.snailjob.common.log.dialect;


import com.aizuda.snailjob.common.log.factory.LogFactory;
import com.aizuda.snailjob.common.log.lang.LogCaller;
import com.aizuda.snailjob.common.log.level.*;

/**
 * 日志统一接口
 *
 * @author wodeyangzipingpingwuqi
 */
public interface Log extends TraceLog, DebugLog, InfoLog, WarnLog, ErrorLog {

    //------------------------------------------------------------------------ Static method start

    /**
     * 获得Log
     *
     * @param clazz 日志发出的类
     * @return Log
     */
    static Log get(Class<?> clazz) {
        return LogFactory.get(clazz);
    }

    /**
     * 获得Log
     *
     * @param name 自定义的日志发出者名称
     * @return Log
     * @since 5.0.0
     */
    static Log get(String name) {
        return LogFactory.get(name);
    }

    /**
     * @return 获得日志，自动判定日志发出者
     * @since 5.0.0
     */
    static Log get() {
        return LogFactory.get(LogCaller.getCallerCaller());
    }
    //------------------------------------------------------------------------ Static method end

    /**
     * @return 日志对象的Name
     */
    String getName();

    /**
     * 是否开启指定日志
     *
     * @param level 日志级别
     * @return 是否开启指定级别
     */
    boolean isEnabled(Level level);

    /**
     * 打印指定级别的日志
     *
     * @param level     级别
     * @param format    消息模板
     * @param arguments 参数
     */
    void log(Level level, Boolean remote, String format, Object... arguments);

    /**
     * 打印 ERROR 等级的日志
     *
     * @param fqcn      完全限定类名(Fully Qualified Class Name)，用于定位日志位置
     * @param level     级别
     * @param format    消息模板
     * @param arguments 参数
     */
    void log(Level level, Boolean remote, String fqcn, String format, Object... arguments);
}
