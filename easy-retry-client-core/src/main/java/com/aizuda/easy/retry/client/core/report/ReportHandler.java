package com.aizuda.easy.retry.client.core.report;

import com.aizuda.easy.retry.client.core.IdempotentIdGenerate;
import com.aizuda.easy.retry.client.core.RetryArgSerializer;
import com.aizuda.easy.retry.client.core.client.NettyClient;
import com.aizuda.easy.retry.client.core.client.proxy.RequestBuilder;
import com.aizuda.easy.retry.client.core.config.EasyRetryProperties;
import com.aizuda.easy.retry.client.core.Lifecycle;
import com.aizuda.easy.retry.client.core.cache.RetryerInfoCache;
import com.aizuda.easy.retry.client.core.exception.EasyRetryClientException;
import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;
import com.aizuda.easy.retry.client.core.spel.SPELParamFunction;
import com.aizuda.easy.retry.client.core.window.RetryLeapArray;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 上报服务端
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-08 09:24
 * @since 1.0.0
 */
@Component
@Slf4j
public class ReportHandler implements Lifecycle {

    @Autowired
    @Qualifier("XRetryJacksonSerializer")
    private RetryArgSerializer retryArgSerializer;

    public static final int SAMPLE_COUNT = 10;

    public static final int INTERVAL_IN_MS = 1000;

    private static ScheduledExecutorService dispatchService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "DispatchService"));

    public static RetryLeapArray slidingWindow = new RetryLeapArray(SAMPLE_COUNT, INTERVAL_IN_MS, new ReportListener());

    /**
     * 异步上报到服务端, 若当前处于远程重试阶段不会进行执行上报
     */
    public Boolean syncReport(String scene, String targetClassName, Object[] args, long timeout, TimeUnit unit) {

        if (RetrySiteSnapshot.getStage().equals(RetrySiteSnapshot.EnumStage.REMOTE.getStage())) {
            LogUtils.info(log,"已经上报成功，无需重复上报 scene:[{}] targetClassName:[{}] args:[{}]", scene, targetClassName, args);
            return Boolean.TRUE;
        }

        return syncReportWithForce(scene, targetClassName, args, timeout, unit);
    }

    /**
     * 异步上报到服务端, 若当前处于远程重试阶段不会进行执行上报
     */
    public Boolean syncReportWithForce(String scene, String targetClassName, Object[] args, long timeout, TimeUnit unit) {

        RetryTaskDTO retryTaskDTO = buildRetryTaskDTO(scene, targetClassName, args);

        NettyClient CLIENT = RequestBuilder.<NettyClient, NettyResult>newBuilder()
            .client(NettyClient.class)
            .async(Boolean.FALSE)
            .timeout(timeout)
            .unit(unit)
            .build();

        NettyResult result = CLIENT.reportRetryInfo(Arrays.asList(retryTaskDTO));
        LogUtils.debug(log, "Data report result result:[{}]", JsonUtil.toJsonString(result));

        return (Boolean) result.getData();
    }

    /**
     * 异步上报到服务端, 若当前处于远程重试阶段不会进行执行上报
     */
    public Boolean asyncReport(String scene, String targetClassName, Object[] args) {

        if (RetrySiteSnapshot.getStage().equals(RetrySiteSnapshot.EnumStage.REMOTE.getStage())) {
            LogUtils.info(log,"已经上报成功，无需重复上报 scene:[{}] targetClassName:[{}] args:[{}]", scene, targetClassName, args);
            return Boolean.TRUE;
        }

        RetryTaskDTO retryTaskDTO = buildRetryTaskDTO(scene, targetClassName, args);
        slidingWindow.currentWindow().value().add(retryTaskDTO);

        return true;
    }

    /**
     * 不需要校验强制当前是否处于远程重试阶段，强制异步上报到服务端
     */
    public Boolean asyncReportWithForce(String scene, String targetClassName, Object[] args) {

        RetryTaskDTO retryTaskDTO = buildRetryTaskDTO(scene, targetClassName, args);
        slidingWindow.currentWindow().value().add(retryTaskDTO);

        return true;
    }

    /**
     * 构建上报任务对象
     *
     * @param scene 场景
     * @param targetClassName 执行对象
     * @param args 参数
     * @return RetryTaskDTO 上报服务端对象
     */
    private RetryTaskDTO buildRetryTaskDTO(final String scene, final String targetClassName, final Object[] args) {
        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, targetClassName);
        Method executorMethod = retryerInfo.getMethod();

        RetryTaskDTO retryTaskDTO = new RetryTaskDTO();
        String idempotentId;
        try {
            Class<? extends IdempotentIdGenerate> idempotentIdGenerate = retryerInfo.getIdempotentIdGenerate();
            IdempotentIdGenerate generate = idempotentIdGenerate.newInstance();
            Method method = idempotentIdGenerate.getMethod("idGenerate", Object[].class);
            Object p = new Object[]{scene, targetClassName, args, executorMethod.getName()};
            idempotentId = (String) ReflectionUtils.invokeMethod(method, generate, p);
        } catch (Exception exception) {
            LogUtils.error(log, "幂等id生成异常：{},{}", scene, args, exception);
            throw new EasyRetryClientException("idempotentId生成异常：{},{}", scene, args);
        }

        String serialize = retryArgSerializer.serialize(args);
        retryTaskDTO.setIdempotentId(idempotentId);
        retryTaskDTO.setExecutorName(targetClassName);
        retryTaskDTO.setArgsStr(serialize);
        retryTaskDTO.setGroupName(EasyRetryProperties.getGroup());
        retryTaskDTO.setSceneName(scene);

        String bizNoSpel = retryerInfo.getBizNo();
        Function<Object[], String> spelParamFunction = new SPELParamFunction(bizNoSpel, executorMethod);
        retryTaskDTO.setBizNo(spelParamFunction.apply(args));
        return retryTaskDTO;
    }

    @Override
    public void start() {
        dispatchService.scheduleAtFixedRate(() -> {
            slidingWindow.currentWindow();
        }, INTERVAL_IN_MS, INTERVAL_IN_MS / SAMPLE_COUNT, TimeUnit.MILLISECONDS);

    }

    @Override
    public void close() {
        log.info("reportHandler about to shutdown");
        slidingWindow.currentWindow();
        log.info("reportHandler has been shutdown");
    }

}
