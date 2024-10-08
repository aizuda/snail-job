package com.aizuda.snailjob.server.job.task.support.result.job;

import akka.actor.ActorRef;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.job.task.dto.ReduceTaskDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.snailjob.template.datasource.persistence.mapper.GroupConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.aizuda.snailjob.common.core.enums.MapReduceStageEnum.MAP;
import static com.aizuda.snailjob.common.core.enums.MapReduceStageEnum.MERGE_REDUCE;
import static com.aizuda.snailjob.common.core.enums.MapReduceStageEnum.REDUCE;

/**
 * @author: opensnail
 * @date : 2024-09-04
 * @since :1.2.0
 */
@Component
public class MapReduceJobExecutorHandler extends AbstractJobExecutorResultHandler {

    public MapReduceJobExecutorHandler(
        final JobTaskMapper jobTaskMapper,
        final JobTaskBatchMapper jobTaskBatchMapper,
        final WorkflowBatchHandler workflowBatchHandler,
        final GroupConfigMapper groupConfigMapper) {
        super(jobTaskMapper, jobTaskBatchMapper, workflowBatchHandler, groupConfigMapper);
    }

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.MAP_REDUCE;
    }

    @Override
    protected void doHandleSuccess(final JobExecutorResultContext context) {
        // 判断是否需要创建Reduce任务
        context.setCreateReduceTask(needReduceTask(context));
    }

    @Override
    protected void doHandleStop(final JobExecutorResultContext context) {

    }

    @Override
    protected void doHandleFail(final JobExecutorResultContext context) {

    }

    @Override
    protected boolean updateStatus(final JobExecutorResultContext context, final Integer status) {
        if (context.isCreateReduceTask()) {
            // 此时中断批次完成，需要开启reduce任务
            return false;
        }
        return super.updateStatus(context, status);
    }

    /**
     * 若需要执行reduce则返回false 不需要更新批次状态， 否则需要更新批次状态
     *
     * @param context 需要执行批次完成所需的参数信息
     * @return true-需要reduce false-不需要reduce
     */
    private boolean needReduceTask(final JobExecutorResultContext context) {
        int mrStage;

        int reduceCount = 0;
        int mapCount = 0;
        for (final JobTask jobTask : context.getJobTaskList()) {
            if (Objects.isNull(jobTask.getMrStage())) {
                continue;
            }

            // 存在MERGE_REDUCE任务了不需要生成
            if (MERGE_REDUCE.getStage() == jobTask.getMrStage()) {
                return false;
            }

            // REDUCE任务累加
            if (REDUCE.getStage() == jobTask.getMrStage()) {
                reduceCount++;
                continue;
            }

            // MAP任务累加
            if (MAP.getStage() == jobTask.getMrStage()) {
                mapCount++;
            }
        }

        // 若存在2个以上的reduce任务则开启merge reduce任务
        if (reduceCount > 1) {
            mrStage = MERGE_REDUCE.getStage();
        } else if (mapCount == context.getJobTaskList().size()) {
            // 若都是MAP任务则开启Reduce任务
            mrStage = REDUCE.getStage();
        } else {
            // 若既不是MAP也是不REDUCE则是其他类型的任务，直接返回即可
            return false;
        }

        // 开启reduce or mergeReduce阶段
        try {
            ReduceTaskDTO reduceTaskDTO = JobTaskConverter.INSTANCE.toReduceTaskDTO(context);
            reduceTaskDTO.setMrStage(mrStage);
            ActorRef actorRef = ActorGenerator.jobReduceActor();
            actorRef.tell(reduceTaskDTO, actorRef);
            return true;
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("tell reduce actor error", e);
        }

        return false;
    }

    @Override
    protected void openNextWorkflowNode(JobExecutorResultContext context) {
        if (context.isCreateReduceTask()){
            // 任务暂未完成，无需开启后续节点；更新上下文
            return;
        }
        super.openNextWorkflowNode(context);
    }
}
