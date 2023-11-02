package com.aizuda.easy.retry.server.retry.task.support.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.util.DateUtil;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskConverter;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskLogConverter;
import com.aizuda.easy.retry.server.common.strategy.WaitStrategies;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


/**
 * 回调数据处理器
 *
 * @author www.byteblogs.com
 * @date 2023-06-04
 * @since 1.5.0
 */
@Component
public class CallbackRetryTaskHandler {

    private static final String CALLBACK_UNIQUE_ID_RULE = "{}_{}";

    @Autowired
    protected AccessTemplate accessTemplate;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Autowired
    private SystemProperties systemProperties;

    /**
     * 创建回调数据
     *
     * @param retryTask {@link RetryTask} 重试任务数据
     */
    @Transactional
    public void create(RetryTask retryTask) {
        if (!TaskTypeEnum.RETRY.getType().equals(retryTask.getTaskType())) {
            return;
        }

        RetryTask callbackRetryTask = RetryTaskConverter.INSTANCE.toRetryTask(retryTask);
        callbackRetryTask.setTaskType(TaskTypeEnum.CALLBACK.getType());
        callbackRetryTask.setId(null);
        callbackRetryTask.setUniqueId(generatorCallbackUniqueId(retryTask.getUniqueId()));
        callbackRetryTask.setRetryStatus(RetryStatusEnum.RUNNING.getStatus());
        callbackRetryTask.setRetryCount(0);
        callbackRetryTask.setCreateDt(LocalDateTime.now());
        callbackRetryTask.setUpdateDt(LocalDateTime.now());

        Long triggerTime = WaitStrategies.randomWait(1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS).computeTriggerTime(null);
        callbackRetryTask.setNextTriggerAt(DateUtil.toLocalDateTime(Instant.ofEpochMilli(triggerTime)));

        Assert.isTrue(1 == accessTemplate.getRetryTaskAccess()
                        .insert(callbackRetryTask.getGroupName(), callbackRetryTask),
                () -> new EasyRetryServerException("failed to report data"));

        // 初始化回调日志
        RetryTaskLog retryTaskLog = RetryTaskLogConverter.INSTANCE.toRetryTask(callbackRetryTask);
        // 记录重试日志
        retryTaskLog.setTaskType(TaskTypeEnum.CALLBACK.getType());
        retryTaskLog.setCreateDt(LocalDateTime.now());
        Assert.isTrue(1 == retryTaskLogMapper.insert(retryTaskLog),
                () -> new EasyRetryServerException("新增重试日志失败"));

    }

    /**
     * 生成回调数据
     *
     * @param uniqueId 重试任务uniqueId
     * @return 回调任务uniqueId
     */
    public String generatorCallbackUniqueId(String uniqueId) {
        // eg: CB_202307180949471
        FormattingTuple callbackUniqueId = MessageFormatter.arrayFormat(CALLBACK_UNIQUE_ID_RULE,
                new Object[]{systemProperties.getCallback().getPrefix(), uniqueId});

        return callbackUniqueId.getMessage();
    }

    /**
     * 获取重试任务uniqueId
     *
     * @param callbackTaskUniqueId 回调任务uniqueId
     * @return 重试任务uniqueId
     */
    public String getRetryTaskUniqueId(String callbackTaskUniqueId) {
        return callbackTaskUniqueId.substring(callbackTaskUniqueId.lastIndexOf(StrUtil.UNDERLINE) + 1);
    }

}
