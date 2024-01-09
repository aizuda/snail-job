package com.aizuda.easy.retry.client.common.log.dialect.tinylog;

import com.aizuda.easy.retry.client.common.log.Log;
import com.aizuda.easy.retry.client.common.log.LogFactory;

/**
 * <a href="http://www.tinylog.org/">TinyLog</a> log.<br>
 *
 * @author wodeyangzipingpingwuqi
 */
public class TinyLogFactory extends LogFactory {

    /**
     * 构造
     */
    public TinyLogFactory() {
        super("TinyLog");
        checkLogExist(org.pmw.tinylog.Logger.class);
    }

    @Override
    public Log createLog(String name) {
        return new TinyLog(name);
    }

    @Override
    public Log createLog(Class<?> clazz) {
        return new TinyLog(clazz);
    }

}
