package com.aizuda.snailjob.server.retry.task.support;

import cn.hutool.core.lang.Pair;

/**
 * 重试过滤策略，为了判断哪些重试数据符合条件
 *
 * @author: opensnail
 * @date : 2021-11-29 18:45
 */
public interface FilterStrategy {

    /**
     * 过滤器执行器
     *
     * @param retryContext {@link RetryContext} 重试上下文
     * @return true- 符合重试条件 false- 不满足重试条件
     */
    Pair<Boolean /*是否符合条件*/, StringBuilder/*描述信息*/> filter(RetryContext retryContext);

    /**
     * 按照正序排列重试过滤器
     * 若相同则按照加入的顺序
     *
     * @return 排序的值
     */
    int order();
}
