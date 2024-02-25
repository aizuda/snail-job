package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.client.model.ExecuteResult;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.dto.CallbackConfig;
import com.aizuda.easy.retry.server.common.dto.DecisionConfig;
import com.aizuda.easy.retry.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.dto.TaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.support.ClientCallbackHandler;
import com.aizuda.easy.retry.server.job.task.support.JobExecutor;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;
import com.aizuda.easy.retry.server.job.task.support.callback.ClientCallbackContext;
import com.aizuda.easy.retry.server.job.task.support.callback.ClientCallbackFactory;
import com.aizuda.easy.retry.server.job.task.support.executor.job.JobExecutorContext;
import com.aizuda.easy.retry.server.job.task.support.executor.job.JobExecutorFactory;
import com.aizuda.easy.retry.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.easy.retry.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.JobBatchQueryVO;
import com.aizuda.easy.retry.server.web.model.request.UserSessionVO;
import com.aizuda.easy.retry.server.web.model.response.JobBatchResponseVO;
import com.aizuda.easy.retry.server.web.service.JobBatchService;
import com.aizuda.easy.retry.server.web.service.convert.JobBatchResponseVOConverter;
import com.aizuda.easy.retry.server.web.util.UserSessionUtils;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.JobBatchQueryDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-12 09:55
 * @since ：2.4.0
 */
@Service
@RequiredArgsConstructor
public class JobBatchServiceImpl implements JobBatchService {

    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final JobMapper jobMapper;
    private final WorkflowNodeMapper workflowNodeMapper;
    private final JobTaskMapper jobTaskMapper;

    @Override
    public PageResult<List<JobBatchResponseVO>> getJobBatchPage(final JobBatchQueryVO queryVO) {

        PageDTO<JobTaskBatch> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        List<String> groupNames = Lists.newArrayList();
        if (userSessionVO.isUser()) {
            groupNames = userSessionVO.getGroupNames();
        }

        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            // 说明当前组不在用户配置的权限中
            if (!CollectionUtils.isEmpty(groupNames) && !groupNames.contains(queryVO.getGroupName())) {
                return new PageResult<>(pageDTO, Lists.newArrayList());
            } else {
                groupNames = Lists.newArrayList(queryVO.getGroupName());
            }
        }

        JobBatchQueryDO jobBatchQueryDO = new JobBatchQueryDO();
        if (StrUtil.isNotBlank(queryVO.getJobName())) {
            jobBatchQueryDO.setJobName(queryVO.getJobName() + "%");
        }

        jobBatchQueryDO.setJobId(queryVO.getJobId());
        jobBatchQueryDO.setTaskBatchStatus(queryVO.getTaskBatchStatus());
        jobBatchQueryDO.setGroupNames(groupNames);
        jobBatchQueryDO.setNamespaceId(userSessionVO.getNamespaceId());
        List<JobBatchResponseDO> batchResponseDOList = jobTaskBatchMapper.selectJobBatchPageList(pageDTO,
                jobBatchQueryDO);

        List<JobBatchResponseVO> batchResponseVOList = JobBatchResponseVOConverter.INSTANCE.toJobBatchResponseVOs(
                batchResponseDOList);

