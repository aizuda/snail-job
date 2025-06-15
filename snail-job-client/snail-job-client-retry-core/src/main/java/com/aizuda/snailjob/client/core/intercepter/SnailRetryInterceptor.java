package com.aizuda.snailjob.client.core.intercepter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.cache.GroupVersionCache;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.core.MethodResult;
import com.aizuda.snailjob.client.core.RetryCondition;
import com.aizuda.snailjob.client.core.annotation.Propagation;
import com.aizuda.snailjob.client.core.annotation.Retryable;
import com.aizuda.snailjob.client.core.cache.RetryerInfoCache;
import com.aizuda.snailjob.client.core.exception.RetryConditionException;
import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import com.aizuda.snailjob.client.core.retryer.RetryerResultContext;
import com.aizuda.snailjob.client.core.strategy.RetryStrategy;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.alarm.SnailJobAlarmFactory;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.RetryNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.RetryResultStatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobHeaders;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.common.core.util.NetUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;
import com.aizuda.snailjob.server.model.dto.ConfigDTO.Notify.Recipient;
import com.google.common.base.Defaults;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.AfterAdvice;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.YYYY_MM_DD_HH_MM_SS;

/**
 * @author opensnail
 * @date 2023-08-23
 */
@Slf4j
public class SnailRetryInterceptor implements MethodInterceptor, AfterAdvice, Serializable, Ordered {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
    private static final String retryErrorMoreThresholdTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试组件异常</font>  \n" +
                    "> IP:{}  \n" +
                    "> 空间ID:{}  \n" +
                    "> 名称:{}  \n" +
                    "> 时间:{}  \n" +
                    "> 异常:{}  \n";

    private final RetryStrategy retryStrategy;
    private final int order;

    public SnailRetryInterceptor(int order, RetryStrategy localRetryStrategies) {
        this.order = order;
        this.retryStrategy = localRetryStrategies;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        String traceId = UUID.randomUUID().toString();

        SnailJobLog.LOCAL.debug("Start entering the around method traceId:[{}]", traceId);
        Retryable retryable = getAnnotationParameter(invocation.getMethod());
        String executorClassName = invocation.getThis().getClass().getName();
        String methodEntrance = getMethodEntrance(retryable, executorClassName);

        if (Propagation.REQUIRES_NEW.equals(retryable.propagation())) {
            // 如果已经是入口了就不需要继续添加入口了
            if (!RetrySiteSnapshot.isMethodEntrance(methodEntrance)) {
                // 这里需要挂起外部重试的内存的信息
                if (RetrySiteSnapshot.isRunning()
                        && RetrySiteSnapshot.getStage() == RetrySiteSnapshot.EnumStage.LOCAL.getStage()) {
                    RetrySiteSnapshot.suspend();
                    // 清除线程信息
                    RetrySiteSnapshot.removeAll();
                }
                // 设置新的内容信息
                RetrySiteSnapshot.setMethodEntrance(methodEntrance);
            }
        } else if (!RetrySiteSnapshot.existedMethodEntrance()) {
            RetrySiteSnapshot.setMethodEntrance(methodEntrance);
        } else {
            SnailJobLog.LOCAL.debug("No need to set entrance signs:[{}]", traceId);
        }

        Throwable throwable = null;
        Object result = null;
        RetryerResultContext retryerResultContext;
        try {
            result = invocation.proceed();
            if (retryIf(result, retryable, traceId, executorClassName)) {
                throw new RetryConditionException("The current return value needs to be retried");
            }
        } catch (Throwable t) {
            throwable = t;
        } finally {

            SnailJobLog.LOCAL.debug("Start retrying. traceId:[{}] scene:[{}] executorClassName:[{}]", traceId,
                    retryable.scene(), executorClassName);
            // 入口则开始处理重试
            retryerResultContext = doHandlerRetry(invocation, traceId, retryable, executorClassName, methodEntrance,
                    throwable);
        }

        SnailJobLog.LOCAL.debug("Method return value is [{}]. traceId:[{}]", result, traceId, throwable);

        // 若是重试完成了, 则判断是否返回重试完成后的数据
        if (Objects.nonNull(retryerResultContext)) {
            // 重试成功直接返回结果
            if (retryerResultContext.getRetryResultStatusEnum().getStatus()
                    .equals(RetryResultStatusEnum.SUCCESS.getStatus())) {
                return retryerResultContext.getResult();
            }
            // 若注解配置了isThrowException=false 则不抛出异常
            else if (!retryable.isThrowException()) {
                Method method = invocation.getMethod();

                // 如果存在用户自定义MethodResult，返回用户自定义值
                try {
                    Class<? extends MethodResult> methodResultClass = retryable.methodResult();
                    if (Objects.nonNull(methodResultClass) && !methodResultClass.isAssignableFrom(MethodResult.NoMethodResult.class)) {
                        MethodResult methodResult = methodResultClass.getDeclaredConstructor().newInstance();
                        Object resultObj = methodResult.result(retryable.scene(), executorClassName, invocation.getArguments(), throwable);
                        if (Objects.nonNull(resultObj) && method.getReturnType().isAssignableFrom(resultObj.getClass())) {
                            return resultObj;
                        }
                    }
                } catch (Throwable e) {
                    SnailJobLog.LOCAL.debug("Get method result is error. traceId:[{}] scene:[{}] executorClassName:[{}]", traceId, retryable.scene(), executorClassName, throwable);
                }

                // 若返回值是NULL且是基本类型则返回默认值
                if (Objects.isNull(retryerResultContext.getResult()) && method.getReturnType().isPrimitive()) {
                    return Defaults.defaultValue(method.getReturnType());
                }
                return retryerResultContext.getResult();
            }
        }

        // 无需开启重试的场景，需要清除缓存信息
        if ((RetrySiteSnapshot.isMethodEntrance(methodEntrance) && !RetrySiteSnapshot.isRunning())) {
            RetrySiteSnapshot.removeAll();
        }

        if (throwable != null) {
            throw throwable;
        } else {
            return result;
        }

    }

