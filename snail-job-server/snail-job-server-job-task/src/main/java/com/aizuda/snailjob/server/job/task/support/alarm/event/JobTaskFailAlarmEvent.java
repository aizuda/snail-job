package com.aizuda.snailjob.server.job.task.support.alarm.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * job任务失败事件
 *
 * @author: zuoJunLin
 * @date : 2023-12-02 21:40
 * @since 2.5.0
 */
@Getter
public class JobTaskFailAlarmEvent extends ApplicationEvent {

    private final Long jobTaskBatchId;

    public JobTaskFailAlarmEvent(Long jobTaskBatchId) {
        super(jobTaskBatchId);
        this.jobTaskBatchId = jobTaskBatchId;
    }

}
