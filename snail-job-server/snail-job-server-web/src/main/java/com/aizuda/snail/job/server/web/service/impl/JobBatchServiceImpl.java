package com.aizuda.snail.job.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snail.job.common.core.constant.SystemConstants;
import com.aizuda.snail.job.common.core.util.JsonUtil;
import com.aizuda.snail.job.server.common.dto.CallbackConfig;
import com.aizuda.snail.job.server.common.dto.DecisionConfig;
import com.aizuda.snail.job.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snail.job.server.web.model.base.PageResult;
import com.aizuda.snail.job.server.web.model.request.JobBatchQueryVO;
import com.aizuda.snail.job.server.web.model.request.UserSessionVO;
import com.aizuda.snail.job.server.web.model.response.JobBatchResponseVO;
import com.aizuda.snail.job.server.web.service.JobBatchService;
import com.aizuda.snail.job.server.web.service.convert.JobBatchResponseVOConverter;
import com.aizuda.snail.job.server.web.service.handler.JobHandler;
import com.aizuda.snail.job.server.web.util.UserSessionUtils;
import com.aizuda.snail.job.template.datasource.persistence.dataobject.JobBatchQueryDO;
import com.aizuda.snail.job.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.snail.job.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snail.job.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snail.job.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.snail.job.template.datasource.persistence.po.Job;
import com.aizuda.snail.job.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.snail.job.template.datasource.persistence.po.WorkflowNode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

        JobBatchQueryDO queryDO = new JobBatchQueryDO();
        if (StrUtil.isNotBlank(queryVO.getJobName())) {
            queryDO.setJobName(queryVO.getJobName() + "%");
        }

        queryDO.setJobId(queryVO.getJobId());
        queryDO.setTaskBatchStatus(queryVO.getTaskBatchStatus());
        queryDO.setGroupNames(groupNames);
        queryDO.setNamespaceId(userSessionVO.getNamespaceId());
        QueryWrapper<JobTaskBatch> wrapper = new QueryWrapper<JobTaskBatch>()
                .eq("a.namespace_id", queryDO.getNamespaceId())
                .eq("a.system_task_type", 3)
                .eq(queryDO.getJobId() != null, "a.job_id", queryDO.getJobId())
                .in(CollUtil.isNotEmpty(queryDO.getGroupNames()), "a.group_name", queryDO.getGroupNames())
                .eq(queryDO.getTaskBatchStatus() != null, "task_batch_status", queryDO.getTaskBatchStatus())
                .eq(StrUtil.isNotBlank(queryDO.getJobName()), "job_name", queryDO.getJobName())
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