    private boolean retryIf(Object result, Retryable retryable, String traceId, String executorClassName) {
        try {
            Class<? extends RetryCondition> retryConditionClass = retryable.retryIf();
            if (Objects.nonNull(retryConditionClass) && !retryConditionClass.isAssignableFrom(RetryCondition.NoRetry.class)) {
                RetryCondition retryCondition = retryConditionClass.getDeclaredConstructor().newInstance();
                return retryCondition.shouldRetry(result);
            }
        } catch (Throwable e) {
            SnailJobLog.LOCAL.debug("Retry condition fail. traceId:[{}] scene:[{}] executorClassName:[{}]", traceId,
                    retryable.scene(), executorClassName);
        }
        return false;
    }


    private RetryerResultContext doHandlerRetry(MethodInvocation invocation, String traceId, Retryable retryable,
                                                String executorClassName, String methodEntrance, Throwable throwable) {

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
                SnailJobLog.LOCAL.debug("Non-method entry does not enable local retries. traceId:[{}] [{}]", traceId,
                        RetrySiteSnapshot.getMethodEntrance());
            } else if (RetrySiteSnapshot.isRunning()) {
                SnailJobLog.LOCAL.debug("Existing running retry tasks do not enable local retries. traceId:[{}] [{}]",
                        traceId, RetrySiteSnapshot.EnumStage.valueOfStage(RetrySiteSnapshot.getStage()));
            } else if (Objects.isNull(throwable)) {
                SnailJobLog.LOCAL.debug("No exception, no local retries. traceId:[{}]", traceId);
            } else if (RetrySiteSnapshot.isRetryFlow()) {
                SnailJobLog.LOCAL.debug("Retry traffic does not enable local retries. traceId:[{}] [{}]", traceId,
                        RetrySiteSnapshot.getRetryHeader());
            } else if (RetrySiteSnapshot.isRetryForStatusCode()) {
                SnailJobLog.LOCAL.debug("Existing exception retry codes do not enable local retries. traceId:[{}]",
                        traceId);
            } else if (!validate(throwable, RetryerInfoCache.get(retryable.scene(), executorClassName))) {
                SnailJobLog.LOCAL.debug("Exception mismatch. traceId:[{}]", traceId);
            } else {
                SnailJobLog.LOCAL.debug("Unknown situations do not enable local retry scenarios. traceId:[{}]",
                        traceId);
            }
            return null;
        }

        return openRetry(invocation, traceId, retryable, executorClassName, throwable);
    }

    private RetryerResultContext openRetry(MethodInvocation point, String traceId, Retryable retryable,
                                           String executorClassName, Throwable throwable) {

        try {

            // 标识重试流量
            initHeaders(retryable);

            RetryerResultContext context = retryStrategy.openRetry(retryable.scene(), executorClassName,
                    point.getArguments());
            if (RetryResultStatusEnum.SUCCESS.getStatus().equals(context.getRetryResultStatusEnum().getStatus())) {
                SnailJobLog.LOCAL.debug("local retry successful. traceId:[{}] result:[{}]", traceId,
                        context.getResult());
            } else {
                SnailJobLog.LOCAL.debug("local retry result. traceId:[{}] throwable:[{}]", traceId,
                        context.getThrowable());
            }

            return context;
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("retry component handling exception，traceId:[{}]", traceId, e);

            // 预警
            sendMessage(e);

        } finally {
            // 清除当前重试的信息
            RetrySiteSnapshot.removeAll();
            // 还原挂起的信息
            RetrySiteSnapshot.restore();

        }

        return null;
    }

    private void initHeaders(final Retryable retryable) {

        SnailJobHeaders snailJobHeaders = new SnailJobHeaders();
        snailJobHeaders.setRetry(Boolean.TRUE);
        snailJobHeaders.setRetryId(IdUtil.getSnowflakeNextIdStr());
        snailJobHeaders.setDdl(GroupVersionCache.getDdl(retryable.scene()));
        RetrySiteSnapshot.setRetryHeader(snailJobHeaders);
    }

    private void sendMessage(Exception e) {

        try {
            ConfigDTO.Notify notify = GroupVersionCache.getRetryNotifyAttribute(
                    RetryNotifySceneEnum.CLIENT_COMPONENT_ERROR.getNotifyScene());
            if (Objects.nonNull(notify)) {
                SnailJobProperties snailJobProperties = SnailSpringContext.getBean(SnailJobProperties.class);
                if (Objects.isNull(snailJobProperties)) {
                    return;
                }
                List<Recipient> recipients = Optional.ofNullable(notify.getRecipients()).orElse(Lists.newArrayList());
                for (final Recipient recipient : recipients) {
                    AlarmContext context = AlarmContext.build()
                            .text(retryErrorMoreThresholdTextMessageFormatter,
                                    EnvironmentUtils.getActiveProfile(),
                                    NetUtil.getLocalIpStr(),
                                    snailJobProperties.getNamespace(),
                                    snailJobProperties.getGroup(),
                                    LocalDateTime.now().format(formatter),
                                    e.getMessage())
                            .title("retry component handling exception:[{}]", snailJobProperties.getGroup())
                            .notifyAttribute(recipient.getNotifyAttribute());

                    Optional.ofNullable(SnailJobAlarmFactory.getAlarmType(recipient.getNotifyType()))
                            .ifPresent(alarm -> alarm.asyncSendMessage(context));
                }

            }
        } catch (Exception e1) {
            SnailJobLog.LOCAL.error("Client failed to send component exception alert.", e1);
        }

    }

    public String getMethodEntrance(Retryable retryable, String executorClassName) {

        if (Objects.isNull(retryable)) {
            return StrUtil.EMPTY;
        }

        return retryable.scene().concat("_").concat(executorClassName);
    }

    private Retryable getAnnotationParameter(Method method) {

        Retryable retryable = null;
        if (method.isAnnotationPresent(Retryable.class)) {
            //获取当前类的方法上标注的注解对象
            retryable = method.getAnnotation(Retryable.class);
        }

        if (retryable == null) {
            // 返回当前类或父类或接口方法上标注的注解对象
            retryable = AnnotatedElementUtils.findMergedAnnotation(method, Retryable.class);
        }

        return retryable;
    }

    @Override
    public int getOrder() {
        return order;
    }


    private boolean validate(Throwable throwable, RetryerInfo retryerInfo) {

        Set<Class<? extends Throwable>> exclude = retryerInfo.getExclude();
        Set<Class<? extends Throwable>> include = retryerInfo.getInclude();

        if (CollUtil.isEmpty(include) && CollUtil.isEmpty(exclude)) {
            return true;
        }

        for (Class<? extends Throwable> e : include) {
            if (e.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }

        if (CollUtil.isNotEmpty(exclude)) {
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
