package com.aizuda.easy.retry.client.core.client;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.client.core.IdempotentIdGenerate;
import com.aizuda.easy.retry.client.core.RetryArgSerializer;
import com.aizuda.easy.retry.client.core.cache.GroupVersionCache;
import com.aizuda.easy.retry.client.core.cache.RetryerInfoCache;
import com.aizuda.easy.retry.client.core.callback.RetryCompleteCallback;
import com.aizuda.easy.retry.client.core.exception.EasyRetryClientException;
import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.easy.retry.client.core.loader.EasyRetrySpiLoader;
import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;
import com.aizuda.easy.retry.client.core.retryer.RetryerResultContext;
import com.aizuda.easy.retry.client.core.serializer.JacksonSerializer;
import com.aizuda.easy.retry.client.core.strategy.RetryStrategy;
import com.aizuda.easy.retry.client.model.DispatchRetryDTO;
import com.aizuda.easy.retry.client.model.DispatchRetryResultDTO;
import com.aizuda.easy.retry.client.model.GenerateRetryIdempotentIdDTO;
import com.aizuda.easy.retry.client.model.RetryCallbackDTO;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.RetryResultStatusEnum;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.IdempotentIdContext;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Result<DispatchRetryResultDTO> dispatch(@RequestBody @Validated DispatchRetryDTO executeReqDto) {

        RetryerInfo retryerInfo = RetryerInfoCache.get(executeReqDto.getScene(), executeReqDto.getExecutorName());
        if (Objects.isNull(retryerInfo)) {
            throw new EasyRetryClientException("场景:[{}]配置不存在, 请检查您的场景和执行器是否存在", executeReqDto.getScene());
        }

        RetryArgSerializer retryArgSerializer = EasyRetrySpiLoader.loadRetryArgSerializer();

        Object[] deSerialize = null;
        try {
            deSerialize = (Object[]) retryArgSerializer.deSerialize(executeReqDto.getArgsStr(),
                retryerInfo.getExecutor().getClass(), retryerInfo.getMethod());
        } catch (JsonProcessingException e) {
            throw new EasyRetryClientException("参数解析异常", e);
        }

        DispatchRetryResultDTO executeRespDto = new DispatchRetryResultDTO();

        try {
            RetrySiteSnapshot.setAttemptNumber(executeReqDto.getRetryCount());

            RetryerResultContext retryerResultContext = retryStrategy.openRetry(executeReqDto.getScene(),
                executeReqDto.getExecutorName(), deSerialize);

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
    public Result callback(@RequestBody @Validated RetryCallbackDTO callbackDTO) {
        RetryerInfo retryerInfo = RetryerInfoCache.get(callbackDTO.getScene(), callbackDTO.getExecutorName());
        if (Objects.isNull(retryerInfo)) {
            throw new EasyRetryClientException("场景:[{}]配置不存在, 请检查您的场景和执行器是否存在", callbackDTO.getScene());
        }

        RetryArgSerializer retryArgSerializer = EasyRetrySpiLoader.loadRetryArgSerializer();

        Object[] deSerialize;
        try {
            deSerialize = (Object[]) retryArgSerializer.deSerialize(callbackDTO.getArgsStr(),
                retryerInfo.getExecutor().getClass(), retryerInfo.getMethod());
        } catch (JsonProcessingException e) {
            throw new EasyRetryClientException("参数解析异常", e);
        }

        try {
            // 以Spring Bean模式回调
            return doCallbackForSpringBean(callbackDTO, retryerInfo, deSerialize);
        } catch (NoSuchBeanDefinitionException e) {
            // 若不是SpringBean 则直接反射以普通类调用
            return doCallbackForOrdinaryClass(callbackDTO, retryerInfo, deSerialize);
        }
    }

    /**
     * 以普通类进行回调
     *
     * @param callbackDTO {@link RetryCallbackDTO} 服务端调度重试入参
     * @param retryerInfo {@link RetryerInfo} 定义重试场景的信息
     * @param deSerialize 参数信息
     * @return Result
     */
    private Result doCallbackForOrdinaryClass(RetryCallbackDTO callbackDTO, RetryerInfo retryerInfo,
        Object[] deSerialize) {
        Class<? extends RetryCompleteCallback> retryCompleteCallbackClazz = retryerInfo.getRetryCompleteCallback();

        try {
            RetryCompleteCallback retryCompleteCallback = retryCompleteCallbackClazz.newInstance();
            Method method;
            switch (Objects.requireNonNull(RetryStatusEnum.getByStatus(callbackDTO.getRetryStatus()))) {
                case FINISH:
                    method = retryCompleteCallbackClazz.getMethod("doSuccessCallback", String.class, String.class,
                        Object[].class);
                    break;
                case MAX_COUNT:
                    method = retryCompleteCallbackClazz.getMethod("doMaxRetryCallback", String.class, String.class,
                        Object[].class);
                    break;
                default:
                    throw new EasyRetryClientException("回调状态异常");
            }

            Assert.notNull(method, () -> new EasyRetryClientException("no such method"));
            ReflectionUtils.invokeMethod(method, retryCompleteCallback, retryerInfo.getScene(),
                retryerInfo.getExecutorClassName(), deSerialize);
            return new Result(1, "回调成功");
        } catch (Exception ex) {
            return new Result(0, ex.getMessage());
        }

    }

    /**
     * 以Spring Bean模式回调
     *
     * @param callbackDTO {@link RetryCallbackDTO} 服务端调度重试入参
     * @param retryerInfo {@link RetryerInfo} 定义重试场景的信息
     * @param deSerialize 参数信息
     * @return Result
     */
    private Result doCallbackForSpringBean(RetryCallbackDTO callbackDTO, RetryerInfo retryerInfo, Object[] deSerialize) {
        Class<? extends RetryCompleteCallback> retryCompleteCallbackClazz = retryerInfo.getRetryCompleteCallback();

        RetryCompleteCallback retryCompleteCallback = SpringContext.getBeanByType(retryCompleteCallbackClazz);
        switch (Objects.requireNonNull(RetryStatusEnum.getByStatus(callbackDTO.getRetryStatus()))) {
            case FINISH:
                retryCompleteCallback.doSuccessCallback(retryerInfo.getScene(), retryerInfo.getExecutorClassName(),
                    deSerialize);
                break;
            case MAX_COUNT:
                retryCompleteCallback.doMaxRetryCallback(retryerInfo.getScene(), retryerInfo.getExecutorClassName(),
                    deSerialize);
                break;
            default:
                throw new EasyRetryClientException("回调状态异常");
        }

        return new Result(1, "回调成功");
    }

    /**
     * 手动新增重试数据，模拟生成idempotentId
     *
     * @param generateRetryIdempotentIdDTO 生成idempotentId模型
     * @return idempotentId
     */
    @PostMapping("/generate/idempotent-id/v1")
    public Result<String> idempotentIdGenerate(
        @RequestBody @Validated GenerateRetryIdempotentIdDTO generateRetryIdempotentIdDTO) {

        String scene = generateRetryIdempotentIdDTO.getScene();
        String executorName = generateRetryIdempotentIdDTO.getExecutorName();
        String argsStr = generateRetryIdempotentIdDTO.getArgsStr();

        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, executorName);
        Assert.notNull(retryerInfo,
            () -> new EasyRetryClientException("重试信息不存在 scene:[{}] executorName:[{}]", scene, executorName));

        Method executorMethod = retryerInfo.getMethod();

        RetryArgSerializer retryArgSerializer = new JacksonSerializer();

        Object[] deSerialize = null;
        try {
            deSerialize = (Object[]) retryArgSerializer.deSerialize(argsStr, retryerInfo.getExecutor().getClass(),
                retryerInfo.getMethod());
        } catch (JsonProcessingException e) {
            throw new EasyRetryClientException("参数解析异常", e);
        }

        String idempotentId;
        try {
            Class<? extends IdempotentIdGenerate> idempotentIdGenerate = retryerInfo.getIdempotentIdGenerate();
            IdempotentIdGenerate generate = idempotentIdGenerate.newInstance();
            Method method = idempotentIdGenerate.getMethod("idGenerate", IdempotentIdContext.class);
            IdempotentIdContext idempotentIdContext = new IdempotentIdContext(scene, executorName, deSerialize,
                executorMethod.getName());
            idempotentId = (String) ReflectionUtils.invokeMethod(method, generate, idempotentIdContext);
        } catch (Exception exception) {
            LogUtils.error(log, "幂等id生成异常：{},{}", scene, argsStr, exception);
            throw new EasyRetryClientException("idempotentId生成异常：{},{}", scene, argsStr);
        }

        return new Result<>(idempotentId);
    }
}
