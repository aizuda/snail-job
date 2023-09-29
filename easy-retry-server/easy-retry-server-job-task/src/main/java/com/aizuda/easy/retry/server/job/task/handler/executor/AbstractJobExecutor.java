package com.aizuda.easy.retry.server.job.task.handler.executor;

import com.aizuda.easy.retry.server.job.task.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.generator.task.JobTaskGenerateContext;
import com.aizuda.easy.retry.server.job.task.generator.task.JobTaskGenerator;
import com.aizuda.easy.retry.server.job.task.generator.task.JobTaskGeneratorFactory;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-10-03 22:13:04
 * @since 2.4.0
 */
public abstract class AbstractJobExecutor implements JobExecutor, InitializingBean {

    @Autowired
    private JobMapper jobMapper;

    @Override
    public void execute(JobExecutorContext context) {

        // 生成任务
        JobTaskGenerator taskInstance = JobTaskGeneratorFactory.getTaskInstance(getTaskInstanceType().getType());
        JobTaskGenerateContext instanceGenerateContext = JobTaskConverter.INSTANCE.toJobTaskInstanceGenerateContext(context);
        instanceGenerateContext.setTaskBatchId(context.getTaskBatchId());
        List<JobTask> taskList = taskInstance.generate(instanceGenerateContext);
        if (CollectionUtils.isEmpty(taskList)) {
            return;
        }

        Job job = jobMapper.selectById(context.getJobId());
        context.setJob(job);
        context.setTaskList(taskList);

        doExecute(context);
    }

    protected abstract void doExecute(JobExecutorContext context);

    @Override
    public void afterPropertiesSet() throws Exception {
        JobExecutorFactory.registerJobExecutor(getTaskInstanceType(), this);
    }
}
