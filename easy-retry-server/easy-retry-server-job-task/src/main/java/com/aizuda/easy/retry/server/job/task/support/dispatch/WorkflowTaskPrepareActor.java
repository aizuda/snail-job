package com.aizuda.easy.retry.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author xiaowoniu
 * @date 2023-12-21 22:41:29
 * @since 2.6.0
 */
@Component(ActorGenerator.WORKFLOW_TASK_PREPARE_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class WorkflowTaskPrepareActor extends AbstractActor {
    private final WorkflowTaskBatchMapper workflowTaskBatchMapper;
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(WorkflowTaskPrepareDTO.class, workflowTaskPrepareDTO -> {
            try {
                doPrepare(workflowTaskPrepareDTO);
            } catch (Exception e) {
                log.error("预处理节点异常", e);
            } finally {
                getContext().stop(getSelf());
            }
        }).build();
    }

    private void doPrepare(WorkflowTaskPrepareDTO workflowTaskPrepareDTO) {
        List<WorkflowTaskBatch> workflowTaskBatches = workflowTaskBatchMapper.selectList(new LambdaQueryWrapper<WorkflowTaskBatch>()
                .eq(WorkflowTaskBatch::getWorkflowId, workflowTaskPrepareDTO.getWorkflowId())
                .eq(WorkflowTaskBatch::getTaskBatchStatus, 0));

        // 则直接创建一个任务批次
        if (CollectionUtils.isEmpty(workflowTaskBatches)) {

        } else {
            // 判断任务是否执行超时
            // 任务是否为发起调用
        }
    }

}
