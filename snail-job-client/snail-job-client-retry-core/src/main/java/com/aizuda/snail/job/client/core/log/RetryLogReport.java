package com.aizuda.snail.job.client.core.log;

import com.aizuda.snail.job.client.common.log.report.AbstractLogReport;
import com.aizuda.snail.job.client.common.log.support.EasyRetryLogManager;
import com.aizuda.snail.job.common.log.dto.LogContentDTO;
import com.aizuda.snail.job.common.log.enums.LogTypeEnum;
import com.aizuda.snail.job.server.model.dto.RetryLogTaskDTO;
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
        RetryLogMeta context = (RetryLogMeta) EasyRetryLogManager.getLogMeta();
        RetryLogTaskDTO logTaskDTO = new RetryLogTaskDTO();
        logTaskDTO.setLogType(LogTypeEnum.RETRY.name());
        logTaskDTO.setUniqueId(context.getUniqueId());
        logTaskDTO.setRealTime(logContentDTO.getTimeStamp());
        logTaskDTO.setNamespaceId(context.getNamespaceId());
        logTaskDTO.setGroupName(context.getGroupName());
        logTaskDTO.setFieldList(logContentDTO.getFieldList());
        return logTaskDTO;
    }

    @Override
    public boolean supports() {
        return LogTypeEnum.RETRY == EasyRetryLogManager.getLogType();
    }
}
