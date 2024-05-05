package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.dto.CallbackConfig;
import com.aizuda.snailjob.server.common.dto.DecisionConfig;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobBatchQueryVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.JobBatchResponseVO;
import com.aizuda.snailjob.server.web.service.JobBatchService;
import com.aizuda.snailjob.server.web.service.convert.JobBatchResponseVOConverter;
import com.aizuda.snailjob.server.web.service.handler.JobHandler;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowNode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:55
 * @since ：2.4.0
 */
@Service
@RequiredArgsConstructor
public class JobBatchServiceImpl implements JobBatchService {

    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final JobMapper jobMapper;
    private final WorkflowNodeMapper workflowNodeMapper;
    private final JobHandler jobHandler;

    @Override
    public PageResult<List<JobBatchResponseVO>> getJobBatchPage(final JobBatchQueryVO queryVO) {
        PageDTO<JobTaskBatch> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        if (!UserSessionUtils.hasGroupPermission(queryVO.getGroupName())) {
            return new PageResult<>(pageDTO, Lists.newArrayList());
        }

        QueryWrapper<JobTaskBatch> wrapper = new QueryWrapper<JobTaskBatch>()
            .eq("a.namespace_id", userSessionVO.getNamespaceId())
            .eq("a.system_task_type", SyetemTaskTypeEnum.JOB.getType())
            .eq(queryVO.getJobId() != null, "a.job_id", queryVO.getJobId())
            .eq(StrUtil.isNotBlank(queryVO.getGroupName()), "a.group_name", queryVO.getGroupName())
            .eq(queryVO.getTaskBatchStatus() != null, "task_batch_status", queryVO.getTaskBatchStatus())
            .likeRight(StrUtil.isNotBlank(queryVO.getJobName()), "job_name", queryVO.getJobName())
            .eq("a.deleted", 0)
            .orderByDesc("a.id");
        List<JobBatchResponseDO> batchResponseDOList = jobTaskBatchMapper.selectJobBatchPageList(pageDTO, wrapper);
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
        return jobHandler.stop(taskBatchId);
    }

    @Override
    @Transactional
    public Boolean retry(Long taskBatchId) {
        return jobHandler.retry(taskBatchId);
    }


}
