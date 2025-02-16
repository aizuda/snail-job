package com.aizuda.snailjob.server.retry.task.support.request;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.enums.TaskGeneratorSceneEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.model.dto.RetryTaskDTO;
import com.aizuda.snailjob.server.retry.task.support.generator.retry.TaskContext;
import com.aizuda.snailjob.server.retry.task.support.generator.retry.TaskGenerator;
import com.aizuda.snailjob.server.retry.task.service.TaskContextConverter;
import com.github.rholder.retry.*;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.BATCH_REPORT;

/**
 * 处理上报数据
 *
 * @author: opensnail
 * @date : 2022-03-07 16:39
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ReportRetryInfoHttpRequestHandler extends PostHttpRequestHandler {
    private final List<TaskGenerator> taskGenerators;

    @Override
    public boolean supports(String path) {
        return BATCH_REPORT.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    @Transactional
    public SnailJobRpcResult doHandler(String content, UrlQuery urlQuery, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("Batch Report Retry Data. content:[{}]", content);

        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();

        try {

            TaskGenerator taskGenerator = taskGenerators.stream()
                    .filter(t -> t.supports(TaskGeneratorSceneEnum.CLIENT_REPORT.getScene()))
                    .findFirst().orElseThrow(() -> new SnailJobServerException("没有匹配的任务生成器"));

            Assert.notEmpty(args, () -> new SnailJobServerException("上报的数据不能为空. reqId:[{}]", retryRequest.getReqId()));
            List<RetryTaskDTO> retryTaskList = JsonUtil.parseList(JsonUtil.toJsonString(args[0]), RetryTaskDTO.class);

            SnailJobLog.LOCAL.info("begin handler report data. <|>{}<|>", JsonUtil.toJsonString(retryTaskList));

            Set<String> set = StreamUtils.toSet(retryTaskList, RetryTaskDTO::getGroupName);
            Assert.isTrue(set.size() <= 1, () -> new SnailJobServerException("批量上报数据,同一批次只能是相同的组. reqId:[{}]", retryRequest.getReqId()));

            Map<String, List<RetryTaskDTO>> map = StreamUtils.groupByKey(retryTaskList, RetryTaskDTO::getSceneName);

            Retryer<Object> retryer = RetryerBuilder.newBuilder()
                    .retryIfException(throwable -> {
                        // 若是数据库异常则重试
                        return throwable instanceof DuplicateKeyException
                                || throwable instanceof TransactionSystemException
                                || throwable instanceof ConcurrencyFailureException
                                || throwable instanceof IOException;
                    })
                    .withStopStrategy(StopStrategies.stopAfterAttempt(5))
                    .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
                    .withRetryListener(new RetryListener() {
                        @Override
                        public <V> void onRetry(final Attempt<V> attempt) {
                            if (attempt.hasException()) {
                                SnailJobLog.LOCAL.error("数据上报发生异常执行重试. reqId:[{}] count:[{}]",
                                        retryRequest.getReqId(), attempt.getAttemptNumber(), attempt.getExceptionCause());
                            }
                        }
                    })
                    .build();

            String namespaceId = headers.getAsString(HeadersEnum.NAMESPACE.getKey());

            retryer.call(() -> {
                map.forEach(((sceneName, retryTaskDTOS) -> {
                    TaskContext taskContext = new TaskContext();
                    taskContext.setSceneName(sceneName);
                    taskContext.setNamespaceId(namespaceId);
                    taskContext.setGroupName(set.stream().findFirst().get());
                    taskContext.setTaskInfos(TaskContextConverter.INSTANCE.toTaskContextInfo(retryTaskDTOS));

                    // 生成任务
                    taskGenerator.taskGenerator(taskContext);
                }));

                return null;
            });

            return new SnailJobRpcResult(StatusEnum.YES.getStatus(), "Batch Retry Data Upload Processed Successfully", Boolean.TRUE, retryRequest.getReqId());
        } catch (Exception e) {

            Throwable throwable = e;
            if (e.getClass().isAssignableFrom(RetryException.class)) {
                RetryException re = (RetryException) e;
                throwable = re.getLastFailedAttempt().getExceptionCause();
            }

            SnailJobLog.LOCAL.error("Batch Report Retry Data Error. <|>{}<|>", args[0], throwable);
            return new SnailJobRpcResult(StatusEnum.YES.getStatus(), throwable.getMessage(), Boolean.FALSE, retryRequest.getReqId());
        }
    }

}
