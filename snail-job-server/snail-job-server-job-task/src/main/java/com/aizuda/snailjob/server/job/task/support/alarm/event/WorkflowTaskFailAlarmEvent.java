package com.aizuda.snailjob.server.job.task.support.alarm.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * workflow任务失败事件
 *
 * @author: opensnail
 * @date : 2023-12-02 21:40
 * @since sj_1.0.0
 */
@Getter
public class WorkflowTaskFailAlarmEvent extends ApplicationEvent {

    private final Long workflowTaskBatchId;

    public WorkflowTaskFailAlarmEvent(Long workflowTaskBatchId) {
        super(workflowTaskBatchId);
        this.workflowTaskBatchId = workflowTaskBatchId;
    }

}
