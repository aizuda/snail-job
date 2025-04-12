package com.aizuda.snailjob.server.retry.task.support.prepare;

import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskPrepareDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryPrePareHandler;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.timer.RetryTimerContext;
import com.aizuda.snailjob.server.retry.task.support.timer.RetryTimerTask;
import com.aizuda.snailjob.server.retry.task.support.timer.RetryTimerWheel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-01-26
 */
@Component
@Slf4j
public class WaitRetryPrepareHandler implements RetryPrePareHandler  {

    @Override
    public boolean matches(Integer status) {
        return Objects.equals(RetryTaskStatusEnum.WAITING.getStatus(), status);
    }

    @Override
    public void handle(RetryTaskPrepareDTO prepare) {
        // 若时间轮中数据不存在则重新加入
        if (!RetryTimerWheel.isExisted(MessageFormat.format(RetryTimerTask.IDEMPOTENT_KEY_PREFIX, prepare.getRetryTaskId()))) {
            log.info("There are pending tasks and no retryTaskId:[{}] in the time wheel", prepare.getRetryTaskId());

            // 进入时间轮
            long delay = prepare.getNextTriggerAt() - DateUtils.toNowMilli();
            RetryTimerContext timerContext = RetryTaskConverter.INSTANCE.toRetryTimerContext(prepare);
            RetryTimerWheel.registerWithRetry(() -> new RetryTimerTask(timerContext), Duration.ofMillis(delay));
        }
    }
}
