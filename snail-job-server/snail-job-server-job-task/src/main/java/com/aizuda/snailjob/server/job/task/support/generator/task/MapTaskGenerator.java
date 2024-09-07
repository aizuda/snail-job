package com.aizuda.snailjob.server.job.task.support.generator.task;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.snailjob.server.job.task.dto.MapReduceArgsStrDTO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2024-06-19
 * @since : sj_1.1.0
 */
@Component
public class MapTaskGenerator extends MapReduceTaskGenerator {

    public MapTaskGenerator(final JobTaskMapper jobTaskMapper,
                            final TransactionTemplate transactionTemplate,
                            final ClientNodeAllocateHandler clientNodeAllocateHandler) {
        super(jobTaskMapper, transactionTemplate, clientNodeAllocateHandler);
    }

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.MAP;
    }

    @Override
    protected List<JobTask> doGenerate(final JobTaskGenerateContext context) {
        return super.doGenerate(context);
    }

    @Override
    protected MapReduceArgsStrDTO getJobParams(JobTaskGenerateContext context) {
        // 这里复用map reduce的参数能力
        MapReduceArgsStrDTO mapReduceArgsStrDTO = new MapReduceArgsStrDTO();
        mapReduceArgsStrDTO.setArgsStr(context.getArgsStr());
        return mapReduceArgsStrDTO;
    }
}
