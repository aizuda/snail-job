package com.aizuda.snailjob.client.core.report;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.client.core.Report;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.core.exception.SnailRetryClientException;
import com.aizuda.snailjob.common.core.expression.ExpressionEngine;
import com.aizuda.snailjob.client.core.IdempotentIdGenerate;
import com.aizuda.snailjob.client.core.RetryArgSerializer;
import com.aizuda.snailjob.client.core.cache.RetryerInfoCache;
import com.aizuda.snailjob.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.snailjob.client.core.loader.SnailRetrySpiLoader;
import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.core.model.IdempotentIdContext;
import com.aizuda.snailjob.server.model.dto.RetryTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * 上报抽象类
 *
 * @author opensnail
 * @date 2023-05-15
 * @since 1.3.0
 */
@Slf4j
public abstract class AbstractReport implements Report {

    @Override
    public boolean report(String scene, final String targetClassName, final Object[] params) {
        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, targetClassName);
        Assert.notNull(retryerInfo, () -> new SnailRetryClientException("retryerInfo is null"));

        if (RetrySiteSnapshot.getStage().equals(RetrySiteSnapshot.EnumStage.REMOTE.getStage()) && !retryerInfo.isForceReport()) {
           SnailJobLog.LOCAL.info("Successfully reported, no need to repeat reporting. scene:[{}] targetClassName:[{}] args:[{}]",
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
            SnailJobLog.LOCAL.error("幂等id生成异常：{},{}", scene, args, exception);
            throw new SnailRetryClientException("idempotentId生成异常：{},{}", scene, args);
        }

        RetryArgSerializer retryArgSerializer = SnailRetrySpiLoader.loadRetryArgSerializer();

        String serialize = retryArgSerializer.serialize(args);
        retryTaskDTO.setIdempotentId(idempotentId);
        retryTaskDTO.setExecutorName(targetClassName);
        retryTaskDTO.setArgsStr(serialize);
        retryTaskDTO.setGroupName(SnailJobProperties.getGroup());
        retryTaskDTO.setSceneName(scene);

        String expression = retryerInfo.getBizNo();
        ExpressionEngine expressionEngine = SnailRetrySpiLoader.loadExpressionEngine();
        retryTaskDTO.setBizNo((String) expressionEngine.eval(expression, args, executorMethod));
        return retryTaskDTO;
    }

}
