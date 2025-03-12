package com.aizuda.snailjob.server.retry.task.support.handler;

import  org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.retry.task.dto.RequestRetryExecutorDTO;
import com.aizuda.snailjob.server.retry.task.dto.RetryExecutorResultDTO;
import com.aizuda.snailjob.server.retry.task.dto.TaskStopJobDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-02-10
 */
@Component
@RequiredArgsConstructor
public class RetryTaskStopHandler {
    /**
     * 执行停止任务
     *
     * @param stopJobDTO
     */
    public void stop(TaskStopJobDTO stopJobDTO) {

        RequestRetryExecutorDTO executorDTO = RetryTaskConverter.INSTANCE.toRealRetryExecutorDTO(stopJobDTO);
        ActorRef actorRef = ActorGenerator.stopRetryTaskActor();
        actorRef.tell(executorDTO, actorRef);

        // 更新结果为失败
        doHandleResult(stopJobDTO);
    }

    private static void doHandleResult(TaskStopJobDTO stopJobDTO) {
        if (!stopJobDTO.isNeedUpdateTaskStatus()) {
            return;
        }
        RetryExecutorResultDTO executorResultDTO = RetryTaskConverter.INSTANCE.toRetryExecutorResultDTO(stopJobDTO);
        executorResultDTO.setExceptionMsg(stopJobDTO.getMessage());
        executorResultDTO.setTaskStatus(RetryTaskStatusEnum.FAIL.getStatus());
        executorResultDTO.setOperationReason(stopJobDTO.getOperationReason());
        ActorRef actorRef = ActorGenerator.retryTaskExecutorResultActor();
        actorRef.tell(executorResultDTO, actorRef);
    }


}
