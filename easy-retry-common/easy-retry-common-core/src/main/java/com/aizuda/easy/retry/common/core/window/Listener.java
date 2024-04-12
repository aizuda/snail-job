package com.aizuda.easy.retry.common.core.window;

import java.util.List;

/**
 * 滑动窗口监听器
 *
 *
 * @author: opensnail
 * @date : 2021-12-02 14:36
 */
public interface Listener<T> {

    /**
     * 数据监听器处理器
     *
     * @param list 到达窗口期的数据
     */
    void handler(List<T> list);
}
