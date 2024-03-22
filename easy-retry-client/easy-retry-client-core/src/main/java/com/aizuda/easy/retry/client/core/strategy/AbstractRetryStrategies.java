package com.aizuda.easy.retry.client.core.strategy;

import com.aizuda.easy.retry.client.core.Report;
import com.aizuda.easy.retry.client.common.cache.GroupVersionCache;
import com.aizuda.easy.retry.client.common.config.EasyRetryProperties;
import com.aizuda.easy.retry.client.core.RetryExecutor;
import com.aizuda.easy.retry.client.core.RetryExecutorParameter;
import com.aizuda.easy.retry.client.core.event.EasyRetryListener;
import com.aizuda.easy.retry.client.core.executor.GuavaRetryExecutor;
import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot.EnumStatus;
import com.aizuda.easy.retry.client.core.loader.EasyRetrySpiLoader;
import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;
import com.aizuda.easy.retry.client.core.retryer.RetryerResultContext;
import com.aizuda.easy.retry.common.core.alarm.Alarm;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.EasyRetryAlarmFactory;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.common.core.util.NetUtil;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.StopStrategy;
import com.github.rholder.retry.WaitStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-04 14:40
 */
@Slf4j
public abstract class AbstractRetryStrategies implements RetryStrategy {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static String retryErrorMoreThresholdTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试组件异常</font>  \n" +
                    "> IP:{}  \n" +
                    "> 空间ID:{}  \n" +
                    "> 名称:{}  \n" +
                    "> 时间:{}  \n" +
                    "> 异常:{}  \n"
            ;

    private final List<EasyRetryListener> easyRetryListeners = EasyRetrySpiLoader.loadEasyRetryListener();

    @Autowired
    private EasyRetryAlarmFactory easyRetryAlarmFactory;
    @Autowired
    private List<Report> reports;
    @Autowired
    private EasyRetryProperties easyRetryProperties;

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
            for (EasyRetryListener easyRetryListener : easyRetryListeners) {
                easyRetryListener.beforeRetry(sceneName, executorClassName, params);
            }

            Object result = retryExecutor.call(retryer, doGetCallable(retryExecutor, params), getRetryErrorConsumer(retryerResultContext, params), getRetrySuccessConsumer(retryerResultContext));
            retryerResultContext.setResult(result);

        } catch (Exception e) {
            log.error("重试期间发生非预期异常, sceneName:[{}] executorClassName:[{}]", sceneName, executorClassName,  e);
            retryerResultContext.setMessage("非预期异常" + e.getMessage());
            // 本地重试状态为失败 远程重试状态为成功
            unexpectedError(e, retryerResultContext);

            // 预警
            sendMessage(e);
        } finally {
            // 重试调度完成
            RetrySiteSnapshot.setStatus(EnumStatus.COMPLETE.getStatus());
        }

        return retryerResultContext;
    }

    protected abstract void setStage();

    protected Consumer<Object> getRetrySuccessConsumer(RetryerResultContext retryerResultContext) {
        return o -> {

            success(retryerResultContext);

            Object result = retryerResultContext.getResult();
            RetryerInfo retryerInfo = retryerResultContext.getRetryerInfo();

            for (EasyRetryListener easyRetryListener : easyRetryListeners) {
                easyRetryListener.successOnRetry(result, retryerInfo.getScene(), retryerInfo.getExecutorClassName());
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
                for (EasyRetryListener easyRetryListener : easyRetryListeners) {
                    easyRetryListener
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
    protected abstract Callable doGetCallable(RetryExecutor<WaitStrategy, StopStrategy> retryExecutor,Object[] params);
    protected abstract RetryExecutorParameter<WaitStrategy, StopStrategy> getRetryExecutorParameter(RetryerInfo retryerInfo);
    /**
     * 上报数据
     *
     * @param retryerInfo 定义重试场景的信息
     * @param params 执行参数
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
            ConfigDTO.Notify notifyAttribute = GroupVersionCache.getNotifyAttribute(NotifySceneEnum.CLIENT_COMPONENT_ERROR.getNotifyScene());
            if (Objects.nonNull(notifyAttribute)) {
                AlarmContext context = AlarmContext.build()
                        .text(retryErrorMoreThresholdTextMessageFormatter,
                                EnvironmentUtils.getActiveProfile(),
                                NetUtil.getLocalIpStr(),
                                easyRetryProperties.getNamespace(),
                                EasyRetryProperties.getGroup(),
                                LocalDateTime.now().format(formatter),
                                e.getMessage())
                        .title("retry component handling exception:[{}]", EasyRetryProperties.getGroup())
                        .notifyAttribute(notifyAttribute.getNotifyAttribute());

                Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(notifyAttribute.getNotifyType());
                alarmType.asyncSendMessage(context);
            }
        } catch (Exception e1) {
            EasyRetryLog.LOCAL.error("Client failed to send component exception alert.", e1);
        }

    }
}
