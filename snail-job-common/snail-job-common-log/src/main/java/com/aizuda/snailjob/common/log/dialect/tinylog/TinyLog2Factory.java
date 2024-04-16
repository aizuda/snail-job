package com.aizuda.snailjob.common.log.dialect.tinylog;


import com.aizuda.snailjob.common.log.dialect.Log;
import com.aizuda.snailjob.common.log.factory.LogFactory;

/**
 * <a href="http://www.tinylog.org/">TinyLog2</a> log.<br>
 *
 * @author wodeyangzipingpingwuqi
 */
public class TinyLog2Factory extends LogFactory {

    /**
     * 构造
     */
    public TinyLog2Factory() {
        super("TinyLog");
        checkLogExist(org.tinylog.Logger.class);
    }

    @Override
    public Log createLog(String name) {
        return new TinyLog2(name);
    }

    @Override
    public Log createLog(Class<?> clazz) {
        return new TinyLog2(clazz);
    }

}
