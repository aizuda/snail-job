package com.aizuda.easy.retry.server.job.task.handler.stop;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.job.task.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.dto.RealStopTaskInstanceDTO;
import com.aizuda.easy.retry.server.job.task.enums.TaskTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 12:59:53
 * @since 2.4.0
 */
@Component
@Slf4j
public class ClusterTaskStopHandler extends AbstractJobTaskStopHandler {


    @Override
    public TaskTypeEnum getTaskType() {
        return TaskTypeEnum.CLUSTER;
    }

    @Override
    public void  doStop(TaskStopJobContext context) {
        List<JobTask> jobTasks = context.getJobTasks();

        String clientId = jobTasks.get(0).getClientId();
        RealStopTaskInstanceDTO taskInstanceDTO = JobTaskConverter.INSTANCE.toRealStopTaskInstanceDTO(context);
        taskInstanceDTO.setClientId(clientId);

        ActorRef actorRef = ActorGenerator.jobRealStopTaskInstanceActor();
        actorRef.tell(taskInstanceDTO, actorRef);

    }



}
