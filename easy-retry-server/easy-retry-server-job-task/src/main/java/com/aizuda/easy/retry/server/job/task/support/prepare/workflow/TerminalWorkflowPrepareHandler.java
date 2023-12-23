package com.aizuda.easy.retry.server.job.task.support.prepare.workflow;

import com.aizuda.easy.retry.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.WorkflowBatchGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author: xiaowoniu
 * @date : 2023-12-22 08:59
 * @since : 2.6.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TerminalWorkflowPrepareHandler extends AbstractWorkflowPrePareHandler {
    private final WorkflowBatchGenerator workflowBatchGenerator;
    @Override
    public boolean matches(final Integer status) {
        return Objects.isNull(status);
    }

    @Override
    protected void doHandler(final WorkflowTaskPrepareDTO jobPrepareDTO) {
        log.info("无处理中的工作流数据. workflowId:[{}]", jobPrepareDTO.getWorkflowId());
        workflowBatchGenerator.generateJobTaskBatch(WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatchGeneratorContext(jobPrepareDTO));
    }
}
