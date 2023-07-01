package com.aizuda.easy.retry.client.core.report;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.client.core.IdempotentIdGenerate;
import com.aizuda.easy.retry.client.core.Report;
import com.aizuda.easy.retry.client.core.RetryArgSerializer;
import com.aizuda.easy.retry.client.core.cache.RetryerInfoCache;
import com.aizuda.easy.retry.client.core.config.EasyRetryProperties;
import com.aizuda.easy.retry.client.core.exception.EasyRetryClientException;
import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;
import com.aizuda.easy.retry.client.core.spel.SPELParamFunction;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.IdempotentIdContext;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * 上报抽象类
 *
 * @author www.byteblogs.com
 * @date 2023-05-15
 * @since 1.3.0
 */
@Slf4j
public abstract class AbstractReport implements Report {

    @Autowired
    @Qualifier("easyRetryJacksonSerializer")
    private RetryArgSerializer retryArgSerializer;

    protected static final int SAMPLE_COUNT = 10;

    protected static final int INTERVAL_IN_MS = 1000;

    @Override
    public boolean report(String scene, final String targetClassName, final Object[] params) {
        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, targetClassName);
        Assert.notNull(retryerInfo, () -> new EasyRetryClientException("retryerInfo is null"));

        if (RetrySiteSnapshot.getStage().equals(RetrySiteSnapshot.EnumStage.REMOTE.getStage()) && !retryerInfo.isForceReport()) {
            LogUtils.info(log, "Successfully reported, no need to repeat reporting. scene:[{}] targetClassName:[{}] args:[{}]",
                    retryerInfo.getScene(), retryerInfo.getExecutorClassName(), params);
            return Boolean.TRUE;
        }

        return doReport(retryerInfo, params);
    }

    public abstract boolean doReport(RetryerInfo retryerInfo, Object[] params);

    /**
     * 构建上报任务对象
     *
     * @param scene           场景
     * @param targetClassName 执行对象
     * @param args            参数
     * @return RetryTaskDTO 上报服务端对象
     */
    protected RetryTaskDTO buildRetryTaskDTO(final String scene, final String targetClassName, final Object[] args) {
        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, targetClassName);
        Method executorMethod = retryerInfo.getMethod();

        RetryTaskDTO retryTaskDTO = new RetryTaskDTO();
        String idempotentId;
        try {
            Class<? extends IdempotentIdGenerate> idempotentIdGenerate = retryerInfo.getIdempotentIdGenerate();
            IdempotentIdGenerate generate = idempotentIdGenerate.newInstance();
            Method method = idempotentIdGenerate.getMethod("idGenerate", IdempotentIdContext.class);
            IdempotentIdContext idempotentIdContext = new IdempotentIdContext(scene, targetClassName, args, executorMethod.getName());
            idempotentId = (String) ReflectionUtils.invokeMethod(method, generate, idempotentIdContext);
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

}
