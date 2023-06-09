package com.aizuda.easy.retry.server.support;

import com.aizuda.easy.retry.server.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;

import java.util.Set;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-29 18:35
 */
public interface RetryContext<V> {

    /**
     * 获取重试数据
     *
     * @return
     */
    RetryTask getRetryTask();

    /**
     * 回调接果
     *
     * @param v 回调的数据
     */
    void setCallResult(V v);

    /**
     * 获取回调接果
     *
     * @return 回调的数据
     */
    V getCallResult();

    /**
     * 调用客户端发生异常信息
     *
     * @param e 异常
     */
    void setException(Exception e);

    /**
     * 是否发生异常
     *
     * @return
     */
    boolean hasException();

    /**
     * 等待策略
     *
     * @param waitStrategy {@link WaitStrategy} 等待策略
     */
    void setWaitStrategy(WaitStrategy waitStrategy);

    /**
     * 获取等待测试
     *
     * @return {@link WaitStrategy} 等待策略
     */
    WaitStrategy getWaitStrategy();

    /**
     * 获取分配的节点
     *
     * @return {@link RegisterNodeInfo} 注册的节点信息
     */
    RegisterNodeInfo getServerNode();

    /**
     * 获取场景黑名单
     *
     * @return 场景集合
     */
    Set<String> getSceneBlacklist();

}
