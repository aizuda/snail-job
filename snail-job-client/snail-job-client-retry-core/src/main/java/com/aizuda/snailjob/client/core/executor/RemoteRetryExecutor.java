package com.aizuda.snailjob.client.core.executor;

import com.aizuda.snailjob.client.common.log.support.SnailJobLogManager;
import com.aizuda.snailjob.client.core.context.RemoteRetryContext;
import com.aizuda.snailjob.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.snailjob.client.core.log.RetryLogMeta;
import com.aizuda.snailjob.client.core.retryer.RetryerResultContext;
import com.aizuda.snailjob.client.core.strategy.RetryStrategy;
import com.aizuda.snailjob.client.model.DispatchRetryResultDTO;
import com.aizuda.snailjob.common.core.enums.RetryResultStatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.log.enums.LogTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-02-11
 */
@Component
@RequiredArgsConstructor
public class RemoteRetryExecutor {
    private final RetryStrategy remoteRetryStrategies;

    /**
     * 执行远程重试
     *
     * @param context 重试上下文
     * @return
     */
    public DispatchRetryResultDTO doRetry(RemoteRetryContext context) {
        DispatchRetryResultDTO executeRespDto = new DispatchRetryResultDTO();
        executeRespDto.setRetryId(context.getRetryId());
        executeRespDto.setRetryTaskId(context.getRetryTaskId());
        executeRespDto.setNamespaceId(context.getNamespaceId());
        executeRespDto.setGroupName(context.getGroupName());
        executeRespDto.setSceneName(context.getScene());

        try {
            // 初始化实时日志上下文
            initLogContext(context);

            RetryerResultContext retryerResultContext = remoteRetryStrategies.openRetry(context.getScene(),
                    context.getExecutorName(), context.getDeSerialize());

            if (RetrySiteSnapshot.isRetryForStatusCode()) {
                executeRespDto.setStatusCode(RetryResultStatusEnum.STOP.getStatus());
                executeRespDto.setExceptionMsg("Downstream marks no need for retry");
            } else {
                RetryResultStatusEnum retryResultStatusEnum = retryerResultContext.getRetryResultStatusEnum();
                if (Objects.isNull(retryResultStatusEnum)) {
                    retryResultStatusEnum = RetryResultStatusEnum.STOP;
                    retryerResultContext.setMessage("No retry status obtained. Task stopped");
                }

                executeRespDto.setStatusCode(retryResultStatusEnum.getStatus());
                executeRespDto.setExceptionMsg(retryerResultContext.getMessage());
            }

            if (Objects.nonNull(retryerResultContext.getResult())) {
                executeRespDto.setResultJson(JsonUtil.toJsonString(retryerResultContext.getResult()));
            }

            Integer retryCount = context.getRetryCount() + 1;
            if (Objects.equals(RetryResultStatusEnum.SUCCESS.getStatus(), executeRespDto.getStatusCode())) {
                SnailJobLog.REMOTE.info("remote retry【SUCCESS】. retryTaskId:[{}] count:[{}] result:[{}]",
                        context.getRetryTaskId(), retryCount, executeRespDto.getResultJson());
            } else if (Objects.equals(RetryResultStatusEnum.STOP.getStatus(), executeRespDto.getStatusCode())) {
                SnailJobLog.REMOTE.warn("remote retry 【STOP】.retryTaskId:[{}] count:[{}]  exceptionMsg:[{}]",
                        context.getRetryTaskId(), retryCount, executeRespDto.getExceptionMsg());
            } else if (Objects.equals(RetryResultStatusEnum.FAILURE.getStatus(), executeRespDto.getStatusCode())) {
                SnailJobLog.REMOTE.error("remote retry 【FAILURE】. retryTaskId:[{}] count:[{}] ",
                        context.getRetryTaskId(), retryCount, retryerResultContext.getThrowable());
            } else {
                SnailJobLog.REMOTE.error("remote retry 【UNKNOWN】. retryTaskId:[{}] count:[{}] result:[{}]",
                        context.getRetryTaskId(), retryCount, executeRespDto.getResultJson(),
                        retryerResultContext.getThrowable());
            }

        } finally {
            RetrySiteSnapshot.removeAll();
            SnailJobLogManager.removeAll();
        }

        return executeRespDto;
    }

    private static void initLogContext(RemoteRetryContext context) {
        RetryLogMeta retryLogMeta = new RetryLogMeta();
        retryLogMeta.setGroupName(context.getGroupName());
        retryLogMeta.setNamespaceId(context.getNamespaceId());
        retryLogMeta.setRetryId(context.getRetryId());
        retryLogMeta.setRetryTaskId(context.getRetryTaskId());
        SnailJobLogManager.initLogInfo(retryLogMeta, LogTypeEnum.RETRY);
    }
}
