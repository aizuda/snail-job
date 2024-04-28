package com.aizuda.snailjob.server.job.task.support.listener;

import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.enums.JobNotifySceneEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.server.common.AlarmInfoConverter;
import com.aizuda.snailjob.server.common.alarm.AbstractJobAlarm;
import com.aizuda.snailjob.server.common.dto.JobAlarmInfo;
import com.aizuda.snailjob.server.common.dto.NotifyConfigInfo;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.triple.Triple;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.support.event.JobTaskFailAlarmEvent;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * JOB任务执行失败告警
 *
 * @author: zuoJunLin
 * @date : 2023-12-02 21:40
 * @since 2.5.0
 */
@Component
@RequiredArgsConstructor
public class JobTaskFailAlarmListener extends AbstractJobAlarm<JobTaskFailAlarmEvent> {
    private final JobTaskBatchMapper jobTaskBatchMapper;

    /**
     * job任务失败数据
     */
    private final LinkedBlockingQueue<Long> queue = new LinkedBlockingQueue<>(1000);

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
                .title("{}环境 JOB任务失败", EnvironmentUtils.getActiveProfile());
    }

    @Override
    protected void startLog() {
       SnailJobLog.LOCAL.info("JobTaskFailAlarmListener started");
    }

    @Override
    protected int getNotifyScene() {
        return JobNotifySceneEnum.JOB_TASK_ERROR.getNotifyScene();
    }

    @Override
    protected List<SyetemTaskTypeEnum> getSystemTaskType() {
        return Lists.newArrayList(SyetemTaskTypeEnum.JOB);
    }

    @Override
    public void onApplicationEvent(JobTaskFailAlarmEvent event) {
        if (!queue.offer(event.getJobTaskBatchId())) {
           SnailJobLog.LOCAL.warn("JOB任务执行失败告警队列已满");
        }
    }
}
