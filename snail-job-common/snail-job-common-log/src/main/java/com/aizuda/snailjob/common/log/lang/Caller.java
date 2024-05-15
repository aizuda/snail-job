package com.aizuda.snailjob.common.log.lang;

/**
 * 调用者接口<br>
 * 可以通过此接口的实现类方法获取调用者、多级调用者以及判断是否被调用
 *
 * @author wodeyangzipingpingwuqi
 */
public interface Caller {

    /**
     * 获得调用者的调用者
     *
     * @return 调用者的调用者
     */
    Class<?> getCallerCaller();

}
