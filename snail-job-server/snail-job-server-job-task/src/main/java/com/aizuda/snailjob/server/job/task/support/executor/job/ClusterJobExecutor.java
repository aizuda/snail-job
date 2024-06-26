package com.aizuda.snailjob.server.job.task.support.executor.job;

import akka.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-10-03 22:12:40
 * @since 2.4.0
 */
@Component
public class ClusterJobExecutor extends AbstractJobExecutor {

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.CLUSTER;
    }

    @Override
    protected void doExecute(JobExecutorContext context) {

        // 调度客户端
        List<JobTask> taskList = context.getTaskList();
        if (CollUtil.isEmpty(taskList)) {
            throw new SnailJobServerException("No executable job task");
        }

        JobTask jobTask = taskList.get(0);
        RealJobExecutorDTO realJobExecutor = JobTaskConverter.INSTANCE.toRealJobExecutorDTO(context, jobTask);
        realJobExecutor.setClientId(ClientInfoUtils.clientId(jobTask.getClientInfo()));
        ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
        actorRef.tell(realJobExecutor, actorRef);

    }

}
