package com.aizuda.snailjob.server.common;

/**
 * 组件生命周期
 *
 * @author: opensnail
 * @date : 2021-11-19 14:43
 */
public interface Lifecycle {

    /**
     * 启动组件
     */
    void start();

    /**
     * 关闭组件
     */
    void close();

}
