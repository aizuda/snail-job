package com.aizuda.easy.retry.server.server.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.server.enums.TaskGeneratorScene;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import com.aizuda.easy.retry.server.service.convert.TaskContextConverter;
import com.aizuda.easy.retry.server.support.generator.TaskGenerator;
import com.aizuda.easy.retry.server.support.generator.task.TaskContext;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
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
        LogUtils.info(log, "Batch Report Retry Data. content:[{}]", content);

        EasyRetryRequest retryRequest = JsonUtil.parseObject(content, EasyRetryRequest.class);
        Object[] args = retryRequest.getArgs();

        try {
            TaskGenerator taskGenerator = taskGenerators.stream()
                    .filter(t -> t.supports(TaskGeneratorScene.CLIENT_REPORT.getScene()))
                    .findFirst().orElseThrow(() -> new EasyRetryServerException("没有匹配的任务生成器"));

            Assert.notEmpty(args, () -> new EasyRetryServerException("上报的数据不能为空. reqId:[{}]", retryRequest.getReqId()));
            List<RetryTaskDTO> retryTaskList = JsonUtil.parseList(JsonUtil.toJsonString(args[0]), RetryTaskDTO.class);

            LogUtils.info(log, "begin handler report data. <|>{}<|>", JsonUtil.toJsonString(retryTaskList));

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
                            LogUtils.error(log, "数据上报发生异常执行重试. reqId:[{}] count:[{}]",
                                retryRequest.getReqId(), attempt.getAttemptNumber(), attempt.getExceptionCause());
                        }
                    }
                })
                .build();

            retryer.call(() -> {
                map.forEach(((sceneName, retryTaskDTOS) -> {
                    TaskContext taskContext = new TaskContext();
                    taskContext.setSceneName(sceneName);
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

            LogUtils.error(log, "Batch Report Retry Data Error. <|>{}<|>", args[0], throwable);
            return JsonUtil.toJsonString(new NettyResult(StatusEnum.YES.getStatus(), throwable.getMessage(), Boolean.FALSE, retryRequest.getReqId()));
        }
    }
}
