package com.aizuda.snailjob.server.retry.task.support.result;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.RetryOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskFailAlarmEventDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.event.RetryTaskFailAlarmEvent;
import com.aizuda.snailjob.server.retry.task.support.handler.CallbackRetryTaskHandler;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.aizuda.snailjob.common.core.enums.RetryNotifySceneEnum.RETRY_TASK_FAIL_ERROR;
import static com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum.NOT_SUCCESS;

/**
 * <p>
 * 客户端执行重试失败、服务端调度时失败等场景导致的任务执行失败
 * </p>
 *
 * @author opensnail
 * @date 2025-02-02
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RetryFailureHandler extends AbstractRetryResultHandler {
    private final AccessTemplate accessTemplate;
    private final CallbackRetryTaskHandler callbackRetryTaskHandler;
    private final TransactionTemplate transactionTemplate;
    private final RetryTaskMapper retryTaskMapper;
    private final RetryMapper retryMapper;

    @Override
    public boolean supports(RetryResultContext context) {
        RetryOperationReasonEnum reasonEnum = RetryOperationReasonEnum.of(context.getOperationReason());
        return NOT_SUCCESS.contains(context.getTaskStatus())
                && RetryOperationReasonEnum.CLIENT_TRIGGER_RETRY_STOP.getReason() != reasonEnum.getReason();
    }

    @Override
    public void doHandler(RetryResultContext context) {
        RetrySceneConfig retrySceneConfig =
                accessTemplate.getSceneConfigAccess().getSceneConfigByGroupNameAndSceneName(
                        context.getGroupName(), context.getSceneName(), context.getNamespaceId());

        Retry retry = retryMapper.selectById(context.getRetryId());
        transactionTemplate.execute(status -> {

            Integer maxRetryCount;
            if (SyetemTaskTypeEnum.CALLBACK.getType().equals(retry.getTaskType())) {
                maxRetryCount = retrySceneConfig.getCbMaxCount();
            } else {
                maxRetryCount = retrySceneConfig.getMaxRetryCount();
            }

            if (maxRetryCount <= retry.getRetryCount() + 1) {
                retry.setRetryStatus(RetryStatusEnum.MAX_COUNT.getStatus());
                retry.setRetryCount(retry.getRetryCount() + 1);
                retry.setUpdateDt(LocalDateTime.now());
                retry.setDeleted(retry.getId());
                Assert.isTrue(1 == retryMapper.updateById(retry),
                        () -> new SnailJobServerException("更新重试任务失败. groupName:[{}]", retry.getGroupName()));
                // 创建一个回调任务
                callbackRetryTaskHandler.create(retry, retrySceneConfig);
            } else if (context.isIncrementRetryCount()) {
                retry.setRetryCount(retry.getRetryCount() + 1);
                retry.setUpdateDt(LocalDateTime.now());
                retry.setDeleted(retry.getId());
                Assert.isTrue(1 == retryMapper.updateById(retry),
                        () -> new SnailJobServerException("更新重试任务失败. groupName:[{}]", retry.getGroupName()));

            }

            RetryTask retryTask = new RetryTask();
            retryTask.setId(context.getRetryTaskId());
            retryTask.setTaskStatus(Optional.ofNullable(context.getTaskStatus()).orElse(RetryTaskStatusEnum.FAIL.getStatus()));
            retryTask.setOperationReason(Optional.ofNullable(context.getOperationReason()).orElse(RetryOperationReasonEnum.NONE.getReason()));
            Assert.isTrue(1 == retryTaskMapper.updateById(retryTask),
                    () -> new SnailJobServerException("更新重试任务失败. groupName:[{}]", retry.getGroupName()));

            RetryTaskFailAlarmEventDTO retryTaskFailAlarmEventDTO =
                    RetryTaskConverter.INSTANCE.toRetryTaskFailAlarmEventDTO(
                            retry, context.getExceptionMsg(), RETRY_TASK_FAIL_ERROR.getNotifyScene());
            SnailSpringContext.getContext().publishEvent(new RetryTaskFailAlarmEvent(retryTaskFailAlarmEventDTO));
            return null;

        });
    }
}
