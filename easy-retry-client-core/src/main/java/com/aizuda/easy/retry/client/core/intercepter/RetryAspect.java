package com.aizuda.easy.retry.client.core.intercepter;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.client.core.cache.GroupVersionCache;
import com.aizuda.easy.retry.client.core.config.XRetryProperties;
import com.aizuda.easy.retry.client.core.exception.XRetryClientException;
import com.aizuda.easy.retry.client.core.strategy.RetryStrategy;
import com.aizuda.easy.retry.client.core.annotation.Retryable;
import com.aizuda.easy.retry.client.core.retryer.RetryerResultContext;
import com.aizuda.easy.retry.common.core.alarm.Alarm;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.AltinAlarmFactory;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.common.core.enums.RetryResultStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 11:41
 */
@Aspect
@Component
@Slf4j
public class RetryAspect implements Ordered {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static String retryErrorMoreThresholdTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试组件异常</font>  \r\n" +
                    "> 名称:{}  \r\n" +
                    "> 时间:{}  \r\n" +
                    "> 异常:{}  \n"
            ;

    @Autowired
    @Qualifier("localRetryStrategies")
    private RetryStrategy retryStrategy;
    @Autowired
    private AltinAlarmFactory altinAlarmFactory;
    @Autowired
    private StandardEnvironment standardEnvironment;

    @Around("@annotation(com.aizuda.easy.retry.client.core.annotation.Retryable)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        String traceId = UUID.randomUUID().toString();

        LogUtils.debug(log,"进入 aop [{}]", traceId);
        Retryable retryable = getAnnotationParameter(point);
        String executorClassName = point.getTarget().getClass().getName();
        String methodEntrance = getMethodEntrance(retryable, executorClassName);
        if (StrUtil.isBlank(RetrySiteSnapshot.getMethodEntrance())) {
            RetrySiteSnapshot.setMethodEntrance(methodEntrance);
        }

        Throwable throwable = null;
        Object result = null;
        RetryerResultContext retryerResultContext;
        try {
            result = point.proceed();
        } catch (Throwable t) {
            throwable = t;
        } finally {

            LogUtils.debug(log,"开始进行重试 aop [{}]", traceId);
            // 入口则开始处理重试
            retryerResultContext = doHandlerRetry(point, traceId, retryable, executorClassName, methodEntrance, throwable);
        }

        LogUtils.debug(log,"aop 结果处理 traceId:[{}] result:[{}] ", traceId, result, throwable);

        // 若是重试完成了, 则判断是否返回重试完成后的数据
        if (Objects.nonNull(retryerResultContext)) {
            // 重试成功直接返回结果 若注解配置了isThrowException=false 则不抛出异常
            if (retryerResultContext.getRetryResultStatusEnum().getStatus().equals(RetryResultStatusEnum.SUCCESS.getStatus())
            || !retryable.isThrowException()) {
                return retryerResultContext.getResult();
            }
        }

        if (throwable != null) {
            throw throwable;
        } else {
            return result;
        }

    }

    private RetryerResultContext doHandlerRetry(ProceedingJoinPoint point, String traceId, Retryable retryable, String executorClassName, String methodEntrance, Throwable throwable) {

        if (!RetrySiteSnapshot.isMethodEntrance(methodEntrance)
                || RetrySiteSnapshot.isRunning()
                || Objects.isNull(throwable)
                // 重试流量不开启重试
                || RetrySiteSnapshot.isRetryFlow()
                // 下游响应不重试码，不开启重试
                || RetrySiteSnapshot.isRetryForStatusCode()
        ) {
            LogUtils.info(log, "校验不通过不开启重试 methodEntrance:[{}] isRunning:[{}] throwable:[{}] isRetryFlow:[{}] isRetryForStatusCode:[{}]" ,
                    !RetrySiteSnapshot.isMethodEntrance(methodEntrance),
                    RetrySiteSnapshot.isRunning(),
                    Objects.isNull(throwable),
                    RetrySiteSnapshot.isRetryFlow(),
                    RetrySiteSnapshot.isRetryForStatusCode()
            );
            return null;
        }

        return openRetry(point, traceId, retryable, executorClassName, throwable);
    }

    private RetryerResultContext openRetry(ProceedingJoinPoint point, String traceId, Retryable retryable, String executorClassName, Throwable throwable) {

        try {

            RetryerResultContext context = retryStrategy.openRetry(retryable.scene(), executorClassName, point.getArgs());
            LogUtils.info(log,"本地重试结果 message:[{}]", context);
            if (RetryResultStatusEnum.SUCCESS.getStatus().equals(context.getRetryResultStatusEnum().getStatus())) {
                LogUtils.debug(log, "aop 结果成功 traceId:[{}] result:[{}]", traceId, context.getResult());
            }

            return context;
        } catch (Exception e) {
            LogUtils.error(log,"重试组件处理异常，{}", e);

            // 预警
            sendMessage(e);

        } finally {
            RetrySiteSnapshot.removeAll();
        }

        return null;
    }

    private void sendMessage(Exception e) {

        try {
            ConfigDTO.Notify notifyAttribute = GroupVersionCache.getNotifyAttribute(NotifySceneEnum.CLIENT_COMPONENT_ERROR.getNotifyScene());
            if (Objects.nonNull(notifyAttribute)) {
                AlarmContext context = AlarmContext.build()
                        .text(retryErrorMoreThresholdTextMessageFormatter,
                                EnvironmentUtils.getActiveProfile(),
                                XRetryProperties.getGroup(),
                                LocalDateTime.now().format(formatter),
                                e.getMessage())
                        .title("重试组件异常:[{}]", XRetryProperties.getGroup())
                        .notifyAttribute(notifyAttribute.getNotifyAttribute());

                Alarm<AlarmContext> alarmType = altinAlarmFactory.getAlarmType(notifyAttribute.getNotifyType());
                alarmType.asyncSendMessage(context);
            }
        } catch (Exception e1) {
            LogUtils.error(log, "客户端发送组件异常告警失败", e1);
        }

    }

    public String getMethodEntrance(Retryable retryable, String executorClassName) {

        if (Objects.isNull(retryable)) {
            return StrUtil.EMPTY;
        }

        return retryable.scene().concat("_").concat(executorClassName);
    }

    private Retryable getAnnotationParameter(ProceedingJoinPoint point) {
        String methodName = point.getSignature().getName();
        Class<?> classTarget = point.getTarget().getClass();
        Class<?>[] par = ((MethodSignature) point.getSignature()).getParameterTypes();
        Method objMethod = null;
        try {
            objMethod = classTarget.getMethod(methodName, par);
        } catch (NoSuchMethodException e) {
            throw new XRetryClientException("注解配置异常：[{}}", methodName);
        }
        return objMethod.getAnnotation(Retryable.class);
    }

    @Override
    public int getOrder() {
        String order = standardEnvironment
            .getProperty("easy-retry.aop.order", String.valueOf(Ordered.HIGHEST_PRECEDENCE));
        return Integer.parseInt(order);
    }
}