        return new PageResult<>(pageDTO, batchResponseVOList);
    }

    @Override
    public JobBatchResponseVO getJobBatchDetail(final Long id) {
        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectById(id);
        if (Objects.isNull(jobTaskBatch)) {
            return null;
        }

        Job job = jobMapper.selectById(jobTaskBatch.getJobId());
        JobBatchResponseVO jobBatchResponseVO = JobBatchResponseVOConverter.INSTANCE.toJobBatchResponseVO(jobTaskBatch, job);

        if (jobTaskBatch.getSystemTaskType().equals(SyetemTaskTypeEnum.WORKFLOW.getType())) {
            WorkflowNode workflowNode = workflowNodeMapper.selectById(jobTaskBatch.getWorkflowNodeId());
            jobBatchResponseVO.setNodeName(workflowNode.getNodeName());

            // 回调节点
            if (SystemConstants.CALLBACK_JOB_ID.equals(jobTaskBatch.getJobId())) {
                jobBatchResponseVO.setCallback(JsonUtil.parseObject(workflowNode.getNodeInfo(), CallbackConfig.class));
                jobBatchResponseVO.setExecutionAt(jobTaskBatch.getCreateDt());
                return jobBatchResponseVO;
            }

            // 条件节点
            if (SystemConstants.DECISION_JOB_ID.equals(jobTaskBatch.getJobId())) {
                jobBatchResponseVO.setDecision(JsonUtil.parseObject(workflowNode.getNodeInfo(), DecisionConfig.class));
                jobBatchResponseVO.setExecutionAt(jobTaskBatch.getCreateDt());
                return jobBatchResponseVO;
            }
        }

        return jobBatchResponseVO;
    }

    @Override
    public boolean stop(Long taskBatchId) {
        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectById(taskBatchId);
        Assert.notNull(jobTaskBatch, () -> new EasyRetryServerException("job batch can not be null."));

        Job job = jobMapper.selectById(jobTaskBatch.getJobId());
        Assert.notNull(job, () -> new EasyRetryServerException("job can not be null."));

        JobTaskStopHandler jobTaskStop = JobTaskStopFactory.getJobTaskStop(job.getTaskType());

        TaskStopJobContext taskStopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(job);
        taskStopJobContext.setJobOperationReason(JobOperationReasonEnum.MANNER_STOP.getReason());
        taskStopJobContext.setTaskBatchId(jobTaskBatch.getId());
        taskStopJobContext.setForceStop(Boolean.TRUE);
        taskStopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);

        jobTaskStop.stop(taskStopJobContext);

        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public Boolean retry(Long taskBatchId) {
        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectOne(new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getId, taskBatchId)
                .in(JobTaskBatch::getTaskBatchStatus, JobTaskBatchStatusEnum.NOT_SUCCESS)
        );
        Assert.notNull(jobTaskBatch, () -> new EasyRetryServerException("job batch can not be null."));

        // 重置状态为运行中
        jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.RUNNING.getStatus());

        Assert.isTrue(jobTaskBatchMapper.updateById(jobTaskBatch) > 0,
                () -> new EasyRetryServerException("update job batch to running failed."));

        Job job = jobMapper.selectById(jobTaskBatch.getJobId());
        Assert.notNull(job, () -> new EasyRetryServerException("job can not be null."));

        List<JobTask> jobTasks = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
                .in(JobTask::getTaskStatus, Lists.newArrayList(
                        JobTaskStatusEnum.FAIL.getStatus(),
                        JobTaskStatusEnum.STOP.getStatus(),
                        JobTaskStatusEnum.CANCEL.getStatus()
                        )
                )
                .eq(JobTask::getTaskBatchId, taskBatchId));
        Assert.notEmpty(jobTasks, () -> new EasyRetryServerException("job task is empty."));

        for (JobTask jobTask : jobTasks) {
            jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
            Assert.isTrue(jobTaskMapper.updateById(jobTask) > 0,
                    () -> new EasyRetryServerException("update job task to running failed."));
            // 模拟失败重试
            ClientCallbackHandler clientCallback = ClientCallbackFactory.getClientCallback(job.getTaskType());
            ClientCallbackContext context = JobTaskConverter.INSTANCE.toClientCallbackContext(job);
            context.setTaskBatchId(jobTaskBatch.getId());
            context.setTaskId(jobTask.getId());
            context.setTaskStatus(JobTaskStatusEnum.FAIL.getStatus());
            context.setExecuteResult(ExecuteResult.failure(null, "手动重试"));
            clientCallback.callback(context);
        }

        return Boolean.TRUE;
    }


}
