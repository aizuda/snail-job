package com.aizuda.snailjob.common.log.dialect.commons;

import com.aizuda.snailjob.common.log.dialect.log4j.Log4jLog;
import com.aizuda.snailjob.common.log.dialect.log4j.Log4jLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;

/**
 * Apache Commons Logging for Log4j
 *
 * @author wodeyangzipingpingwuqi
 */
public class ApacheCommonsLog4JLog extends Log4jLog {
    private static final long serialVersionUID = -6843151523380063975L;

    // ------------------------------------------------------------------------- Constructor
    public ApacheCommonsLog4JLog(Log logger) {
        super(((Log4JLogger) logger).getLogger());
    }

    public ApacheCommonsLog4JLog(Class<?> clazz) {
        super(clazz);
    }

    public ApacheCommonsLog4JLog(String name) {
        super(name);
    }
}
