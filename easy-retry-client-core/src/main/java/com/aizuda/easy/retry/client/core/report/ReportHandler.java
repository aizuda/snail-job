package com.aizuda.easy.retry.client.core.report;

import com.aizuda.easy.retry.client.core.BizIdGenerate;
import com.aizuda.easy.retry.client.core.RetryArgSerializer;
import com.aizuda.easy.retry.client.core.config.XRetryProperties;
import com.aizuda.easy.retry.client.core.Lifecycle;
import com.aizuda.easy.retry.client.core.cache.RetryerInfoCache;
import com.aizuda.easy.retry.client.core.exception.XRetryClientException;
import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;
import com.aizuda.easy.retry.client.core.spel.SPELParamFunction;
import com.aizuda.easy.retry.client.core.window.RetryLeapArray;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-08 09:24
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
     * 异步上报到服务端
     */
    public Boolean report(String scene, String targetClassName, Object[] args) {

        if (RetrySiteSnapshot.getStage().equals(RetrySiteSnapshot.EnumStage.REMOTE.getStage())) {
            LogUtils.info(log,"已经上报成功，无需重复上报 scene:[{}] targetClassName:[{}] args:[{}]", scene, targetClassName, args);
            return Boolean.TRUE;
        }

        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, targetClassName);
        Method executorMethod = retryerInfo.getExecutorMethod();

        RetryTaskDTO retryTaskDTO = new RetryTaskDTO();
        String bizId;
        try {
            Class<? extends BizIdGenerate> bizIdGenerate = retryerInfo.getBizIdGenerate();
            BizIdGenerate generate = bizIdGenerate.newInstance();
            Method method = bizIdGenerate.getMethod("idGenerate", Object[].class);
            Object p = new Object[]{scene, targetClassName, args, executorMethod.getName()};
            bizId = (String) ReflectionUtils.invokeMethod(method, generate, p);
        } catch (Exception exception) {
            LogUtils.error(log, "自定义id生成异常：{},{}", scene, args, exception);
            throw new XRetryClientException("bizId生成异常：{},{}", scene, args);
        }

        String serialize = retryArgSerializer.serialize(args);
        retryTaskDTO.setBizId(bizId);
        retryTaskDTO.setExecutorName(targetClassName);
        retryTaskDTO.setArgsStr(serialize);
        retryTaskDTO.setGroupName(XRetryProperties.getGroup());
        retryTaskDTO.setSceneName(scene);

        String bizNoSpel = retryerInfo.getBizNo();
        Function<Object[], String> spelParamFunction = new SPELParamFunction(bizNoSpel, executorMethod);
        retryTaskDTO.setBizNo(spelParamFunction.apply(args));

        slidingWindow.currentWindow().value().add(retryTaskDTO);

        return true;
    }

    @Override
    public void start() {
        dispatchService.scheduleAtFixedRate(() -> {
            slidingWindow.currentWindow();
        }, INTERVAL_IN_MS, INTERVAL_IN_MS / SAMPLE_COUNT, TimeUnit.MILLISECONDS);

    }

    @Override
    public void close() {
    }

}
