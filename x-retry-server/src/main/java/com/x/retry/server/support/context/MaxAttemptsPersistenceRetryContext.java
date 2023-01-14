package com.x.retry.server.support.context;

import com.x.retry.server.persistence.mybatis.po.RetryTask;
import com.x.retry.server.persistence.mybatis.po.ServerNode;
import com.x.retry.server.support.RetryContext;
import com.x.retry.server.support.WaitStrategy;
import lombok.Data;

import java.util.Set;

/**
 * 持续重试上下文
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-29 18:32
 */
@Data
public class MaxAttemptsPersistenceRetryContext<V> implements RetryContext<V> {

    /**
     * 通知客户端回调结果
     */
    private V callResult;

    /**
     * 等待策略
     */
    private WaitStrategy waitStrategy;

    /**
     * 当前重试数据
     */
    private RetryTask retryTask;

    /**
     * 目前处理关闭的场景
     */
    private Set<String> sceneBlacklist;

    /**
     * 需要调度的节点
     */
    private ServerNode serverNode;

    @Override
    public void setCallResult(V v) {
        this.callResult = v;
    }
}
