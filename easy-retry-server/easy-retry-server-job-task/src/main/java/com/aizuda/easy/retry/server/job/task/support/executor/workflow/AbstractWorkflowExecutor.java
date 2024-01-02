package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import com.aizuda.easy.retry.server.job.task.support.LockExecutor;
import com.aizuda.easy.retry.server.job.task.support.WorkflowExecutor;
import com.aizuda.easy.retry.server.job.task.support.handler.DistributedLockHandler;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:15:19
 * @since 2.6.0
 */
@Slf4j
public abstract class AbstractWorkflowExecutor implements WorkflowExecutor, InitializingBean {

    @Autowired
    private DistributedLockHandler distributedLockHandler;
    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;

    @Override
    @Transactional
    public void execute(WorkflowExecutorContext context) {
        distributedLockHandler.lockAndProcessAfterUnlockDel("workflow_execute_" + context.getWorkflowNodeId(), "PT5S",
            () -> {

                Long total = jobTaskBatchMapper.selectCount(new LambdaQueryWrapper<JobTaskBatch>()
                    .eq(JobTaskBatch::getWorkflowTaskBatchId, context.getWorkflowTaskBatchId())
                    .eq(JobTaskBatch::getWorkflowNodeId, context.getWorkflowNodeId())
                );
                if (total > 0) {
                    log.warn("任务节点[{}]已被执行，请勿重复执行", context.getWorkflowNodeId());
                    return;
                }

                doExecute(context);
            });

    }

    protected abstract void doExecute(WorkflowExecutorContext context);

    @Override
    public void afterPropertiesSet() throws Exception {
        WorkflowExecutorFactory.registerJobExecutor(getWorkflowNodeType(), this);
    }
}
