package com.aizuda.snailjob.client.core.client;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.annotation.SnailEndPoint;
import com.aizuda.snailjob.client.common.log.support.SnailJobLogManager;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.client.core.IdempotentIdGenerate;
import com.aizuda.snailjob.client.core.RetryArgSerializer;
import com.aizuda.snailjob.client.common.cache.GroupVersionCache;
import com.aizuda.snailjob.client.core.cache.RetryerInfoCache;
import com.aizuda.snailjob.client.core.callback.RetryCompleteCallback;
import com.aizuda.snailjob.client.core.exception.SnailRetryClientException;
import com.aizuda.snailjob.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.snailjob.client.core.loader.SnailRetrySpiLoader;
import com.aizuda.snailjob.client.core.log.RetryLogMeta;
import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import com.aizuda.snailjob.client.core.retryer.RetryerResultContext;
import com.aizuda.snailjob.client.core.serializer.JacksonSerializer;
import com.aizuda.snailjob.client.core.strategy.RetryStrategy;
import com.aizuda.snailjob.client.model.DispatchRetryDTO;
import com.aizuda.snailjob.client.model.DispatchRetryResultDTO;
import com.aizuda.snailjob.client.model.GenerateRetryIdempotentIdDTO;
import com.aizuda.snailjob.client.model.RetryCallbackDTO;
import com.aizuda.snailjob.common.core.context.SpringContext;
import com.aizuda.snailjob.common.core.enums.RetryResultStatusEnum;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.core.model.IdempotentIdContext;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.enums.LogTypeEnum;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

/**
 * 服务端调调用客户端进行重试流量下发、配置变更通知等操作
 *
 * @author: opensnail
 * @date : 2022-03-09 16:33
 */
@SnailEndPoint
public class SnailRetryEndPoint {

    @Autowired
    @Qualifier("remoteRetryStrategies")
    private RetryStrategy retryStrategy;

    /**
     * 服务端调度重试入口
     */
    @Mapping(path = "/retry/dispatch/v1", method = RequestMethod.POST)
    public Result<DispatchRetryResultDTO> dispatch(DispatchRetryDTO executeReqDto) {

        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<DispatchRetryDTO>> set = validator.validate(executeReqDto);
        for (final ConstraintViolation<DispatchRetryDTO> violation : set) {
            return new Result<>(violation.getMessage(), null);
        }

        RetryerInfo retryerInfo = RetryerInfoCache.get(executeReqDto.getScene(), executeReqDto.getExecutorName());
        if (Objects.isNull(retryerInfo)) {
            SnailJobLog.REMOTE.error("场景:[{}]配置不存在, 请检查您的场景和执行器是否存在", executeReqDto.getScene());
            throw new SnailRetryClientException("场景:[{}]配置不存在, 请检查您的场景和执行器是否存在",
                executeReqDto.getScene());
        }

        RetryArgSerializer retryArgSerializer = SnailRetrySpiLoader.loadRetryArgSerializer();

        Object[] deSerialize;
        try {
            deSerialize = (Object[]) retryArgSerializer.deSerialize(executeReqDto.getArgsStr(),
                retryerInfo.getExecutor().getClass(), retryerInfo.getMethod());
        } catch (JsonProcessingException e) {
            SnailJobLog.REMOTE.error("参数解析异常", e);
            throw new SnailRetryClientException("参数解析异常", e);
        }

        DispatchRetryResultDTO executeRespDto = new DispatchRetryResultDTO();

        try {
            RetrySiteSnapshot.setAttemptNumber(executeReqDto.getRetryCount());

            // 初始化实时日志上下文
            RetryLogMeta retryLogMeta = new RetryLogMeta();
            retryLogMeta.setGroupName(executeReqDto.getGroupName());
            retryLogMeta.setNamespaceId(executeReqDto.getNamespaceId());
            retryLogMeta.setUniqueId(executeReqDto.getUniqueId());
            SnailJobLogManager.initLogInfo(retryLogMeta, LogTypeEnum.RETRY);

            RetryerResultContext retryerResultContext = retryStrategy.openRetry(executeReqDto.getScene(),
                executeReqDto.getExecutorName(), deSerialize);

            if (RetrySiteSnapshot.isRetryForStatusCode()) {
                executeRespDto.setStatusCode(RetryResultStatusEnum.STOP.getStatus());
                executeRespDto.setExceptionMsg("下游标记不需要重试");
            } else {
                RetryResultStatusEnum retryResultStatusEnum = retryerResultContext.getRetryResultStatusEnum();
                if (Objects.isNull(retryResultStatusEnum)) {
                    retryResultStatusEnum = RetryResultStatusEnum.STOP;
                    retryerResultContext.setMessage("未获取重试状态. 任务停止");
                }

                executeRespDto.setStatusCode(retryResultStatusEnum.getStatus());
                executeRespDto.setExceptionMsg(retryerResultContext.getMessage());
            }

            executeRespDto.setIdempotentId(executeReqDto.getIdempotentId());
            executeRespDto.setUniqueId(executeReqDto.getUniqueId());
            if (Objects.nonNull(retryerResultContext.getResult())) {
                executeRespDto.setResultJson(JsonUtil.toJsonString(retryerResultContext.getResult()));
            }

            if (Objects.equals(RetryResultStatusEnum.SUCCESS.getStatus(), executeRespDto.getStatusCode())) {
                SnailJobLog.REMOTE.info("remote retry【SUCCESS】. count:[{}] result:[{}]", executeReqDto.getRetryCount(),
                    executeRespDto.getResultJson());
            } else if (Objects.equals(RetryResultStatusEnum.STOP.getStatus(), executeRespDto.getStatusCode())) {
                SnailJobLog.REMOTE.warn("remote retry 【STOP】. count:[{}] exceptionMsg:[{}]",
                    executeReqDto.getRetryCount(), executeRespDto.getExceptionMsg());
            } else if (Objects.equals(RetryResultStatusEnum.FAILURE.getStatus(), executeRespDto.getStatusCode())) {
                SnailJobLog.REMOTE.error("remote retry 【FAILURE】. count:[{}] ", executeReqDto.getRetryCount(),
                    retryerResultContext.getThrowable());
            } else {
                SnailJobLog.REMOTE.error("remote retry 【UNKNOWN】. count:[{}] result:[{}]", executeReqDto.getRetryCount(),
                    executeRespDto.getResultJson(), retryerResultContext.getThrowable());
            }

        } finally {
            RetrySiteSnapshot.removeAll();
            SnailJobLogManager.removeAll();
        }

        return new Result<>(executeRespDto);
    }

