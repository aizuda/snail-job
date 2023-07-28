package com.aizuda.easy.retry.server.persistence.support.processor;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.support.RetryTaskAccess;
import com.aizuda.easy.retry.server.persistence.support.access.retry.AbstractRetryTaskAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 重试数据操作代理类
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-24 14:19
 */
@Component
public class RetryTaskAccessProcessor implements RetryTaskAccess<RetryTask> {

    @Autowired
    @Qualifier("mybatisRetryTaskAccess")
    private AbstractRetryTaskAccess retryTaskAccesses;

    /**
     * 批量查询重试任务
     */
    @Override
    public List<RetryTask> listAvailableTasks(String groupName, LocalDateTime lastAt, final Long lastId, Integer pageSize, Integer taskType) {
        return retryTaskAccesses.listAvailableTasks(groupName, lastAt, lastId, pageSize, taskType);
    }

    @Override
    public List<RetryTask> listRetryTaskByRetryCount(String groupName, Integer retryStatus){
        return retryTaskAccesses.listRetryTaskByRetryCount(groupName, retryStatus);
    }

    @Transactional
    @Override
    public int deleteByDelayLevel(String groupName, Integer delayLevel) {
        return retryTaskAccesses.deleteByDelayLevel(groupName, delayLevel);
    }

    @Transactional
    @Override
    public int updateRetryTask(RetryTask retryTask) {
        Assert.isTrue(1 == retryTaskAccesses.updateRetryTask(retryTask),
            () -> new EasyRetryServerException("更新重试任务失败"));

        return 1;
    }

    @Transactional
    @Override
    public int saveRetryTask(RetryTask retryTask){
        return retryTaskAccesses.saveRetryTask(retryTask);
    }

}
