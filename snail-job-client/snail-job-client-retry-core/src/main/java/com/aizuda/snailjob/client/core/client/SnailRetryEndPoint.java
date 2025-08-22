package com.aizuda.snailjob.client.core.client;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.client.common.Lifecycle;
import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.annotation.SnailEndPoint;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.log.support.SnailJobLogManager;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.client.core.IdempotentIdGenerate;
import com.aizuda.snailjob.client.core.RetryArgSerializer;
import com.aizuda.snailjob.client.core.cache.FutureCache;
import com.aizuda.snailjob.client.core.cache.RetryerInfoCache;
import com.aizuda.snailjob.client.core.callback.future.CallbackTaskExecutorFutureCallback;
import com.aizuda.snailjob.client.core.callback.future.RetryTaskExecutorFutureCallback;
import com.aizuda.snailjob.client.core.context.CallbackContext;
import com.aizuda.snailjob.client.core.context.RemoteRetryContext;
import com.aizuda.snailjob.client.core.exception.RetryArgSerializeException;
import com.aizuda.snailjob.client.core.exception.SnailRetryClientException;
import com.aizuda.snailjob.client.core.executor.RemoteCallbackExecutor;
import com.aizuda.snailjob.client.core.executor.RemoteRetryExecutor;
import com.aizuda.snailjob.client.core.loader.SnailRetrySpiLoader;
import com.aizuda.snailjob.client.core.log.RetryLogMeta;
import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import com.aizuda.snailjob.client.core.timer.StopTaskTimerTask;
import com.aizuda.snailjob.client.core.timer.TimerManager;
import com.aizuda.snailjob.model.dto.DispatchRetryResultDTO;
import com.aizuda.snailjob.model.request.GenerateRetryIdempotentIdRequest;
import com.aizuda.snailjob.model.request.RetryArgsDeserializeRequest;
import com.aizuda.snailjob.model.request.RetryCallbackRequest;
import com.aizuda.snailjob.model.request.DispatchRetryRequest;
import com.aizuda.snailjob.model.request.StopRetryRequest;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.IdempotentIdContext;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.log.enums.LogTypeEnum;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.*;

/**
 * 服务端调调用客户端进行重试流量下发、配置变更通知等操作
 *
 * @author: opensnail
 * @date : 2022-03-09 16:33
 */
@SnailEndPoint
@Validated
@RequiredArgsConstructor
public class SnailRetryEndPoint implements Lifecycle {
    private final RemoteRetryExecutor remoteRetryExecutor;
    private final RemoteCallbackExecutor remoteCallbackExecutor;
    private final SnailJobProperties snailJobProperties;
    private ThreadPoolExecutor dispatcherThreadPool = null;

    /**
     * 服务端调度重试入口
     */
    @Mapping(path = RETRY_DISPATCH, method = RequestMethod.POST)
    public Result<Boolean> dispatch(@Valid DispatchRetryRequest request) {

        RemoteRetryContext retryContext = bulidRemoteRetryContext(request);

        RetryerInfo retryerInfo = RetryerInfoCache.get(request.getSceneName(), request.getExecutorName());
        if (Objects.isNull(retryerInfo)) {
            SnailJobLog.REMOTE.error("Scene [{}] configuration does not exist, please check if your scene and executor exist", request.getSceneName());
            return new Result<>(StatusEnum.NO.getStatus(), MessageFormat.format("Scene [{0}] configuration does not exist, please check if your scene and executor exist", request.getSceneName()));
        }

        // 初始化实时日志上下文
        initLogContext(retryContext);

        RetryArgSerializer retryArgSerializer = SnailRetrySpiLoader.loadRetryArgSerializer(request.getSerializerName());

        Object[] deSerialize;
        try {
            deSerialize = (Object[]) retryArgSerializer.deSerialize(request.getArgsStr(),
                    retryerInfo.getExecutor().getClass(), retryerInfo.getMethod());
        } catch (RetryArgSerializeException e) {
            SnailJobLog.REMOTE.error("Parameter parsing exception args:[{}]", request.getArgsStr(), e);
            return new Result<>(StatusEnum.NO.getStatus(), MessageFormat.format("Parameter parsing exception args:[{0}]", request.getArgsStr()));
        }

        retryContext.setDeSerialize(deSerialize);

        ListeningExecutorService decorator = MoreExecutors.listeningDecorator(dispatcherThreadPool);
        ListenableFuture<DispatchRetryResultDTO> submit = decorator.submit(() -> remoteRetryExecutor.doRetry(retryContext));

        FutureCache.addFuture(request.getRetryTaskId(), submit);
        Futures.addCallback(submit, new RetryTaskExecutorFutureCallback(retryContext), decorator);

        // 将任务添加到时间轮中，到期停止任务
        TimerManager.add(new StopTaskTimerTask(request.getRetryTaskId()), request.getExecutorTimeout(), TimeUnit.SECONDS);

        SnailJobLog.REMOTE.info(" Retry task:[{}] scheduled successfully.", request.getRetryTaskId());

        return new Result<>(Boolean.TRUE);
    }

