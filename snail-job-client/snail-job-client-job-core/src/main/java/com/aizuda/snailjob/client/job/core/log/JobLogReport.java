package com.aizuda.snailjob.client.job.core.log;

import com.aizuda.snailjob.client.common.log.report.AbstractLogReport;
import com.aizuda.snailjob.client.common.log.support.SnailJobLogManager;
import com.aizuda.snailjob.common.log.dto.LogContentDTO;
import com.aizuda.snailjob.common.log.enums.LogTypeEnum;
import com.aizuda.snailjob.server.model.dto.JobLogTaskDTO;
import org.springframework.stereotype.Component;

/**
 * @author xiaowoniu
 * @date 2024-03-20 23:25:24
 * @since 3.2.0
 */
@Component
public class JobLogReport extends AbstractLogReport<JobLogTaskDTO> {

    @Override
    public boolean supports() {
        return LogTypeEnum.JOB == SnailJobLogManager.getLogType();
    }

    @Override
    protected JobLogTaskDTO buildLogTaskDTO(LogContentDTO logContentDTO) {
        JobLogMeta context = (JobLogMeta) SnailJobLogManager.getLogMeta();
        JobLogTaskDTO logTaskDTO = new JobLogTaskDTO();
        logTaskDTO.setJobId(context.getJobId());
        logTaskDTO.setLogType(LogTypeEnum.JOB.name());
        logTaskDTO.setTaskId(context.getTaskId());
        logTaskDTO.setTaskBatchId(context.getTaskBatchId());
        logTaskDTO.setRealTime(logContentDTO.getTimeStamp());
        logTaskDTO.setNamespaceId(context.getNamespaceId());
        logTaskDTO.setGroupName(context.getGroupName());
        logTaskDTO.setFieldList(logContentDTO.getFieldList());
        return logTaskDTO;
    }
}
