package com.x.retry.client.core.report;

import com.x.retry.client.core.BizIdGenerate;
import com.x.retry.client.core.Lifecycle;
import com.x.retry.client.core.RetryArgSerializer;
import com.x.retry.client.core.cache.RetryerInfoCache;
import com.x.retry.client.core.config.XRetryProperties;
import com.x.retry.client.core.exception.XRetryClientException;
import com.x.retry.client.core.intercepter.RetrySiteSnapshot;
import com.x.retry.client.core.retryer.RetryerInfo;
import com.x.retry.client.core.spel.SPELParamFunction;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.window.Listener;
import com.x.retry.common.core.window.SlidingWindow;
import com.x.retry.server.model.dto.RetryTaskDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-08 09:24
 */
@Component
public class ReportHandler implements Lifecycle {

    @Autowired
    @Qualifier("jacksonSerializer")
    private RetryArgSerializer retryArgSerializer;

    @Autowired
    private XRetryProperties xRetryProperties;

    private static SlidingWindow<RetryTaskDTO> slidingWindow;

    /**
     * 异步上报到服务端
     */
    public Boolean report(String scene, String targetClassName, Object[] args) {

        if (RetrySiteSnapshot.getStage().equals(RetrySiteSnapshot.EnumStage.REMOTE.getStage())) {
            LogUtils.info("已经上报成功，无需重复上报 scene:[{}] targetClassName:[{}] args:[{}]", scene, targetClassName, args);
            return Boolean.TRUE;
        }

        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, targetClassName);
        Method executorMethod = retryerInfo.getExecutorMethod();

        RetryTaskDTO retryTaskDTO = new RetryTaskDTO();
        String bizId = null;
        try {
            Class<? extends BizIdGenerate> bizIdGenerate = retryerInfo.getBizIdGenerate();
            BizIdGenerate generate = bizIdGenerate.newInstance();
            Method method = bizIdGenerate.getMethod("idGenerate", Object[].class);
            Object p = new Object[]{scene, targetClassName, args, executorMethod.getName()};
            bizId = (String) ReflectionUtils.invokeMethod(method, generate, p);
        } catch (Exception exception) {
            LogUtils.error("自定义id生成异常：{},{}", scene, args, exception);
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

        slidingWindow.add(retryTaskDTO);

        return true;
    }

    @Override
    public void start() {

        Listener<RetryTaskDTO> reportListener = new ReportListener();

        slidingWindow = SlidingWindow
                .Builder
                .<RetryTaskDTO>newBuilder()
                .withTotalThreshold(50)
                .withDuration(5, ChronoUnit.SECONDS)
                .withListener(reportListener)
                .build();
        slidingWindow.start();
    }

    @Override
    public void close() {
        if (Objects.nonNull(slidingWindow)) {
            slidingWindow.end();
        }
    }

}
