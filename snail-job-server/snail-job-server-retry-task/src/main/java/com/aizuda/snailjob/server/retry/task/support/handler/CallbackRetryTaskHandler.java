package com.aizuda.snailjob.server.retry.task.support.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskLogConverter;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLog;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


/**
 * 回调数据处理器
 *
 * @author opensnail
 * @date 2023-06-04
 * @since 1.5.0
 */
@Component
@Slf4j
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
        if (!SyetemTaskTypeEnum.RETRY.getType().equals(retryTask.getTaskType())) {
            return;
        }

        RetryTask callbackRetryTask = RetryTaskConverter.INSTANCE.toRetryTask(retryTask);
        callbackRetryTask.setTaskType(SyetemTaskTypeEnum.CALLBACK.getType());
        callbackRetryTask.setId(null);
        callbackRetryTask.setUniqueId(generatorCallbackUniqueId(retryTask.getUniqueId()));
        callbackRetryTask.setRetryStatus(RetryStatusEnum.RUNNING.getStatus());
        callbackRetryTask.setRetryCount(0);
        callbackRetryTask.setCreateDt(LocalDateTime.now());
        callbackRetryTask.setUpdateDt(LocalDateTime.now());

        long triggerInterval = systemProperties.getCallback().getTriggerInterval();
        WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(WaitStrategyEnum.FIXED.getType());
        WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
        waitStrategyContext.setNextTriggerAt(DateUtils.toNowMilli());
        waitStrategyContext.setTriggerInterval(String.valueOf(triggerInterval));

        callbackRetryTask.setNextTriggerAt(DateUtils.toLocalDateTime(waitStrategy.computeTriggerTime(waitStrategyContext)));

        try {
            Assert.isTrue(1 == accessTemplate.getRetryTaskAccess()
                            .insert(callbackRetryTask.getGroupName(), callbackRetryTask.getNamespaceId(), callbackRetryTask),
                    () -> new SnailJobServerException("failed to report data"));
        } catch (DuplicateKeyException e) {
            log.warn("回调数据重复新增. [{}]", JsonUtil.toJsonString(retryTask));
            return;
        }

        // 初始化回调日志
        RetryTaskLog retryTaskLog = RetryTaskLogConverter.INSTANCE.toRetryTask(callbackRetryTask);
        // 记录重试日志
        retryTaskLog.setTaskType(SyetemTaskTypeEnum.CALLBACK.getType());
        retryTaskLog.setCreateDt(LocalDateTime.now());
        Assert.isTrue(1 == retryTaskLogMapper.insert(retryTaskLog),
                () -> new SnailJobServerException("新增重试日志失败"));

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
