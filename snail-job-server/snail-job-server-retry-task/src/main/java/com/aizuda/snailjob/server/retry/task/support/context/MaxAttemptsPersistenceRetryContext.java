package com.aizuda.snailjob.server.retry.task.support.context;

import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.retry.task.support.RetryContext;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import lombok.Data;
import lombok.Getter;

import java.util.Objects;
import java.util.Set;

/**
 * 持续重试上下文
 *
 * @author: opensnail
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

    /**
     * 场景配置
     */
    private RetrySceneConfig retrySceneConfig;

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

    @Override
    public Exception getException() {
        return exception;
    }

    @Override
    public RetrySceneConfig sceneConfig() {
        return retrySceneConfig;
    }

}
