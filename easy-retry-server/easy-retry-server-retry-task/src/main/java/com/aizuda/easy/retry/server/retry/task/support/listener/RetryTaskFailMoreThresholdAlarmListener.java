package com.aizuda.easy.retry.server.retry.task.support.listener;
import com.aizuda.easy.retry.common.core.alarm.Alarm;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.EasyRetryAlarmFactory;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.common.core.util.HostUtils;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.flow.control.AbstractFlowControl;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.retry.task.support.cache.CacheNotifyRateLimiter;
import com.aizuda.easy.retry.server.retry.task.support.event.RetryTaskFailMoreThresholdAlarmEvent;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 重试任务失败数量超过阈值监听器
 * @author: zuoJunLin
 * @date : 2023-11-20 21:40
 * @since 2.5.0
 */
@Component
@Slf4j
public class RetryTaskFailMoreThresholdAlarmListener extends AbstractFlowControl implements Runnable, Lifecycle, ApplicationListener<RetryTaskFailMoreThresholdAlarmEvent> {

    /**
     * 重试任务失败数量超过阈值告警数据
     */
    private LinkedBlockingQueue<RetryTask> queue = new LinkedBlockingQueue<>(1000);

    private static String retryTaskDeadTextMessagesFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试任务失败数量超过阈值</font>  \n" +
            "> 组名称:{}  \n" +
            "> 执行器名称:{}  \n" +
            "> 场景名称:{}  \n" +
            "> 重试次数:{}  \n" +
            "> 业务数据:{}  \n" +
            "> 时间:{}  \n";


    @Autowired
    private EasyRetryAlarmFactory easyRetryAlarmFactory;

    @Autowired
    protected AccessTemplate accessTemplate;

    @Override
    public void start() {
        new Thread(this).start();
    }

    @Override
    public void close() {

    }

    @Override
    public void run() {
        LogUtils.info(log, "RetryTaskFailMoreThresholdAlarmListener time[{}] ip:[{}]", LocalDateTime.now(), HostUtils.getIp());
        for (; ; ) {
            try {
                RetryTask retryTask = queue.poll(5, TimeUnit.SECONDS);
                if (Objects.isNull(retryTask)) {
                    continue;
                }
                //获取通知配置
                List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess().getNotifyConfigByGroupNameAndSceneName(retryTask.getGroupName(), retryTask.getSceneName(), NotifySceneEnum.RETRY_TASK_REACH_THRESHOLD.getNotifyScene());
                for (NotifyConfig notifyConfig : notifyConfigs) {
                    //阈值判断
                    if (retryTask.getRetryCount() >= notifyConfig.getNotifyThreshold()) {
                        //限流判断
                        if (Objects.equals(notifyConfig.getRateLimiterStatus(), StatusEnum.YES.getStatus())) {
                            RateLimiter rateLimiter = getRateLimiter(CacheNotifyRateLimiter.getAll(), String.valueOf(notifyConfig.getId()),  notifyConfig.getRateLimiterThreshold());
                            if (Objects.nonNull(rateLimiter) && !rateLimiter.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                                LogUtils.warn(log, "组:[{}] 场景:[{}]  通知阈值:[{}] 幂等id:[{}] 重试任务失败数量超过阈值已到达最大限流阈值,本次通知不执行", notifyConfig.getGroupName(), notifyConfig.getSceneName(), notifyConfig.getNotifyThreshold(), retryTask.getIdempotentId());
                                continue;
                            }
                        }
                        // 预警
                        AlarmContext context = AlarmContext.build().text(retryTaskDeadTextMessagesFormatter,
                                        EnvironmentUtils.getActiveProfile(),
                                        retryTask.getGroupName(),
                                        retryTask.getExecutorName(),
                                        retryTask.getSceneName(),
                                        retryTask.getRetryCount(),
                                        retryTask.getArgsStr(),
                                        DateUtils.format(retryTask.getCreateDt(), DateUtils.NORM_DATETIME_PATTERN))
                                .title("{}环境 环境重试任务失败数量超过阈值", EnvironmentUtils.getActiveProfile())
                                .notifyAttribute(notifyConfig.getNotifyAttribute());
                        Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(notifyConfig.getNotifyType());
                        alarmType.asyncSendMessage(context);
                    }
                }
            } catch (Exception e) {
                LogUtils.error(log, "RetryTaskFailMoreThresholdAlarmListener queue poll Exception", e);
            }
        }
    }

    @Override
    public void onApplicationEvent(RetryTaskFailMoreThresholdAlarmEvent event) {
        try {
            queue.put(event.getRetryTask());
        } catch (InterruptedException e) {
            LogUtils.error(log, "RetryTaskFailMoreThresholdAlarmListener queue push Exception", e);
        }
    }
}