    private static RemoteRetryContext bulidRemoteRetryContext(DispatchRetryRequest request) {
        RemoteRetryContext retryContext = new RemoteRetryContext();
        retryContext.setRetryTaskId(request.getRetryTaskId());
        retryContext.setRetryId(request.getRetryId());
        retryContext.setRetryCount(request.getRetryCount());
        retryContext.setArgsStr(request.getArgsStr());
        retryContext.setGroupName(request.getGroupName());
        retryContext.setNamespaceId(request.getNamespaceId());
        retryContext.setScene(request.getSceneName());
        retryContext.setExecutorName(request.getExecutorName());
        return retryContext;
    }

    private static void initLogContext(RemoteRetryContext context) {
        RetryLogMeta retryLogMeta = new RetryLogMeta();
        retryLogMeta.setGroupName(context.getGroupName());
        retryLogMeta.setNamespaceId(context.getNamespaceId());
        retryLogMeta.setRetryId(context.getRetryId());
        retryLogMeta.setRetryTaskId(context.getRetryTaskId());
        SnailJobLogManager.initLogInfo(retryLogMeta, LogTypeEnum.RETRY);
    }

    @Mapping(path = RETRY_CALLBACK, method = RequestMethod.POST)
    public Result<Boolean> callback(@Valid RetryCallbackRequest callbackDTO) {
        CallbackContext callbackContext = buildCallbackContext(callbackDTO);

        try {
            initLogContext(callbackContext);

            RetryerInfo retryerInfo = RetryerInfoCache.get(callbackDTO.getSceneName(), callbackDTO.getExecutorName());
            if (Objects.isNull(retryerInfo)) {
                SnailJobLog.REMOTE.error("Scene [{}] configuration does not exist, please check if your scene and executor exist", callbackDTO.getSceneName());
                return new Result<>(0, "Callback failed", Boolean.FALSE);
            }

            RetryArgSerializer retryArgSerializer = SnailRetrySpiLoader.loadRetryArgSerializer(callbackDTO.getSerializerName());

            Object[] deSerialize = (Object[]) retryArgSerializer.deSerialize(callbackDTO.getArgsStr(),
                    retryerInfo.getExecutor().getClass(), retryerInfo.getMethod());
            callbackContext.setDeSerialize(deSerialize);
            callbackContext.setRetryerInfo(retryerInfo);
        } catch (RetryArgSerializeException e) {
            SnailJobLog.REMOTE.error("Parameter parsing exception", e);
            return new Result<>(0, "Callback failed", Boolean.FALSE);
        }

        ListeningExecutorService decorator = MoreExecutors.listeningDecorator(dispatcherThreadPool);
        ListenableFuture<Boolean> submit = decorator.submit(() -> {
            remoteCallbackExecutor.doRetryCallback(callbackContext);
            return Boolean.TRUE;
        });

        FutureCache.addFuture(callbackDTO.getRetryTaskId(), submit);
        Futures.addCallback(submit, new CallbackTaskExecutorFutureCallback(callbackContext), decorator);

        // 将任务添加到时间轮中，到期停止任务
        TimerManager.add(new StopTaskTimerTask(callbackDTO.getRetryTaskId()), callbackDTO.getExecutorTimeout(), TimeUnit.SECONDS);

        SnailJobLog.REMOTE.info(" Callback task:[{}] scheduled successfully.", callbackDTO.getRetryTaskId());
        return new Result<>(Boolean.TRUE);
    }

    private static CallbackContext buildCallbackContext(RetryCallbackRequest callbackDTO) {
        CallbackContext callbackContext = new CallbackContext();
        callbackContext.setRetryTaskId(callbackDTO.getRetryTaskId());
        callbackContext.setRetryId(callbackDTO.getRetryId());
        callbackContext.setGroupName(callbackDTO.getGroupName());
        callbackContext.setNamespaceId(callbackDTO.getNamespaceId());
        callbackContext.setSceneName(callbackDTO.getSceneName());
        callbackContext.setRetryStatus(callbackDTO.getRetryStatus());
        return callbackContext;
    }

