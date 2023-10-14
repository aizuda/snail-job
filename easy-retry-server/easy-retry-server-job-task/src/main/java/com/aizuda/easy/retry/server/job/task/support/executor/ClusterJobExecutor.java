package com.aizuda.easy.retry.server.job.task.support.executor;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.easy.retry.server.job.task.enums.TaskTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-10-03 22:12:40
 * @since 2.4.0
 */
@Slf4j
@Component
public class ClusterJobExecutor extends AbstractJobExecutor {


    @Override
    public TaskTypeEnum getTaskInstanceType() {
        return TaskTypeEnum.CLUSTER;
    }

    @Override
    protected void doExecute(JobExecutorContext context) {

        // 调度客户端
        List<JobTask> taskList = context.getTaskList();
        RealJobExecutorDTO realJobExecutor = JobTaskConverter.INSTANCE.toRealJobExecutorDTO(context, taskList.get(0));
        ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
        actorRef.tell(realJobExecutor, actorRef);

    }

}
