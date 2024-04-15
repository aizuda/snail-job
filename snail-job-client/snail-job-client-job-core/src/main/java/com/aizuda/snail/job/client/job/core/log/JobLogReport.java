package com.aizuda.snail.job.client.job.core.log;

import com.aizuda.snail.job.client.common.log.report.AbstractLogReport;
import com.aizuda.snail.job.client.common.log.support.EasyRetryLogManager;
import com.aizuda.snail.job.common.log.dto.LogContentDTO;
import com.aizuda.snail.job.common.log.enums.LogTypeEnum;
import com.aizuda.snail.job.server.model.dto.JobLogTaskDTO;
import org.springframework.stereotype.Component;

/**
 * @author xiaowoniu
 * @date 2024-03-20 23:25:24
 * @since 3.2.0
 */
@Component
public class JobLogReport extends AbstractLogReport<JobLogTaskDTO> {

    @Override
    public boolean supports () {
        return LogTypeEnum.JOB == EasyRetryLogManager.getLogType();
    }

    @Override
    protected JobLogTaskDTO buildLogTaskDTO(LogContentDTO logContentDTO) {
        JobLogMeta context = (JobLogMeta) EasyRetryLogManager.getLogMeta();
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
