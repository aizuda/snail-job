package com.aizuda.easy.retry.server.job.task.support.listener;

import com.aizuda.easy.retry.common.core.alarm.Alarm;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.EasyRetryAlarmFactory;
import com.aizuda.easy.retry.common.core.enums.JobNotifySceneEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.common.core.util.HostUtils;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.triple.ImmutableTriple;
import com.aizuda.easy.retry.server.common.triple.Triple;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.support.event.JobTaskFailAlarmEvent;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobNotifyConfigMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;


/**
 * JOB任务执行失败告警
 *
 * @author: zuoJunLin
 * @date : 2023-12-02 21:40
 * @since 2.5.0
 */
@Component
@Slf4j
public class JobTaskFailAlarmListener implements ApplicationListener<JobTaskFailAlarmEvent>, Runnable, Lifecycle {


    @Autowired
    private JobNotifyConfigMapper jobNotifyConfigMapper;

    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;

    @Autowired
    private EasyRetryAlarmFactory easyRetryAlarmFactory;

    /**
     * job任务失败数据
     */
    private LinkedBlockingQueue<Long> queue = new LinkedBlockingQueue<>(1000);

    private static String jobTaskFailTextMessagesFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 Job任务执行失败</font>  \n" +
                    "> 组名称:{}  \n" +
                    "> 任务名称:{}  \n" +
                    "> 执行器名称:{}  \n" +
                    "> 方法参数:{}  \n" +
                    "> 时间:{}  \n";

    private Thread thread;

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
        LogUtils.info(log, "JobTaskFailAlarmListener time[{}] ip:[{}]", LocalDateTime.now(), HostUtils.getIp());
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 无数据时阻塞线程
                Long jobTaskBatchId = queue.take();
                // 拉取200条
                List<Long> jobTaskBatchIds = Lists.newArrayList(jobTaskBatchId);
                queue.drainTo(jobTaskBatchIds, 200);
                List<JobBatchResponseDO> jobTaskBatchList = jobTaskBatchMapper.selectJobBatchListByIds(jobTaskBatchIds);
                Set<String> namespaceIds = new HashSet<>();
                Set<String> groupNames = new HashSet<>();
                Set<Long> jobIds = new HashSet<>();
                Map<Triple<String, String, Long>, List<JobBatchResponseDO>> jobTaskBatchMap = getJobTaskBatchMap(
                        jobTaskBatchList, namespaceIds, groupNames, jobIds);

                Map<Triple<String, String, Long>, List<JobNotifyConfig>> jobNotifyConfigMap = getJobNotifyConfigMap(
                        namespaceIds, groupNames, jobIds);
                if (jobNotifyConfigMap == null) {
                    continue;
                }
                // 循环发送消息
                jobTaskBatchMap.forEach((key, list) -> {
                    List<JobNotifyConfig> jobNotifyConfigsList = jobNotifyConfigMap.get(key);
                    for (JobBatchResponseDO JobBatch : list) {
                        for (final JobNotifyConfig jobNotifyConfig : jobNotifyConfigsList) {
                            // 预警
                            AlarmContext context = AlarmContext.build()
                                    .text(jobTaskFailTextMessagesFormatter,
                                            EnvironmentUtils.getActiveProfile(),
                                            JobBatch.getGroupName(),
                                            JobBatch.getJobName(),
                                            JobBatch.getExecutorInfo(),
                                            JobBatch.getArgsStr(),
                                            DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN))
                                    .title("{}环境 JOB任务失败", EnvironmentUtils.getActiveProfile())
                                    .notifyAttribute(jobNotifyConfig.getNotifyAttribute());
                            Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(jobNotifyConfig.getNotifyType());
                            alarmType.asyncSendMessage(context);
                        }
                    }
                });
            } catch (InterruptedException e) {
                LogUtils.info(log, "job task fail more alarm stop");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LogUtils.error(log, "JobTaskFailAlarmListener queue take Exception", e);
            }
        }
    }

    private Map<Triple<String, String, Long>, List<JobNotifyConfig>> getJobNotifyConfigMap(Set<String> namespaceIds, Set<String> groupNames, Set<Long> jobIds) {
        // 批量获取所需的通知配置
        List<JobNotifyConfig> jobNotifyConfigs = jobNotifyConfigMapper.selectList(
                new LambdaQueryWrapper<JobNotifyConfig>()
                        .eq(JobNotifyConfig::getNotifyStatus, StatusEnum.YES)
                        .eq(JobNotifyConfig::getNotifyScene, JobNotifySceneEnum.JOB_TASK_ERROR.getNotifyScene())
                        .in(JobNotifyConfig::getNamespaceId, namespaceIds)
                        .in(JobNotifyConfig::getGroupName, groupNames)
                        .in(JobNotifyConfig::getJobId, jobIds)
        );
        if (CollectionUtils.isEmpty(jobNotifyConfigs)) {
            return null;
        }
        return jobNotifyConfigs.stream()
                .collect(Collectors.groupingBy(i -> {

                    String namespaceId = i.getNamespaceId();
                    String groupName = i.getGroupName();
                    Long jobId = i.getJobId();

                    return ImmutableTriple.of(namespaceId, groupName, jobId);
                }));
    }

    private Map<Triple<String, String, Long>, List<JobBatchResponseDO>> getJobTaskBatchMap(List<JobBatchResponseDO> jobTaskBatchList, Set<String> namespaceIds, Set<String> groupNames, Set<Long> jobIds) {
        return jobTaskBatchList.stream().collect(Collectors.groupingBy(i -> {
            String namespaceId = i.getNamespaceId();
            String groupName = i.getGroupName();
            Long jobId = i.getJobId();
            namespaceIds.add(namespaceId);
            groupNames.add(groupName);
            jobIds.add(jobId);
            return ImmutableTriple.of(namespaceId, groupName, jobId);
        }));
    }


    @Override
    public void onApplicationEvent(JobTaskFailAlarmEvent event) {
        if (queue.offer(event.getJobTaskBatchId())) {
            LogUtils.warn(log, "JOB任务执行失败告警队列已满");
        }
    }
}
