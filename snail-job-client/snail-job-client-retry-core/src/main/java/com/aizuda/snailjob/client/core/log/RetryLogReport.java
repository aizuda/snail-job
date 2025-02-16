package com.aizuda.snailjob.client.core.log;

import com.aizuda.snailjob.client.common.log.report.AbstractLogReport;
import com.aizuda.snailjob.client.common.log.support.SnailJobLogManager;
import com.aizuda.snailjob.common.log.dto.LogContentDTO;
import com.aizuda.snailjob.common.log.enums.LogTypeEnum;
import com.aizuda.snailjob.server.model.dto.RetryLogTaskDTO;
import org.springframework.stereotype.Component;

/**
 * @author xiaowoniu
 * @date 2024-03-20 23:25:24
 * @since 3.2.0
 */
@Component
public class RetryLogReport extends AbstractLogReport<RetryLogTaskDTO> {
    @Override
    protected RetryLogTaskDTO buildLogTaskDTO(LogContentDTO logContentDTO) {
        RetryLogMeta context = (RetryLogMeta) SnailJobLogManager.getLogMeta();
        RetryLogTaskDTO logTaskDTO = new RetryLogTaskDTO();
        logTaskDTO.setLogType(LogTypeEnum.RETRY.name());
        logTaskDTO.setRetryId(context.getRetryId());
        logTaskDTO.setRetryTaskId(context.getRetryTaskId());
        logTaskDTO.setRealTime(logContentDTO.getTimeStamp());
        logTaskDTO.setNamespaceId(context.getNamespaceId());
        logTaskDTO.setGroupName(context.getGroupName());
        logTaskDTO.setFieldList(logContentDTO.getFieldList());
        return logTaskDTO;
    }

    @Override
    public boolean supports() {
        return LogTypeEnum.RETRY == SnailJobLogManager.getLogType();
    }
}
