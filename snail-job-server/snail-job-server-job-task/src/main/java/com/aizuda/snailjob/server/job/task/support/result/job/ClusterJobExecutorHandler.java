package com.aizuda.snailjob.server.job.task.support.result.job;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.snailjob.template.datasource.persistence.mapper.GroupConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import org.springframework.stereotype.Component;

import static com.aizuda.snailjob.common.core.enums.MapReduceStageEnum.MAP;
import static com.aizuda.snailjob.common.core.enums.MapReduceStageEnum.MERGE_REDUCE;
import static com.aizuda.snailjob.common.core.enums.MapReduceStageEnum.REDUCE;

/**
 * @author: opensnail
 * @date : 2024-09-04
 * @since :1.2.0
 */
@Component
public class ClusterJobExecutorHandler extends AbstractJobExecutorResultHandler {

    public ClusterJobExecutorHandler(
        final JobTaskMapper jobTaskMapper,
        final JobTaskBatchMapper jobTaskBatchMapper,
        final WorkflowBatchHandler workflowBatchHandler,
        final GroupConfigMapper groupConfigMapper) {
        super(jobTaskMapper, jobTaskBatchMapper, workflowBatchHandler, groupConfigMapper);
    }

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.CLUSTER;
    }

    @Override
    protected void doHandleSuccess(final JobExecutorResultContext context) {
    }

    @Override
    protected void doHandleStop(final JobExecutorResultContext context) {

    }

    @Override
    protected void doHandleFail(final JobExecutorResultContext context) {

    }

    @Override
    protected void stop(final JobExecutorResultContext context) {
    }
}