    private static void initLogContext(CallbackContext context) {
        // 初始化实时日志上下文
        RetryLogMeta retryLogMeta = new RetryLogMeta();
        retryLogMeta.setGroupName(context.getGroupName());
        retryLogMeta.setNamespaceId(context.getNamespaceId());
        retryLogMeta.setRetryTaskId(context.getRetryTaskId());
        retryLogMeta.setRetryId(context.getRetryId());
        SnailJobLogManager.initLogInfo(retryLogMeta, LogTypeEnum.RETRY);
    }


    /**
     * 手动新增重试数据，模拟生成idempotentId
     *
     * @param request 生成idempotentId模型
     * @return idempotentId
     */
    @Mapping(path = RETRY_GENERATE_IDEM_ID, method = RequestMethod.POST)
    public Result<String> idempotentIdGenerate(@Valid
                                               GenerateRetryIdempotentIdRequest request) {

        String scene = request.getScene();
        String executorName = request.getExecutorName();
        String argsStr = request.getArgsStr();

        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, executorName);
        Assert.notNull(retryerInfo,
                () -> new SnailRetryClientException("Retry information does not exist for scene:[{}] executorName:[{}]", scene, executorName));

        Method executorMethod = retryerInfo.getMethod();

        RetryArgSerializer retryArgSerializer = SnailRetrySpiLoader.loadRetryArgSerializer(request.getSerializerName());

        Object[] deSerialize;
        try {
            deSerialize = (Object[]) retryArgSerializer.deSerialize(argsStr, retryerInfo.getExecutor().getClass(),
                    retryerInfo.getMethod());
        } catch (RetryArgSerializeException e) {
            throw new SnailRetryClientException("Parameter parsing exception", e);
        }

        String idempotentId;
        try {
            Class<? extends IdempotentIdGenerate> idempotentIdGenerate = retryerInfo.getIdempotentIdGenerate();
            IdempotentIdGenerate generate = idempotentIdGenerate.newInstance();
            Method method = idempotentIdGenerate.getMethod("idGenerate", IdempotentIdContext.class);
            IdempotentIdContext idempotentIdContext = new IdempotentIdContext(scene, executorName, deSerialize,
                    executorMethod.getName(), argsStr);
            idempotentId = (String) ReflectionUtils.invokeMethod(method, generate, idempotentIdContext);
        } catch (Exception exception) {
            SnailJobLog.LOCAL.error("Idempotent ID generation exception: {}, {}", scene, argsStr, exception);
            throw new SnailRetryClientException("idempotentId generation exception: {}, {}", scene, argsStr);
        }

        return new Result<>(idempotentId);
    }

    @Mapping(path = RETRY_DESERIALIZE_ARGS, method = RequestMethod.POST)
    public Result<Object> deserialize(@Valid RetryArgsDeserializeRequest retryDeserializeRequest) {
        String scene = retryDeserializeRequest.getScene();
        String executorName = retryDeserializeRequest.getExecutorName();
        String argsStr = retryDeserializeRequest.getArgsStr();

        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, executorName);
        RetryArgSerializer retryArgSerializer = SnailRetrySpiLoader.loadRetryArgSerializer(retryDeserializeRequest.getSerializerName());

        Object result;
        try {
            result = retryArgSerializer.deSerialize(argsStr, retryerInfo.getExecutor().getClass(), retryerInfo.getMethod());
        } catch (RetryArgSerializeException e) {
            throw new SnailRetryClientException("Parameter parsing exception", e);
        }

        return new Result<>(result);
    }

    @Mapping(path = RETRY_STOP, method = RequestMethod.POST)
    public Result<Boolean> stop(@Valid StopRetryRequest stopRetryRequest) {
        FutureCache.remove(stopRetryRequest.getRetryTaskId());
        return new Result<>(Boolean.TRUE);
    }

    @Override
    public void start() {
        if (Objects.nonNull(dispatcherThreadPool)) {
            return;
        }

        SnailJobProperties.ThreadPoolConfig threadPoolConfig = snailJobProperties.getRetry().getDispatcherThreadPool();
        this.dispatcherThreadPool = new ThreadPoolExecutor(
                threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaximumPoolSize(),
                threadPoolConfig.getKeepAliveTime(),
                threadPoolConfig.getTimeUnit(),
                new LinkedBlockingQueue<>(threadPoolConfig.getQueueCapacity()),
                new CustomizableThreadFactory("snail-retry-dispatcher-"));
    }

    @Override
    public void close() {
        if (Objects.nonNull(dispatcherThreadPool)) {
            dispatcherThreadPool.shutdown();
        }
    }
}
