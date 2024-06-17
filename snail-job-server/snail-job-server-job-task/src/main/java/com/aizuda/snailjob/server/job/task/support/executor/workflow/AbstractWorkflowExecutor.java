package com.aizuda.snailjob.server.job.task.support.executor.workflow;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.JobArgsTypeEnum;
import com.aizuda.snailjob.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.support.WorkflowExecutor;
import com.aizuda.snailjob.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.snailjob.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.aizuda.snailjob.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.aizuda.snailjob.server.job.task.support.handler.DistributedLockHandler;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum.WORKFLOW_SUCCESSOR_SKIP_EXECUTION;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:15:19
 * @since 2.6.0
 */
@Slf4j
public abstract class AbstractWorkflowExecutor implements WorkflowExecutor, InitializingBean {

    private static final String KEY = "workflow_execute_{0}_{1}";
    @Autowired
    private DistributedLockHandler distributedLockHandler;
    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;
    @Autowired
    private JobTaskBatchGenerator jobTaskBatchGenerator;
    @Autowired
    protected WorkflowBatchHandler workflowBatchHandler;
    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public void execute(WorkflowExecutorContext context) {
        distributedLockHandler.lockWithDisposableAndRetry(
                () -> {
                    long total = 0;
                    // 条件节点存在并发问题，需要特殊处理
                    if (WorkflowNodeTypeEnum.DECISION.getType() == context.getNodeType()) {
                        List<JobTaskBatch> jobTaskBatches = jobTaskBatchMapper.selectList(new LambdaQueryWrapper<JobTaskBatch>()
                                .select(JobTaskBatch::getOperationReason)
                                .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
                                .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
                        );

                        if (CollUtil.isNotEmpty(jobTaskBatches)) {
                            total = jobTaskBatches.size();
                            JobTaskBatch jobTaskBatch = jobTaskBatches.get(0);
                            if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(jobTaskBatch.getOperationReason())) {
                                context.setEvaluationResult(Boolean.FALSE);
                            } else {
                                context.setEvaluationResult(Boolean.TRUE);
                            }
                        }

                    } else {
                        total = jobTaskBatchMapper.selectCount(new LambdaQueryWrapper<JobTaskBatch>()
                                .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
                                .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
                        );
                    }

                    if (total > 0) {
                        log.warn("任务节点[{}]已被执行，请勿重复执行", context.getWorkflowNodeId());
                        return;
                    }

                    // 合并job task的结果到全局上下文中
                    workflowBatchHandler.mergeAllWorkflowContext(context.getWorkflowTaskBatchId(), context.getTaskBatchId());

                    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(final TransactionStatus status) {

                            if (!preValidate(context)) {
                                return;
                            }
                            beforeExecute(context);

                            doExecute(context);

                            afterExecute(context);
                        }
                    });
                }, MessageFormat.format(KEY, context.getWorkflowTaskBatchId(), context.getWorkflowNodeId()),
                Duration.ofSeconds(10), Duration.ofSeconds(3), 16);

    }

    protected boolean preValidate(WorkflowExecutorContext context) {
        return doPreValidate(context);
    }

    protected abstract boolean doPreValidate(WorkflowExecutorContext context);

    protected abstract void afterExecute(WorkflowExecutorContext context);

    protected abstract void beforeExecute(WorkflowExecutorContext context);

    protected abstract void doExecute(WorkflowExecutorContext context);

    protected JobTaskBatch generateJobTaskBatch(WorkflowExecutorContext context) {
        JobTaskBatchGeneratorContext generatorContext = WorkflowTaskConverter.INSTANCE.toJobTaskBatchGeneratorContext(context);
        return jobTaskBatchGenerator.generateJobTaskBatch(generatorContext);
    }

    protected void workflowTaskExecutor(WorkflowExecutorContext context) {
        WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
        taskExecuteDTO.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
        taskExecuteDTO.setTaskExecutorScene(context.getTaskExecutorScene());
        taskExecuteDTO.setParentId(context.getWorkflowNodeId());
        taskExecuteDTO.setTaskBatchId(context.getTaskBatchId());
        workflowBatchHandler.openNextNode(taskExecuteDTO);
    }

    protected JobTask generateJobTask(WorkflowExecutorContext context, JobTaskBatch jobTaskBatch) {
        // 生成执行任务实例
        JobTask jobTask = new JobTask();
        jobTask.setGroupName(context.getGroupName());
        jobTask.setNamespaceId(context.getNamespaceId());
        jobTask.setJobId(context.getJobId());
        jobTask.setClientInfo(StrUtil.EMPTY);
        jobTask.setTaskBatchId(jobTaskBatch.getId());
        jobTask.setArgsType(JobArgsTypeEnum.TEXT.getArgsType());
        // TODO 待定是否删除
        jobTask.setArgsStr(Optional.ofNullable(context.getTaskResult()).orElse(StrUtil.EMPTY));
        jobTask.setTaskStatus(context.getJobTaskStatus());
        jobTask.setResultMessage(String.valueOf(context.getEvaluationResult()));
        Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new SnailJobServerException("新增任务实例失败"));
        return jobTask;
    }

    @Override
    public void afterPropertiesSet() {
        WorkflowExecutorFactory.registerJobExecutor(getWorkflowNodeType(), this);
    }
}
