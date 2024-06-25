package com.aizuda.snailjob.client.core.strategy;

import com.aizuda.snailjob.client.common.cache.GroupVersionCache;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.core.Report;
import com.aizuda.snailjob.client.core.RetryExecutor;
import com.aizuda.snailjob.client.core.RetryExecutorParameter;
import com.aizuda.snailjob.client.core.event.SnailJobListener;
import com.aizuda.snailjob.client.core.executor.GuavaRetryExecutor;
import com.aizuda.snailjob.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.snailjob.client.core.loader.SnailRetrySpiLoader;
import com.aizuda.snailjob.client.core.retryer.RetryerInfo;
import com.aizuda.snailjob.client.core.retryer.RetryerResultContext;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.alarm.SnailJobAlarmFactory;
import com.aizuda.snailjob.common.core.enums.RetryNotifySceneEnum;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.common.core.util.NetUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;
import com.aizuda.snailjob.server.model.dto.ConfigDTO.Notify.Recipient;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.StopStrategy;
import com.github.rholder.retry.WaitStrategy;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.YYYY_MM_DD_HH_MM_SS;

/**
 * @author: opensnail
 * @date : 2022-03-04 14:40
 */
@Slf4j
public abstract class AbstractRetryStrategies implements RetryStrategy {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
    private static final String TEXT_MESSAGE_FORMATTER =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试组件异常</font>  \n" +
                    "> IP:{}  \n" +
                    "> 空间ID:{}  \n" +
                    "> 名称:{}  \n" +
                    "> 时间:{}  \n" +
                    "> 异常:{}  \n";

    private final List<SnailJobListener> snailJobListeners = SnailRetrySpiLoader.loadSnailJobListener();

    @Autowired
    private List<Report> reports;
    @Autowired
    private SnailJobProperties snailJobProperties;

    @Override
    public RetryerResultContext openRetry(String sceneName, String executorClassName, Object[] params) {

        RetryerResultContext retryerResultContext = new RetryerResultContext();

        // 开始内存重试
        RetryExecutor<WaitStrategy, StopStrategy> retryExecutor =
                new GuavaRetryExecutor(sceneName, executorClassName);
        RetryerInfo retryerInfo = retryExecutor.getRetryerInfo();

        if (!preValidator(retryerInfo, retryerResultContext)) {
            return retryerResultContext;
        }

        RetrySiteSnapshot.setStatus(RetrySiteSnapshot.EnumStatus.RUNNING.getStatus());

        setStage();

        Retryer retryer = retryExecutor.build(getRetryExecutorParameter(retryerInfo));

        retryerResultContext.setRetryerInfo(retryerInfo);

        try {
            for (SnailJobListener snailJobListener : snailJobListeners) {
                snailJobListener.beforeRetry(sceneName, executorClassName, params);
            }

            Object result = retryExecutor.call(retryer, doGetCallable(retryExecutor, params), getRetryErrorConsumer(retryerResultContext, params), getRetrySuccessConsumer(retryerResultContext));
            retryerResultContext.setResult(result);

        } catch (Exception e) {
            log.error("重试期间发生非预期异常, sceneName:[{}] executorClassName:[{}]", sceneName, executorClassName, e);
            retryerResultContext.setMessage("非预期异常" + e.getMessage());
            // 本地重试状态为失败 远程重试状态为成功
            unexpectedError(e, retryerResultContext);

            // 预警
            sendMessage(e);
        } finally {
            // 重试调度完成
            RetrySiteSnapshot.setStatus(RetrySiteSnapshot.EnumStatus.COMPLETE.getStatus());
        }

        return retryerResultContext;
    }

    protected abstract void setStage();

    protected Consumer<Object> getRetrySuccessConsumer(RetryerResultContext retryerResultContext) {
        return o -> {

            success(retryerResultContext);

            Object result = retryerResultContext.getResult();
            RetryerInfo retryerInfo = retryerResultContext.getRetryerInfo();

            for (SnailJobListener snailJobListener : snailJobListeners) {
                snailJobListener.successOnRetry(result, retryerInfo.getScene(), retryerInfo.getExecutorClassName());
            }

            doRetrySuccessConsumer(retryerResultContext).accept(retryerResultContext);
        };
    }


    protected abstract Consumer<Object> doRetrySuccessConsumer(RetryerResultContext context);

    private Consumer<Throwable> getRetryErrorConsumer(RetryerResultContext context, Object... params) {
        return throwable -> {
            context.setThrowable(throwable);
            context.setMessage(throwable.getMessage());

            error(context);

            RetryerInfo retryerInfo = context.getRetryerInfo();
            try {
                for (SnailJobListener snailJobListener : snailJobListeners) {
                    snailJobListener
                            .failureOnRetry(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), throwable);
                }
            } catch (Exception e) {
                log.error("失败监听者模式 处理失败 ", e);
                throw e;
            }

            doGetRetryErrorConsumer(retryerInfo, params).accept(throwable);

        };
    }

    protected abstract void error(RetryerResultContext context);

    protected abstract boolean preValidator(RetryerInfo retryerInfo, RetryerResultContext resultContext);

    protected abstract void unexpectedError(Exception e, RetryerResultContext retryerResultContext);

    protected abstract void success(RetryerResultContext retryerResultContext);

    protected abstract Consumer<Throwable> doGetRetryErrorConsumer(RetryerInfo retryerInfo, Object[] params);

    protected abstract Callable doGetCallable(RetryExecutor<WaitStrategy, StopStrategy> retryExecutor, Object[] params);

    protected abstract RetryExecutorParameter<WaitStrategy, StopStrategy> getRetryExecutorParameter(RetryerInfo retryerInfo);

    /**
     * 上报数据
     *
     * @param retryerInfo 定义重试场景的信息
     * @param params      执行参数
     */
    protected boolean doReport(final RetryerInfo retryerInfo, final Object[] params) {

        for (Report report : reports) {
            if (report.supports(retryerInfo.isAsync())) {
                return report.report(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), params);
            }
        }

        return Boolean.FALSE;
    }

    private void sendMessage(Exception e) {

        try {
            ConfigDTO.Notify notify = GroupVersionCache.getRetryNotifyAttribute(RetryNotifySceneEnum.CLIENT_COMPONENT_ERROR.getNotifyScene());
            if (Objects.nonNull(notify)) {
                List<Recipient> recipients = Optional.ofNullable(notify.getRecipients()).orElse(Lists.newArrayList());

                for (final Recipient recipient : recipients) {
                    AlarmContext context = AlarmContext.build()
                            .text(TEXT_MESSAGE_FORMATTER,
                                    EnvironmentUtils.getActiveProfile(),
                                    NetUtil.getLocalIpStr(),
                                    snailJobProperties.getNamespace(),
                                    snailJobProperties.getGroup(),
                                    LocalDateTime.now().format(formatter),
                                    e.getMessage())
                            .title("retry component handling exception:[{}]", snailJobProperties.getGroup())
                            .notifyAttribute(recipient.getNotifyAttribute());
                    Optional.ofNullable(SnailJobAlarmFactory.getAlarmType(recipient.getNotifyType())).ifPresent(alarm -> alarm.asyncSendMessage(context));
                }
            }
        } catch (Exception e1) {
            SnailJobLog.LOCAL.error("Client failed to send component exception alert.", e1);
        }

    }
}
