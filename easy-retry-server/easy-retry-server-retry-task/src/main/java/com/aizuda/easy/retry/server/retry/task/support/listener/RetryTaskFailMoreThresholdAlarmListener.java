package com.aizuda.easy.retry.server.retry.task.support.listener;

import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.common.core.util.HostUtils;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.flow.control.AbstractFlowControl;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.common.triple.ImmutableTriple;
import com.aizuda.easy.retry.server.common.triple.Triple;
import com.aizuda.easy.retry.server.retry.task.support.event.RetryTaskFailMoreThresholdAlarmEvent;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * 重试任务失败数量超过阈值监听器
 *
 * @author: zuoJunLin
 * @date : 2023-11-20 21:40
 * @since 2.5.0
 */
@Component
@Slf4j
public class RetryTaskFailMoreThresholdAlarmListener extends
    AbstractFlowControl<RetryTaskFailMoreThresholdAlarmEvent> implements Runnable, Lifecycle {

    /**
     * 重试任务失败数量超过阈值告警数据
     */
    private LinkedBlockingQueue<RetryTask> queue = new LinkedBlockingQueue<>(1000);
    private Thread thread;
    private static String retryTaskFailMoreThresholdMessagesFormatter =
        "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试任务失败数量超过阈值</font>  \n" +
            "> 组名称:{}  \n" +
            "> 执行器名称:{}  \n" +
            "> 场景名称:{}  \n" +
            "> 重试次数:{}  \n" +
            "> 业务数据:{}  \n" +
            "> 时间:{}  \n";

    @Override
    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void close() {
        if (Objects.nonNull(thread)) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        LogUtils.info(log, "RetryTaskFailMoreThresholdAlarmListener time[{}] ip:[{}]", LocalDateTime.now(),
            HostUtils.getIp());
        while (!Thread.currentThread().isInterrupted()) {
            try {

                // 无数据时阻塞线程
                RetryTask retryTask = queue.take();

                // 拉取100条
                List<RetryTask> lists = Lists.newArrayList(retryTask);
                queue.drainTo(lists, 200);

                Set<String> namespaceIds = new HashSet<>();
                Set<String> groupNames = new HashSet<>();
                Set<String> sceneNames = new HashSet<>();
                Map<Triple<String, String, String>, List<RetryTask>> retryTaskMap = getRetryTaskMap(
                    lists, namespaceIds, groupNames, sceneNames);

                Map<Triple<String, String, String>, List<NotifyConfig>> notifyConfigMap = getNotifyConfigMap(
                    namespaceIds, groupNames, sceneNames);
                if (notifyConfigMap == null) {
                    continue;
                }

                // 循环发送消息
                retryTaskMap.forEach((key, list) -> {
                    List<NotifyConfig> notifyConfigsList = notifyConfigMap.get(key);
                    for (RetryTask retryTask1 : list) {
                        doSendAlarm(key, notifyConfigsList, AlarmDTOConverter.INSTANCE.toAlarmDTO(retryTask1));
                    }
                });
            } catch (InterruptedException e) {
                LogUtils.info(log, "retry task fail more alarm stop");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LogUtils.error(log, "RetryTaskFailMoreThresholdAlarmListener queue poll Exception", e);
            }
        }
    }

    @Override
    public void onApplicationEvent(RetryTaskFailMoreThresholdAlarmEvent event) {
        if (queue.offer(event.getRetryTask())) {
            LogUtils.warn(log, "任务失败数量超过阈值告警队列已满");
        }
    }

    @Override
    protected AlarmContext buildAlarmContext(AlarmDTO alarmDTO, NotifyConfig notifyConfig) {
        // 预警
        return AlarmContext.build().text(retryTaskFailMoreThresholdMessagesFormatter,
                EnvironmentUtils.getActiveProfile(),
                alarmDTO.getGroupName(),
                alarmDTO.getExecutorName(),
                alarmDTO.getSceneName(),
                alarmDTO.getRetryCount(),
                alarmDTO.getArgsStr(),
                DateUtils.format(alarmDTO.getCreateDt(), DateUtils.NORM_DATETIME_PATTERN))
            .title("组:[{}] 场景:[{}] 环境重试任务失败数量超过阈值", alarmDTO.getGroupName(), alarmDTO.getSceneName())
            .notifyAttribute(notifyConfig.getNotifyAttribute());
    }

    @NotNull
    private static Map<Triple<String, String, String>, List<RetryTask>> getRetryTaskMap(
        final List<RetryTask> list, final Set<String> namespaceIds,
        final Set<String> groupNames, final Set<String> sceneNames) {

        return list.stream()
            .collect(Collectors.groupingBy(retryTask -> {
                String namespaceId = retryTask.getNamespaceId();
                String groupName = retryTask.getGroupName();
                String sceneName = retryTask.getSceneName();

                namespaceIds.add(namespaceId);
                groupNames.add(groupName);
                sceneNames.add(sceneName);

                return ImmutableTriple.of(namespaceId, groupName, sceneName);
            }));

    }


}
