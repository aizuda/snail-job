package com.aizuda.easy.retry.client.core.log;

import com.aizuda.easy.retry.client.common.report.AbstractLogReport;
import com.aizuda.easy.retry.client.common.util.ThreadLocalLogUtil;
import com.aizuda.easy.retry.common.core.model.JobContext;
import com.aizuda.easy.retry.common.log.dto.LogContentDTO;
import com.aizuda.easy.retry.server.model.dto.LogTaskDTO;
import org.springframework.stereotype.Component;

/**
 * @author xiaowoniu
 * @date 2024-03-20 23:25:24
 * @since 3.2.0
 */
//@Component
public class RetryLogReport extends AbstractLogReport {
    @Override
    protected LogTaskDTO buildLogTaskDTO(LogContentDTO logContentDTO) {
//        JobContext context = ThreadLocalLogUtil.getContext();

        LogTaskDTO logTaskDTO = new LogTaskDTO();
//        logTaskDTO.setJobId(context.getJobId());
//        logTaskDTO.setTaskId(context.getTaskId());
//        logTaskDTO.setTaskBatchId(context.getTaskBatchId());
//        logTaskDTO.setRealTime(logContentDTO.getTimeStamp());
//        logTaskDTO.setNamespaceId(context.getNamespaceId());
//        logTaskDTO.setGroupName(context.getGroupName());
        logTaskDTO.setFieldList(logContentDTO.getFieldList());
        return logTaskDTO;
    }
}
