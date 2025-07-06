package com.aizuda.snailjob.server.retry.task.support.request;

import  org.apache.pekko.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.client.model.request.DispatchCallbackResultRequest;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.retry.task.dto.RetryExecutorResultDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.REPORT_CALLBACK_RESULT;

/**
 * 上报回调执行的处理结果
 *
 * @author: opensnail
 * @date : 2022-03-07 16:39
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ReportCallbackResultHttpRequestHandler extends PostHttpRequestHandler {

    @Override
    public boolean supports(String path) {
        return REPORT_CALLBACK_RESULT.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    @Transactional
    public SnailJobRpcResult doHandler(String content, UrlQuery urlQuery, HttpHeaders headers) {
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();

        try {
            DispatchCallbackResultRequest request = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), DispatchCallbackResultRequest.class);
            RetryExecutorResultDTO executorResultDTO = RetryTaskConverter.INSTANCE.toRetryExecutorResultDTO(request);
            RetryTaskStatusEnum statusEnum = RetryTaskStatusEnum.getByStatus(request.getTaskStatus());
            Assert.notNull(statusEnum, () -> new SnailJobServerException("task status code is invalid"));
            executorResultDTO.setIncrementRetryCount(true);
            ActorRef actorRef = ActorGenerator.retryTaskExecutorResultActor();
            actorRef.tell(executorResultDTO, actorRef);

            return new SnailJobRpcResult(StatusEnum.YES.getStatus(), "Report dispatch result processed successfully", Boolean.TRUE, retryRequest.getReqId());
        } catch (Exception e) {
            return new SnailJobRpcResult(StatusEnum.YES.getStatus(), e.getMessage(), Boolean.FALSE, retryRequest.getReqId());
        }
    }

}
