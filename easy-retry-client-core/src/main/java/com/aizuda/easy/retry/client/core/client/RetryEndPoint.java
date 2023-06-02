package com.aizuda.easy.retry.client.core.client;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.client.core.IdempotentIdGenerate;
import com.aizuda.easy.retry.client.core.RetryArgSerializer;
import com.aizuda.easy.retry.client.core.cache.GroupVersionCache;
import com.aizuda.easy.retry.client.core.cache.RetryerInfoCache;
import com.aizuda.easy.retry.client.core.callback.RetryCompleteCallback;
import com.aizuda.easy.retry.client.core.exception.EasyRetryClientException;
import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;
import com.aizuda.easy.retry.client.core.retryer.RetryerResultContext;
import com.aizuda.easy.retry.client.core.serializer.JacksonSerializer;
import com.aizuda.easy.retry.client.core.strategy.RetryStrategy;
import com.aizuda.easy.retry.client.model.GenerateRetryIdempotentIdDTO;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.RetryResultStatusEnum;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.aizuda.easy.retry.client.model.DispatchRetryDTO;
import com.aizuda.easy.retry.client.model.DispatchRetryResultDTO;
import com.aizuda.easy.retry.client.model.RetryCallbackDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 服务端调调用客户端进行重试流量下发、配置变更通知等操作
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-09 16:33
 */
@RestController
@RequestMapping("/retry")
@Slf4j
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
            throw new EasyRetryClientException("场景:[{}]配置不存在", executeReqDto.getScene());
        }

        RetryArgSerializer retryArgSerializer = new JacksonSerializer();

        Object[] deSerialize = null;
        try {
            deSerialize = (Object[]) retryArgSerializer.deSerialize(executeReqDto.getArgsStr(), retryerInfo.getExecutor().getClass(), retryerInfo.getMethod());
        } catch (JsonProcessingException e) {
            throw new EasyRetryClientException("参数解析异常", e);
        }

        DispatchRetryResultDTO executeRespDto = new DispatchRetryResultDTO();

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();
            request.setAttribute("attemptNumber", executeReqDto.getRetryCount());

            RetryerResultContext retryerResultContext = retryStrategy.openRetry(executeReqDto.getScene(), executeReqDto.getExecutorName(), deSerialize);

            if (RetrySiteSnapshot.isRetryForStatusCode()) {
                executeRespDto.setStatusCode(RetryResultStatusEnum.STOP.getStatus());

                // TODO 需要标记是哪个系统不需要重试
                executeRespDto.setExceptionMsg("下游标记不需要重试");
            } else {
                executeRespDto.setStatusCode(retryerResultContext.getRetryResultStatusEnum().getStatus());
                executeRespDto.setExceptionMsg(retryerResultContext.getMessage());
            }

            executeRespDto.setIdempotentId(executeReqDto.getIdempotentId());
            executeRespDto.setUniqueId(executeReqDto.getUniqueId());
            if (Objects.nonNull(retryerResultContext.getResult())) {
                executeRespDto.setResultJson(JsonUtil.toJsonString(retryerResultContext.getResult()));
            }


        } finally {
            RetrySiteSnapshot.removeAll();
        }

        return new Result<>(executeRespDto);
    }

    /**
     * 同步版本
     */
    @PostMapping("/sync/version/v1")
    public Result syncVersion(@RequestBody ConfigDTO configDTO) {
        GroupVersionCache.configDTO = configDTO;
        return new Result();
    }

    @PostMapping("/callback/v1")
    public Result callback(@RequestBody RetryCallbackDTO callbackDTO) {
        RetryerInfo retryerInfo = RetryerInfoCache.get(callbackDTO.getScene(), callbackDTO.getExecutorName());
        if (Objects.isNull(retryerInfo)) {
            throw new EasyRetryClientException("场景:[{}]配置不存在", callbackDTO.getScene());
        }

        RetryArgSerializer retryArgSerializer = new JacksonSerializer();

        Object[] deSerialize = null;
        try {
            deSerialize = (Object[]) retryArgSerializer.deSerialize(callbackDTO.getArgsStr(), retryerInfo.getExecutor().getClass(), retryerInfo.getMethod());
        } catch (JsonProcessingException e) {
            throw new EasyRetryClientException("参数解析异常", e);
        }

        Class<? extends RetryCompleteCallback> retryCompleteCallbackClazz = retryerInfo.getRetryCompleteCallback();
        RetryCompleteCallback retryCompleteCallback = SpringContext.getBeanByType(retryCompleteCallbackClazz);

        if (RetryStatusEnum.FINISH.getStatus().equals(callbackDTO.getRetryStatus())) {
            retryCompleteCallback.doSuccessCallback(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), deSerialize);
        }

        if (RetryStatusEnum.MAX_RETRY_COUNT.getStatus().equals(callbackDTO.getRetryStatus())) {
            retryCompleteCallback.doMaxRetryCallback(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), deSerialize);
        }

        return new Result();
    }

    /**
     * 手动新增重试数据，模拟生成idempotentId
     *
     * @param generateRetryIdempotentIdDTO 生成idempotentId模型
     * @return idempotentId
     */
    @PostMapping("/generate/idempotent-id/v1")
    public Result<String> idempotentIdGenerate(@RequestBody @Validated GenerateRetryIdempotentIdDTO generateRetryIdempotentIdDTO) {

        String scene = generateRetryIdempotentIdDTO.getScene();
        String executorName = generateRetryIdempotentIdDTO.getExecutorName();
        String argsStr = generateRetryIdempotentIdDTO.getArgsStr();

        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, executorName);
        Assert.notNull(retryerInfo, ()-> new EasyRetryClientException("重试信息不存在 scene:[{}] executorName:[{}]", scene, executorName));

        Method executorMethod = retryerInfo.getMethod();

        RetryArgSerializer retryArgSerializer = new JacksonSerializer();

        Object[] deSerialize = null;
        try {
            deSerialize = (Object[]) retryArgSerializer.deSerialize(argsStr, retryerInfo.getExecutor().getClass(), retryerInfo.getMethod());
        } catch (JsonProcessingException e) {
            throw new EasyRetryClientException("参数解析异常", e);
        }

        String idempotentId;
        try {
            Class<? extends IdempotentIdGenerate> idempotentIdGenerate = retryerInfo.getIdempotentIdGenerate();
            IdempotentIdGenerate generate = idempotentIdGenerate.newInstance();
            Method method = idempotentIdGenerate.getMethod("idGenerate", Object[].class);
            Object p = new Object[]{scene, executorName, deSerialize, executorMethod.getName()};
            idempotentId = (String) ReflectionUtils.invokeMethod(method, generate, p);
        } catch (Exception exception) {
            LogUtils.error(log, "幂等id生成异常：{},{}", scene, argsStr, exception);
            throw new EasyRetryClientException("idempotentId生成异常：{},{}", scene, argsStr);
        }

        return new Result<>(idempotentId);
    }
}