    /**
     * 同步版本
     */
    @Mapping(path = "/retry/sync/version/v1", method = RequestMethod.POST)
    public Result syncVersion(ConfigDTO configDTO) {
        GroupVersionCache.setConfig(configDTO);
        return new Result();
    }

    @Mapping(path = "/retry/callback/v1", method = RequestMethod.POST)
    public Result callback(RetryCallbackDTO callbackDTO) {

        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<RetryCallbackDTO>> set = validator.validate(callbackDTO);
        for (final ConstraintViolation<RetryCallbackDTO> violation : set) {
            return new Result<>(violation.getMessage(), null);
        }

        RetryerInfo retryerInfo = null;
        Object[] deSerialize = null;
        try {

            // 初始化实时日志上下文
            RetryLogMeta retryLogMeta = new RetryLogMeta();
            retryLogMeta.setGroupName(callbackDTO.getGroup());
            retryLogMeta.setNamespaceId(callbackDTO.getNamespaceId());
            retryLogMeta.setUniqueId(callbackDTO.getUniqueId());
            SnailJobLogManager.initLogInfo(retryLogMeta, LogTypeEnum.RETRY);

            retryerInfo = RetryerInfoCache.get(callbackDTO.getScene(), callbackDTO.getExecutorName());
            if (Objects.isNull(retryerInfo)) {
                SnailJobLog.REMOTE.error("场景:[{}]配置不存在, 请检查您的场景和执行器是否存在", callbackDTO.getScene());
                return new Result(0, "回调失败");
            }

            RetryArgSerializer retryArgSerializer = SnailRetrySpiLoader.loadRetryArgSerializer();

            deSerialize = (Object[]) retryArgSerializer.deSerialize(callbackDTO.getArgsStr(),
                retryerInfo.getExecutor().getClass(), retryerInfo.getMethod());

            // 以Spring Bean模式回调
            return doCallbackForSpringBean(callbackDTO, retryerInfo, deSerialize);
        } catch (JsonProcessingException e) {
            SnailJobLog.REMOTE.error("参数解析异常", e);
            return new Result(0, "回调失败");
        } catch (NoSuchBeanDefinitionException e) {
            // 若不是SpringBean 则直接反射以普通类调用
            return doCallbackForOrdinaryClass(callbackDTO, retryerInfo, deSerialize);
        } finally {
            SnailJobLogManager.removeAll();
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
                    throw new SnailRetryClientException("回调状态异常");
            }

            Assert.notNull(method, () -> new SnailRetryClientException("no such method"));
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
    private Result doCallbackForSpringBean(RetryCallbackDTO callbackDTO, RetryerInfo retryerInfo,
        Object[] deSerialize) {
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
                throw new SnailRetryClientException("回调状态异常");
        }

        return new Result(1, "回调成功");
    }

    /**
     * 手动新增重试数据，模拟生成idempotentId
     *
     * @param generateRetryIdempotentIdDTO 生成idempotentId模型
     * @return idempotentId
     */
    @Mapping(path = "/retry/generate/idempotent-id/v1", method = RequestMethod.POST)
    public Result<String> idempotentIdGenerate(
       GenerateRetryIdempotentIdDTO generateRetryIdempotentIdDTO) {

        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<GenerateRetryIdempotentIdDTO>> set = validator.validate(generateRetryIdempotentIdDTO);
        for (final ConstraintViolation<GenerateRetryIdempotentIdDTO> violation : set) {
            return new Result<>(violation.getMessage(), null);
        }

        String scene = generateRetryIdempotentIdDTO.getScene();
        String executorName = generateRetryIdempotentIdDTO.getExecutorName();
        String argsStr = generateRetryIdempotentIdDTO.getArgsStr();

        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, executorName);
        Assert.notNull(retryerInfo,
            () -> new SnailRetryClientException("重试信息不存在 scene:[{}] executorName:[{}]", scene, executorName));

        Method executorMethod = retryerInfo.getMethod();

        RetryArgSerializer retryArgSerializer = new JacksonSerializer();

        Object[] deSerialize = null;
        try {
            deSerialize = (Object[]) retryArgSerializer.deSerialize(argsStr, retryerInfo.getExecutor().getClass(),
                retryerInfo.getMethod());
        } catch (JsonProcessingException e) {
            throw new SnailRetryClientException("参数解析异常", e);
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
            SnailJobLog.LOCAL.error("幂等id生成异常：{},{}", scene, argsStr, exception);
            throw new SnailRetryClientException("idempotentId生成异常：{},{}", scene, argsStr);
        }

        return new Result<>(idempotentId);
    }
}
