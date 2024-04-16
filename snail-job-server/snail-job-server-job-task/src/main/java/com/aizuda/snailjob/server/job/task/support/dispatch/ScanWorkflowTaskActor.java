package com.aizuda.snailjob.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.util.RandomUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheConsumerGroup;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.dto.ScanTask;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.job.task.dto.WorkflowPartitionTaskDTO;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.GroupConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.Workflow;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaowoniu
 * @date 2023-12-21 21:15:29
 * @since 2.6.0
 */
@Component(ActorGenerator.SCAN_WORKFLOW_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class ScanWorkflowTaskActor extends AbstractActor {
    private final WorkflowMapper workflowMapper;
    private final SystemProperties systemProperties;
    private final GroupConfigMapper groupConfigMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ScanTask.class, config -> {

            try {
                doScan(config);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("Data scanner processing exception. [{}]", config, e);
            }

        }).build();
    }

    private void doScan(ScanTask scanTask) {
        PartitionTaskUtils.process(startId -> listAvailableWorkflows(startId, scanTask),
                this::processPartitionTasks, 0);
    }

    private void processPartitionTasks(List<? extends PartitionTask> partitionTasks) {
        List<Workflow> waitUpdateJobs = new ArrayList<>();
        List<WorkflowTaskPrepareDTO> waitExecWorkflows = new ArrayList<>();
        long now = DateUtils.toNowMilli();
        for (PartitionTask partitionTask : partitionTasks) {
            WorkflowPartitionTaskDTO workflowPartitionTaskDTO = (WorkflowPartitionTaskDTO) partitionTask;
            processWorkflow(workflowPartitionTaskDTO, waitUpdateJobs, waitExecWorkflows, now);
        }

        // 批量更新
        workflowMapper.updateBatchNextTriggerAtById(waitUpdateJobs);

        for (final WorkflowTaskPrepareDTO waitExecTask : waitExecWorkflows) {
            // 执行预处理阶段
            ActorRef actorRef = ActorGenerator.workflowTaskPrepareActor();
            waitExecTask.setTaskExecutorScene(JobTaskExecutorSceneEnum.AUTO_WORKFLOW.getType());
            actorRef.tell(waitExecTask, actorRef);
        }
    }

    private void processWorkflow(WorkflowPartitionTaskDTO partitionTask, List<Workflow> waitUpdateWorkflows,
                                 List<WorkflowTaskPrepareDTO> waitExecJobs, long now) {
        CacheConsumerGroup.addOrUpdate(partitionTask.getGroupName(), partitionTask.getNamespaceId());

        // 更新下次触发时间
        Long nextTriggerAt = calculateNextTriggerTime(partitionTask, now);

        Workflow workflow = new Workflow();
        workflow.setId(partitionTask.getId());
        workflow.setNextTriggerAt(nextTriggerAt);
        waitUpdateWorkflows.add(workflow);

        waitExecJobs.add(WorkflowTaskConverter.INSTANCE.toWorkflowTaskPrepareDTO(partitionTask));

    }

    private Long calculateNextTriggerTime(WorkflowPartitionTaskDTO partitionTask, long now) {

        long nextTriggerAt = partitionTask.getNextTriggerAt();
        if ((nextTriggerAt + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD)) < now) {
            long randomMs = (long) (RandomUtil.randomDouble(0, 4, 2, RoundingMode.UP) * 1000);
            nextTriggerAt = now + randomMs;
            partitionTask.setNextTriggerAt(nextTriggerAt);
        }

        // 更新下次触发时间
        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(partitionTask.getTriggerType());
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(partitionTask.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(nextTriggerAt);

        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }

    private List<WorkflowPartitionTaskDTO> listAvailableWorkflows(Long startId, ScanTask scanTask) {
        if (CollectionUtils.isEmpty(scanTask.getBuckets())) {
            return Collections.emptyList();
        }

        List<Workflow> workflows = workflowMapper.selectPage(new PageDTO<>(0, systemProperties.getJobPullPageSize()),
                new LambdaQueryWrapper<Workflow>()
                        .select(Workflow::getId, Workflow::getGroupName, Workflow::getNextTriggerAt, Workflow::getTriggerType,
                                Workflow::getTriggerInterval, Workflow::getExecutorTimeout, Workflow::getNamespaceId,
                                Workflow::getFlowInfo, Workflow::getBlockStrategy)
                        .eq(Workflow::getWorkflowStatus, StatusEnum.YES.getStatus())
                        .eq(Workflow::getDeleted, StatusEnum.NO.getStatus())
                        .in(Workflow::getBucketIndex, scanTask.getBuckets())
                        .le(Workflow::getNextTriggerAt, DateUtils.toNowMilli() + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD))
                        .ge(Workflow::getId, startId)
                        .orderByAsc(Workflow::getId)
        ).getRecords();

        // 过滤已关闭的组
        if (!CollectionUtils.isEmpty(workflows)) {
            List<String> groupConfigs = groupConfigMapper.selectList(new LambdaQueryWrapper<GroupConfig>()
                    .select(GroupConfig::getGroupName)
                    .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus())
                    .in(GroupConfig::getGroupName, workflows.stream().map(Workflow::getGroupName).collect(Collectors.toList())))
                .stream().map(GroupConfig::getGroupName).collect(Collectors.toList());
            workflows = workflows.stream().filter(workflow -> groupConfigs.contains(workflow.getGroupName())).collect(Collectors.toList());
        }

        return WorkflowTaskConverter.INSTANCE.toWorkflowPartitionTaskList(workflows);
    }
}
