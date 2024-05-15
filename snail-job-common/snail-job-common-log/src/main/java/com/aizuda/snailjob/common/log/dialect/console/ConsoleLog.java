package com.aizuda.snailjob.common.log.dialect.console;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.log.dialect.AbstractLog;
import com.aizuda.snailjob.common.log.level.Level;

/**
 * 利用System.out.println()打印日志
 *
 * @author wodeyangzipingpingwuqi
 */
public class ConsoleLog extends AbstractLog {

    private static final long serialVersionUID = -6843151523380063975L;

    private static final String logFormat = "[{date}] [{level}] {name}: {msg}";
    private static Level currentLevel = Level.DEBUG;

    private final String name;

    //------------------------------------------------------------------------- Constructor

    /**
     * 构造
     *
     * @param clazz 类
     */
    public ConsoleLog(Class<?> clazz) {
        this.name = (null == clazz) ? StrUtil.NULL : clazz.getName();
    }

    /**
     * 构造
     *
     * @param name 类名
     */
    public ConsoleLog(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * 设置自定义的日志显示级别
     *
     * @param customLevel 自定义级别
     * @since 4.1.10
     */
    public static void setLevel(Level customLevel) {
        Assert.notNull(customLevel);
        currentLevel = customLevel;
    }

    //------------------------------------------------------------------------- Trace
    @Override
    public boolean isTraceEnabled() {
        return isEnabled(Level.TRACE);
    }

    @Override
    public void trace(Boolean remote, String fqcn, String format, Object... arguments) {
        log(Level.TRACE, remote, fqcn, format, arguments);
    }

    //------------------------------------------------------------------------- Debug
    @Override
    public boolean isDebugEnabled() {
        return isEnabled(Level.DEBUG);
    }

    @Override
    public void debug(Boolean remote, String fqcn, String format, Object... arguments) {
        log(Level.DEBUG, remote, fqcn, format, arguments);
    }

    //------------------------------------------------------------------------- Info
    @Override
    public boolean isInfoEnabled() {
        return isEnabled(Level.INFO);
    }

    @Override
    public void info(Boolean remote, String fqcn, String format, Object... arguments) {
        log(Level.INFO, remote, fqcn, format, arguments);
    }

    //------------------------------------------------------------------------- Warn
    @Override
    public boolean isWarnEnabled() {
        return isEnabled(Level.WARN);
    }

    @Override
    public void warn(Boolean remote, String fqcn, String format, Object... arguments) {
        log(Level.WARN, remote, fqcn, format, arguments);
    }

    //------------------------------------------------------------------------- Error
    @Override
    public boolean isErrorEnabled() {
        return isEnabled(Level.ERROR);
    }

    @Override
    public void error(Boolean remote, String fqcn, String format, Object... arguments) {
        log(Level.ERROR, remote, fqcn, format, arguments);
    }

    //------------------------------------------------------------------------- Log
    @Override
    public void log(Level level, Boolean remote, String fqcn, String format, Object... arguments) {
        // fqcn 无效
        if (!isEnabled(level)) {
            return;
        }

        final Dict dict = Dict.create()
                .set("date", DateUtil.now())
                .set("level", level.toString())
                .set("name", this.name)
                .set("msg", StrUtil.format(format, arguments));

        final String logMsg = StrUtil.format(logFormat, dict);

        //WARN以上级别打印至System.err
        if (level.ordinal() >= Level.WARN.ordinal()) {
            Console.error(ConsoleLogFactory.extractThrowable(arguments), logMsg);
        } else {
            Console.log(ConsoleLogFactory.extractThrowable(arguments), logMsg);
        }
    }

    @Override
    public boolean isEnabled(Level level) {
        return currentLevel.compareTo(level) <= 0;
    }
}
