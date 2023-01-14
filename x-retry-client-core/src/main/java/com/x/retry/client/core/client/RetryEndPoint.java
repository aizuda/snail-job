package com.x.retry.client.core.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.x.retry.client.core.RetryArgSerializer;
import com.x.retry.client.core.cache.RetryerInfoCache;
import com.x.retry.client.core.exception.XRetryClientException;
import com.x.retry.client.core.intercepter.RetrySiteSnapshot;
import com.x.retry.client.core.retryer.RetryerInfo;
import com.x.retry.client.core.retryer.RetryerResultContext;
import com.x.retry.client.core.serializer.JacksonSerializer;
import com.x.retry.client.core.strategy.RetryStrategy;
import com.x.retry.client.model.DispatchRetryDTO;
import com.x.retry.client.model.DispatchRetryResultDTO;
import com.x.retry.common.core.model.Result;
import com.x.retry.common.core.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-09 16:33
 */
@RestController
@RequestMapping("/retry")
public class RetryEndPoint {

    @Autowired
    @Qualifier("remoteRetryStrategies")
    private RetryStrategy retryStrategy;

    /**
     * 服务端调度重试入口
     */
    @PostMapping("/dispatch/v1")
   public Result<DispatchRetryResultDTO> dispatch(@RequestBody DispatchRetryDTO executeReqDto) {

        RetryerInfo retryerInfo = RetryerInfoCache.get(executeReqDto.getScene(), executeReqDto.getExecutorName());
        if (Objects.isNull(retryerInfo)) {
            throw new XRetryClientException("场景:[{}]配置不存在", executeReqDto.getScene());
        }

        RetryArgSerializer retryArgSerializer = new JacksonSerializer();

        Object[] deSerialize = null;
        try {
            deSerialize = (Object[]) retryArgSerializer.deSerialize(executeReqDto.getArgsStr(), retryerInfo.getExecutor().getClass(), retryerInfo.getExecutorMethod());
        } catch (JsonProcessingException e) {
            throw new XRetryClientException("参数解析异常", e);
        }

        DispatchRetryResultDTO executeRespDto = new DispatchRetryResultDTO();

        try {
            RetryerResultContext retryerResultContext = retryStrategy.openRetry(executeReqDto.getScene(), executeReqDto.getExecutorName(), deSerialize);
            executeRespDto.setStatusCode(retryerResultContext.getRetryResultStatusEnum().getStatus());
            executeRespDto.setBizId(executeReqDto.getBizId());
            if (Objects.nonNull(retryerResultContext.getResult())) {
                executeRespDto.setResultJson(JsonUtil.toJsonString(retryerResultContext.getResult()));
            }

            executeRespDto.setExceptionMsg(retryerResultContext.getMessage());
        } finally {
            RetrySiteSnapshot.removeAll();
        }

        return new Result<>(executeRespDto);
    }
}
