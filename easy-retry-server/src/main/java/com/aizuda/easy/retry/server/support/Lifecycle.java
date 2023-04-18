package com.aizuda.easy.retry.server.support;

/**
 * 组件生命周期
 *
 * @author: www.byteblogs.com
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
