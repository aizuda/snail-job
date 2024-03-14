package com.aizuda.easy.retry.server.retry.task.support.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.HeadersEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.server.common.enums.TaskGeneratorSceneEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.handler.PostHttpRequestHandler;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import com.aizuda.easy.retry.server.retry.task.generator.task.TaskContext;
import com.aizuda.easy.retry.server.retry.task.generator.task.TaskGenerator;
import com.aizuda.easy.retry.server.retry.task.service.TaskContextConverter;
import com.github.rholder.retry.*;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

import static com.aizuda.easy.retry.common.core.constant.SystemConstants.HTTP_PATH.BATCH_REPORT;

/**
 * 处理上报数据
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:39
 * @since 1.0.0
 */
@Component
@Slf4j
public class ReportRetryInfoHttpRequestHandler extends PostHttpRequestHandler {

    @Autowired
    private List<TaskGenerator> taskGenerators;

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
    public String doHandler(String content, UrlQuery urlQuery, HttpHeaders  headers) {
       EasyRetryLog.LOCAL.debug("Batch Report Retry Data. content:[{}]", content);

        EasyRetryRequest retryRequest = JsonUtil.parseObject(content, EasyRetryRequest.class);
        Object[] args = retryRequest.getArgs();

        try {

            // 同步版本
            syncConfig(headers);

            TaskGenerator taskGenerator = taskGenerators.stream()
                    .filter(t -> t.supports(TaskGeneratorSceneEnum.CLIENT_REPORT.getScene()))
                    .findFirst().orElseThrow(() -> new EasyRetryServerException("没有匹配的任务生成器"));

            Assert.notEmpty(args, () -> new EasyRetryServerException("上报的数据不能为空. reqId:[{}]", retryRequest.getReqId()));
            List<RetryTaskDTO> retryTaskList = JsonUtil.parseList(JsonUtil.toJsonString(args[0]), RetryTaskDTO.class);

           EasyRetryLog.LOCAL.info("begin handler report data. <|>{}<|>", JsonUtil.toJsonString(retryTaskList));

            Set<String> set = retryTaskList.stream().map(RetryTaskDTO::getGroupName).collect(Collectors.toSet());
            Assert.isTrue(set.size() <= 1, () -> new EasyRetryServerException("批量上报数据,同一批次只能是相同的组. reqId:[{}]", retryRequest.getReqId()));

            Map<String, List<RetryTaskDTO>> map = retryTaskList.stream().collect(Collectors.groupingBy(RetryTaskDTO::getSceneName));

            Retryer<Object> retryer =  RetryerBuilder.newBuilder()
                    .retryIfException(throwable -> {
                        // 若是数据库异常则重试
                        if (throwable instanceof DuplicateKeyException
                            || throwable instanceof TransactionSystemException
                            || throwable instanceof ConcurrencyFailureException
                            || throwable instanceof IOException) {
                            return true;
                        }
                        return false;
                    })
                .withStopStrategy(StopStrategies.stopAfterAttempt(5))
                .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(final Attempt<V> attempt) {
                        if (attempt.hasException()) {
                            EasyRetryLog.LOCAL.error("数据上报发生异常执行重试. reqId:[{}] count:[{}]",
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

            return JsonUtil.toJsonString(new NettyResult(StatusEnum.YES.getStatus(), "Batch Retry Data Upload Processed Successfully", Boolean.TRUE, retryRequest.getReqId()));
        } catch (Exception e) {

            Throwable throwable = e;
            if (e.getClass().isAssignableFrom(RetryException.class)) {
                RetryException re = (RetryException) e;
                throwable = re.getLastFailedAttempt().getExceptionCause();
            }

            EasyRetryLog.LOCAL.error("Batch Report Retry Data Error. <|>{}<|>", args[0], throwable);
            return JsonUtil.toJsonString(new NettyResult(StatusEnum.YES.getStatus(), throwable.getMessage(), Boolean.FALSE, retryRequest.getReqId()));
        }
    }

    private void syncConfig(HttpHeaders  headers) {
        ConfigVersionSyncHandler syncHandler = SpringContext.getBeanByType(ConfigVersionSyncHandler.class);
        Integer clientVersion = headers.getInt(HeadersEnum.VERSION.getKey());
        String namespace = headers.getAsString(HeadersEnum.NAMESPACE.getKey());
        syncHandler.addSyncTask(headers.get(HeadersEnum.GROUP_NAME.getKey()), namespace, clientVersion);
    }
}
