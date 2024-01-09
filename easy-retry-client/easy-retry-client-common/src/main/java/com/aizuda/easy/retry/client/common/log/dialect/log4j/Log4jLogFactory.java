package com.aizuda.easy.retry.client.common.log.dialect.log4j;


import com.aizuda.easy.retry.client.common.log.Log;
import com.aizuda.easy.retry.client.common.log.LogFactory;

/**
 * <a href="http://logging.apache.org/log4j/1.2/index.html">Apache Log4J</a> log.<br>
 *
 * @author wodeyangzipingpingwuqi
 */
public class Log4jLogFactory extends LogFactory {

    public Log4jLogFactory() {
        super("Log4j");
        checkLogExist(org.apache.log4j.Logger.class);
    }

    @Override
    public Log createLog(String name) {
        return new Log4jLog(name);
    }

    @Override
    public Log createLog(Class<?> clazz) {
        return new Log4jLog(clazz);
    }

}
