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
import com.aizuda.easy.retry.server.retry.task.support.event.RetryTaskFailDeadLetterAlarmEvent;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryDeadLetter;
import com.google.common.cache.Cache;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 重试任务失败进入死信队列监听器
 * @author: zuoJunLin
 * @date : 2023-11-20 21:40
 * @since 2.5.0
 */
@Component
@Slf4j
public class RetryTaskFailDeadLetterAlarmListener extends AbstractFlowControl implements Runnable, Lifecycle, ApplicationListener<RetryTaskFailDeadLetterAlarmEvent> {

    /**
     * 死信告警数据
     */
    private LinkedBlockingQueue<List<RetryDeadLetter>> queue = new LinkedBlockingQueue<>(1000);

    private static String retryTaskDeadTextMessagesFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试任务失败进入死信队列</font>  \n" +
            "> 组名称:{}  \n" +
            "> 执行器名称:{}  \n" +
            "> 场景名称:{}  \n" +
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
        LogUtils.info(log, "RetryTaskFailDeadLetterAlarmListener time[{}] ip:[{}]", LocalDateTime.now(), HostUtils.getIp());
        for (; ; ) {
            try {
                List<RetryDeadLetter> allRetryDeadLetterList = queue.poll(5, TimeUnit.SECONDS);
                if (CollectionUtils.isEmpty(allRetryDeadLetterList)) {
                    continue;
                }
                //组分组
                Map<String, List<RetryDeadLetter>> groupNameMap = allRetryDeadLetterList.stream()
                        .collect(Collectors.groupingBy(RetryDeadLetter::getGroupName));
                groupNameMap.forEach(((groupName, groupRetryDeadLetterList) -> {
                    //场景分组
                    Map<String, List<RetryDeadLetter>> sceneNameMap = groupRetryDeadLetterList.stream()
                            .collect(Collectors.groupingBy(RetryDeadLetter::getSceneName));
                    sceneNameMap.forEach(((sceneName, sceneRetryDeadLetterList) -> {
                        //获取通知配置
                        List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess().getNotifyConfigByGroupNameAndSceneName(groupName, sceneName, NotifySceneEnum.RETRY_TASK_ENTER_DEAD_LETTER.getNotifyScene());
                        for (RetryDeadLetter retryDeadLetter : sceneRetryDeadLetterList) {
                            for (NotifyConfig notifyConfig : notifyConfigs) {
                                if (Objects.equals(notifyConfig.getRateLimiterStatus(),StatusEnum.YES.getStatus())) {
                                    //限流
                                    RateLimiter rateLimiter = getRateLimiter(CacheNotifyRateLimiter.getAll(), String.valueOf(notifyConfig.getId()), notifyConfig.getRateLimiterThreshold());
                                    if (Objects.nonNull(rateLimiter) && !rateLimiter.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                                        LogUtils.warn(log, "组:[{}] 场景:[{}] 幂等id:[{}]  任务重试失败进入死信队列已到达最大限流阈值,本次通知不执行", groupName, sceneName, retryDeadLetter.getIdempotentId());
                                        continue;
                                    }
                                }
                                // 预警
                                AlarmContext context = AlarmContext.build().text(retryTaskDeadTextMessagesFormatter,
                                                EnvironmentUtils.getActiveProfile(),
                                                retryDeadLetter.getGroupName(),
                                                retryDeadLetter.getExecutorName(),
                                                retryDeadLetter.getSceneName(),
                                                retryDeadLetter.getArgsStr(),
                                                DateUtils.format(retryDeadLetter.getCreateDt(), DateUtils.NORM_DATETIME_PATTERN))
                                        .title("{}环境 重试任务失败进入死信队列", EnvironmentUtils.getActiveProfile())
                                        .notifyAttribute(notifyConfig.getNotifyAttribute());
                                Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(notifyConfig.getNotifyType());
                                alarmType.asyncSendMessage(context);
                            }
                        }
                    }));
                }));
            } catch (Exception e) {
                LogUtils.error(log, "RetryTaskFailDeadLetterAlarmListener queue poll Exception", e);
            }
        }
    }

    @Override
    public void onApplicationEvent(RetryTaskFailDeadLetterAlarmEvent event) {
        try {
            queue.put(event.getRetryDeadLetters());
        } catch (InterruptedException e) {
            LogUtils.error(log, "RetryTaskFailDeadLetterAlarmListener queue push Exception", e);
        }
    }


}
