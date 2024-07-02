package com.aizuda.snailjob.server.job.task.support.executor.job;

import akka.actor.ActorRef;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-10-06 17:33:51
 * @since 2.4.0
 */
@Component
@Slf4j
public class ShardingJobExecutor extends AbstractJobExecutor {

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.SHARDING;
    }

    @Override
    protected void doExecute(JobExecutorContext context) {
        List<JobTask> taskList = context.getTaskList();
        for (int i = 0; i < taskList.size(); i++) {
            JobTask jobTask = taskList.get(i);
            if (StrUtil.isBlank(jobTask.getClientInfo())) {
                return;
            }
            RealJobExecutorDTO realJobExecutor = JobTaskConverter.INSTANCE.toRealJobExecutorDTO(context, jobTask);
            realJobExecutor.setClientId(ClientInfoUtils.clientId(jobTask.getClientInfo()));
            realJobExecutor.setShardingIndex(i);
            realJobExecutor.setShardingTotal(taskList.size());
            ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
            actorRef.tell(realJobExecutor, actorRef);
        }

    }
}
