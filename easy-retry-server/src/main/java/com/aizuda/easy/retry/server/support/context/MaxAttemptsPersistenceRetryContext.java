package com.aizuda.easy.retry.server.support.context;

import com.aizuda.easy.retry.server.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.support.RetryContext;
import com.aizuda.easy.retry.server.support.WaitStrategy;
import lombok.Data;
import lombok.Getter;

import java.util.Objects;
import java.util.Set;

/**
 * 持续重试上下文
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-29 18:32
 */
@Data
@Getter
public class MaxAttemptsPersistenceRetryContext<V> implements RetryContext<V> {

    /**
     * 通知客户端回调结果
     */
    private V callResult;

    /**
     * 异常信息
     */
    private Exception exception;

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
    private RegisterNodeInfo serverNode;

    @Override
    public void setCallResult(V v) {
        this.callResult = v;
    }

    @Override
    public void setException(Exception e) {
        this.exception = e;
    }

    @Override
    public boolean hasException() {
        return Objects.nonNull(exception);
    }
}
