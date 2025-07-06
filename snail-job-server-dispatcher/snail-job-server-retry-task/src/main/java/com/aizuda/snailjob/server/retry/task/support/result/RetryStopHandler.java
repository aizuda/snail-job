package com.aizuda.snailjob.server.retry.task.support.result;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.enums.RetryOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

import static com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum.STOP;

/**
 * <p>
 * 客户端触发停止重试指令, 重试挂起
 * </p>
 *
 * @author opensnail
 * @date 2025-02-02
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RetryStopHandler extends AbstractRetryResultHandler {
    private final TransactionTemplate transactionTemplate;
    private final RetryTaskMapper retryTaskMapper;
    private final RetryMapper retryMapper;

    @Override
    public boolean supports(RetryResultContext context) {
        RetryOperationReasonEnum reasonEnum = RetryOperationReasonEnum.of(context.getOperationReason());
        return STOP.getStatus().equals(context.getTaskStatus())
                && RetryOperationReasonEnum.CLIENT_TRIGGER_RETRY_STOP.getReason() == reasonEnum.getReason();
    }

    @Override
    public void doHandler(RetryResultContext context) {
        transactionTemplate.execute((status) -> {

            Retry retry = new Retry();
            retry.setId(context.getRetryId());
            retry.setRetryStatus(RetryStatusEnum.SUSPEND.getStatus());
            retry.setUpdateDt(LocalDateTime.now());
            retry.setRetryCount(retry.getRetryCount() + 1);
            Assert.isTrue(1 == retryMapper.updateById(retry),
                    () -> new SnailJobServerException("Update retry task failed. Group name:[{}]",
                            retry.getGroupName()));

            RetryTask retryTask = new RetryTask();
            retryTask.setId(context.getRetryTaskId());
            retryTask.setOperationReason(context.getOperationReason());
            retryTask.setTaskStatus(STOP.getStatus());
            Assert.isTrue(1 == retryTaskMapper.updateById(retryTask),
                    () -> new SnailJobServerException("Update retry task failed. Group name:[{}]", retry.getGroupName()));

            return null;
        });
    }
}
