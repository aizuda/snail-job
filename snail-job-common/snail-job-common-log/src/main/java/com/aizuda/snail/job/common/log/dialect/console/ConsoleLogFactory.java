package com.aizuda.snail.job.common.log.dialect.console;


import com.aizuda.snail.job.common.log.dialect.Log;
import com.aizuda.snail.job.common.log.factory.LogFactory;

/**
 * 利用System.out.println()打印日志
 *
 * @author wodeyangzipingpingwuqi
 */
public class ConsoleLogFactory extends LogFactory {

    public ConsoleLogFactory() {
        super("Hutool Console Logging");
    }

    @Override
    public Log createLog(String name) {
        return new ConsoleLog(name);
    }

    @Override
    public Log createLog(Class<?> clazz) {
        return new ConsoleLog(clazz);
    }

}
