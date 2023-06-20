package com.aizuda.easy.retry.client.core.intercepter;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.client.core.cache.GroupVersionCache;
import com.aizuda.easy.retry.client.core.cache.RetryerInfoCache;
import com.aizuda.easy.retry.client.core.config.EasyRetryProperties;
import com.aizuda.easy.retry.client.core.exception.EasyRetryClientException;
import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot.EnumStage;
import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;
import com.aizuda.easy.retry.client.core.strategy.RetryStrategy;
import com.aizuda.easy.retry.client.core.annotation.Retryable;
import com.aizuda.easy.retry.client.core.retryer.RetryerResultContext;
import com.aizuda.easy.retry.common.core.alarm.Alarm;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.EasyRetryAlarmFactory;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.common.core.enums.RetryResultStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.EasyRetryHeaders;
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
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
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
    private EasyRetryAlarmFactory easyRetryAlarmFactory;
    @Autowired
    private StandardEnvironment standardEnvironment;

    @Around("@annotation(com.aizuda.easy.retry.client.core.annotation.Retryable)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        String traceId = UUID.randomUUID().toString();

        LogUtils.debug(log,"Start entering the around method traceId:[{}]", traceId);
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

            LogUtils.debug(log,"Start retrying. traceId:[{}] scene:[{}] executorClassName:[{}]", traceId, retryable.scene(), executorClassName);
            // 入口则开始处理重试
            retryerResultContext = doHandlerRetry(point, traceId, retryable, executorClassName, methodEntrance, throwable);
        }

        LogUtils.debug(log,"Method return value is [{}]. traceId:[{}]", result, traceId, throwable);

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
                // 匹配异常信息
                || !validate(throwable, RetryerInfoCache.get(retryable.scene(), executorClassName))
        ) {
            if (!RetrySiteSnapshot.isMethodEntrance(methodEntrance)) {
                LogUtils.debug(log, "Non-method entry does not enable local retries. traceId:[{}] [{}]", traceId, RetrySiteSnapshot.getMethodEntrance());
            } else if (RetrySiteSnapshot.isRunning()) {
                LogUtils.debug(log, "Existing running retry tasks do not enable local retries. traceId:[{}] [{}]", traceId, EnumStage.valueOfStage(RetrySiteSnapshot.getStage()));
            } else if (Objects.isNull(throwable)) {
                LogUtils.debug(log, "No exception, no local retries. traceId:[{}]", traceId);
            } else if (RetrySiteSnapshot.isRetryFlow()) {
                LogUtils.debug(log, "Retry traffic does not enable local retries. traceId:[{}] [{}]", traceId,  RetrySiteSnapshot.getRetryHeader());
            } else if (RetrySiteSnapshot.isRetryForStatusCode()) {
                LogUtils.debug(log, "Existing exception retry codes do not enable local retries. traceId:[{}]", traceId);
            } else if(!validate(throwable, RetryerInfoCache.get(retryable.scene(), executorClassName))) {
                LogUtils.debug(log, "Exception mismatch. traceId:[{}]", traceId);
            } else {
                LogUtils.debug(log, "Unknown situations do not enable local retry scenarios. traceId:[{}]", traceId);
            }
            return null;
        }

        return openRetry(point, traceId, retryable, executorClassName, throwable);
    }

    private RetryerResultContext openRetry(ProceedingJoinPoint point, String traceId, Retryable retryable, String executorClassName, Throwable throwable) {

        try {

            // 标识重试流量
            initHeaders(retryable);

            RetryerResultContext context = retryStrategy.openRetry(retryable.scene(), executorClassName, point.getArgs());
            LogUtils.info(log,"local retry result. traceId:[{}] message:[{}]", traceId, context);
            if (RetryResultStatusEnum.SUCCESS.getStatus().equals(context.getRetryResultStatusEnum().getStatus())) {
                LogUtils.debug(log, "local retry successful. traceId:[{}] result:[{}]", traceId, context.getResult());
            }

            return context;
        } catch (Exception e) {
            LogUtils.error(log,"retry component handling exception，traceId:[{}]", traceId,  e);

            // 预警
            sendMessage(e);

        } finally {
            RetrySiteSnapshot.removeAll();
        }

        return null;
    }

    private void initHeaders(final Retryable retryable) {

        EasyRetryHeaders easyRetryHeaders = new EasyRetryHeaders();
        easyRetryHeaders.setEasyRetry(Boolean.TRUE);
        easyRetryHeaders.setEasyRetryId(IdUtil.getSnowflakeNextIdStr());
        easyRetryHeaders.setDdl(GroupVersionCache.getDdl(retryable.scene()));
        RetrySiteSnapshot.setRetryHeader(easyRetryHeaders);
    }

    private void sendMessage(Exception e) {

        try {
            ConfigDTO.Notify notifyAttribute = GroupVersionCache.getNotifyAttribute(NotifySceneEnum.CLIENT_COMPONENT_ERROR.getNotifyScene());
            if (Objects.nonNull(notifyAttribute)) {
                AlarmContext context = AlarmContext.build()
                        .text(retryErrorMoreThresholdTextMessageFormatter,
                                EnvironmentUtils.getActiveProfile(),
                                EasyRetryProperties.getGroup(),
                                LocalDateTime.now().format(formatter),
                                e.getMessage())
                        .title("retry component handling exception:[{}]", EasyRetryProperties.getGroup())
                        .notifyAttribute(notifyAttribute.getNotifyAttribute());

                Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(notifyAttribute.getNotifyType());
                alarmType.asyncSendMessage(context);
            }
        } catch (Exception e1) {
            LogUtils.error(log, "Client failed to send component exception alert.", e1);
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
            throw new EasyRetryClientException("注解配置异常：[{}}", methodName);
        }
        return objMethod.getAnnotation(Retryable.class);
    }

    @Override
    public int getOrder() {
        String order = standardEnvironment
            .getProperty("easy-retry.aop.order", String.valueOf(Ordered.HIGHEST_PRECEDENCE));
        return Integer.parseInt(order);
    }

    private boolean validate(Throwable throwable, RetryerInfo retryerInfo) {

        Set<Class<? extends Throwable>> exclude = retryerInfo.getExclude();
        Set<Class<? extends Throwable>> include = retryerInfo.getInclude();

        if (CollectionUtils.isEmpty(include) && CollectionUtils.isEmpty(exclude)) {
            return true;
        }

        for (Class<? extends Throwable> e : include) {
            if (e.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }

        if (!CollectionUtils.isEmpty(exclude)) {
            for (Class<? extends Throwable> e : exclude) {
                if (e.isAssignableFrom(throwable.getClass())) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }
}
