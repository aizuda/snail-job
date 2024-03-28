package com.aizuda.easy.retry.server.job.task.support.listener;

import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.EasyRetryAlarmFactory;
import com.aizuda.easy.retry.common.core.enums.JobNotifySceneEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.server.common.AlarmInfoConverter;
import com.aizuda.easy.retry.server.common.alarm.AbstractJobAlarm;
import com.aizuda.easy.retry.server.common.dto.JobAlarmInfo;
import com.aizuda.easy.retry.server.common.dto.NotifyConfigInfo;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.support.event.JobTaskFailAlarmEvent;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * JOB任务执行失败告警
 *
 * @author: zuoJunLin
 * @date : 2023-12-02 21:40
 * @since 2.5.0
 */
@Component
@Slf4j
public class JobTaskFailAlarmListener extends AbstractJobAlarm<JobTaskFailAlarmEvent> {

    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;

    /**
     * job任务失败数据
     */
    private LinkedBlockingQueue<Long> queue = new LinkedBlockingQueue<>(1000);

    private static String jobTaskFailTextMessagesFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 Job任务执行失败</font> \n" +
                    "> 空间ID:{}  \n" +
                    "> 组名称:{}  \n" +
                    "> 任务名称:{}  \n" +
                    "> 执行器名称:{}  \n" +
                    "> 方法参数:{}  \n" +
                    "> 时间:{}  \n";

    @Override
    protected List<JobAlarmInfo> poll() throws InterruptedException {
        // 无数据时阻塞线程
        Long jobTaskBatchId = queue.take();
        // 拉取200条
        List<Long> jobTaskBatchIds = Lists.newArrayList(jobTaskBatchId);
        queue.drainTo(jobTaskBatchIds, 200);
        QueryWrapper<JobTaskBatch> wrapper = new QueryWrapper<JobTaskBatch>()
                .in("a.id", jobTaskBatchIds).eq("a.deleted", 0);
        List<JobBatchResponseDO> jobTaskBatchList = jobTaskBatchMapper.selectJobBatchListByIds(wrapper);
        return AlarmInfoConverter.INSTANCE.toJobAlarmInfos(jobTaskBatchList);
    }

    @Override
    protected AlarmContext buildAlarmContext(JobAlarmInfo alarmDTO, NotifyConfigInfo notifyConfig) {
        // 预警
        return AlarmContext.build()
                .text(jobTaskFailTextMessagesFormatter,
                        EnvironmentUtils.getActiveProfile(),
                        alarmDTO.getNamespaceId(),
                        alarmDTO.getGroupName(),
                        alarmDTO.getJobName(),
                        alarmDTO.getExecutorInfo(),
                        alarmDTO.getArgsStr(),
                        DateUtils.toNowFormat(DateUtils.NORM_DATETIME_PATTERN))
                .title("{}环境 JOB任务失败", EnvironmentUtils.getActiveProfile())
                .notifyAttribute(notifyConfig.getNotifyAttribute());
    }

    @Override
    protected void startLog() {
       EasyRetryLog.LOCAL.info("JobTaskFailAlarmListener started");
    }

    @Override
    protected int getNotifyScene() {
        return JobNotifySceneEnum.JOB_TASK_ERROR.getNotifyScene();
    }


    @Override
    public void onApplicationEvent(JobTaskFailAlarmEvent event) {
        if (!queue.offer(event.getJobTaskBatchId())) {
           EasyRetryLog.LOCAL.warn("JOB任务执行失败告警队列已满");
        }
    }
}
