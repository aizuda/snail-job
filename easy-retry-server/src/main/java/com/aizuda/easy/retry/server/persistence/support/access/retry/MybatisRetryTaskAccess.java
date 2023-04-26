package com.aizuda.easy.retry.server.persistence.support.access.retry;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.util.Assert;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-23 16:03
 */
@Component
public class MybatisRetryTaskAccess extends AbstractRetryTaskAccess {

    @Autowired
    private RetryTaskMapper retryTaskMapper;

    @Override
    public boolean support(String groupId) {
        return true;
    }

    @Override
    public List<RetryTask> listAvailableTasks(String groupName, LocalDateTime lastAt, Integer pageSize) {
        setPartition(groupName);
        return retryTaskMapper.selectPage(new PageDTO<>(0, pageSize),
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus())
                        .eq(RetryTask::getGroupName, groupName).ge(RetryTask::getCreateDt, lastAt)
                        .orderByAsc(RetryTask::getCreateDt)).getRecords();
    }

    @Override
    public List<RetryTask> listRetryTaskByRetryCount(String groupName, Integer retryStatus) {
        setPartition(groupName);
        return retryTaskMapper.selectPage(new PageDTO<>(0, 1000),
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getRetryStatus, retryStatus)
                        .eq(RetryTask::getGroupName, groupName)).getRecords();
    }

    @Override
    public int deleteByDelayLevel(String groupName, Integer retryStatus) {
        setPartition(groupName);
        return retryTaskMapper.delete(new LambdaQueryWrapper<RetryTask>()
                .eq(RetryTask::getGroupName, groupName).eq(RetryTask::getRetryStatus, retryStatus));
    }

    @Override
    public int updateRetryTask(RetryTask retryTask) {
        setPartition(retryTask.getGroupName());
        retryTask.setUpdateDt(LocalDateTime.now());
        return retryTaskMapper.updateById(retryTask);
    }

    @Override
    public int saveRetryTask(RetryTask retryTask) {

        setPartition(retryTask.getGroupName());
        int i = retryTaskMapper.insert(retryTask);
        Assert.isTrue(1 == i, new EasyRetryServerException("同步重试数据失败", JsonUtil.toJsonString(retryTask)));
        return i;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
