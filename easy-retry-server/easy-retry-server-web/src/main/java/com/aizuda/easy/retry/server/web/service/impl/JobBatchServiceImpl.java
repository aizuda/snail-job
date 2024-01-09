package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.dto.CallbackConfig;
import com.aizuda.easy.retry.server.common.dto.DecisionConfig;
import com.aizuda.easy.retry.server.common.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.support.JobPrePareHandler;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;
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
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
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

        if (jobTaskBatch.getTaskType().equals(TaskTypeEnum.JOB.getType())) {
            Job job = jobMapper.selectById(jobTaskBatch.getJobId());
            return JobBatchResponseVOConverter.INSTANCE.toJobBatchResponseVO(jobTaskBatch, job);
        }

        JobBatchResponseVO jobBatchResponseVO = JobBatchResponseVOConverter.INSTANCE.toJobBatchResponseVO(jobTaskBatch);

        // 回调节点
        if (SystemConstants.CALLBACK_JOB_ID.equals(jobTaskBatch.getJobId())) {
            WorkflowNode workflowNode = workflowNodeMapper.selectById(jobTaskBatch.getWorkflowNodeId());
            jobBatchResponseVO.setJobName(workflowNode.getNodeName());
            jobBatchResponseVO.setCallback(JsonUtil.parseObject(workflowNode.getNodeInfo(), CallbackConfig.class));
        }

        // 条件节点
        if (SystemConstants.DECISION_JOB_ID.equals(jobTaskBatch.getJobId())) {
            WorkflowNode workflowNode = workflowNodeMapper.selectById(jobTaskBatch.getWorkflowNodeId());
            jobBatchResponseVO.setJobName(workflowNode.getNodeName());
            jobBatchResponseVO.setDecision(JsonUtil.parseObject(workflowNode.getNodeInfo(), DecisionConfig.class));
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

        TaskStopJobContext taskStopJobContext = new TaskStopJobContext();
        taskStopJobContext.setJobId(job.getId());
        taskStopJobContext.setTaskType(job.getTaskType());
        taskStopJobContext.setGroupName(job.getGroupName());
        taskStopJobContext.setJobOperationReason(JobOperationReasonEnum.MANNER_STOP.getReason());
        taskStopJobContext.setTaskBatchId(jobTaskBatch.getId());
        taskStopJobContext.setForceStop(Boolean.TRUE);
        taskStopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);

        jobTaskStop.stop(taskStopJobContext);

        return Boolean.TRUE;
    }

}
