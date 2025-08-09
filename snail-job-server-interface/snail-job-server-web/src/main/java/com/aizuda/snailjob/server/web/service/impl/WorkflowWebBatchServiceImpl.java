package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.snailjob.server.service.service.impl.AbstractWorkflowBatchService;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.request.WorkflowBatchQueryVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowBatchResponseVO;
import com.aizuda.snailjob.server.web.service.WorkflowWebBatchService;
import com.aizuda.snailjob.server.web.service.convert.WorkflowWebConverter;
import com.aizuda.snailjob.server.web.service.handler.JobHandler;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.WorkflowBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.*;
import com.aizuda.snailjob.template.datasource.persistence.po.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author xiaowoniu
 * @date 2023-12-23 17:48:31
 * @since 2.6.0
 */
@Service("WorkflowWebBatchCommonService")
@Slf4j
@RequiredArgsConstructor
public class WorkflowWebBatchServiceImpl extends AbstractWorkflowBatchService implements WorkflowWebBatchService {

    private final WorkflowTaskBatchMapper workflowTaskBatchMapper;
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final WorkflowBatchHandler workflowBatchHandler;
    private final JobHandler jobHandler;

    private static boolean isNoOperation(JobTaskBatch i) {
        return JobOperationReasonEnum.WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(i.getOperationReason())
                || i.getTaskBatchStatus() == JobTaskBatchStatusEnum.STOP.getStatus();
    }

    @Override
    public PageResult<List<WorkflowBatchResponseVO>> listPage(WorkflowBatchQueryVO queryVO) {
        PageDTO<JobTaskBatch> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        List<String> groupNames = UserSessionUtils.getGroupNames(queryVO.getGroupName());

        // 如果当前用户为普通用户, 且计算后组名条件为空, 不能查询
        if (userSessionVO.isUser() && CollUtil.isEmpty(groupNames)) {
            return new PageResult<>(pageDTO, Collections.emptyList());
        }

        QueryWrapper<WorkflowTaskBatch> wrapper = new QueryWrapper<WorkflowTaskBatch>()
                .eq("batch.namespace_id", userSessionVO.getNamespaceId())
                .eq(queryVO.getWorkflowId() != null, "batch.workflow_id", queryVO.getWorkflowId())
                .in(CollUtil.isNotEmpty(groupNames), "batch.group_name", groupNames)
                .eq(queryVO.getTaskBatchStatus() != null, "batch.task_batch_status", queryVO.getTaskBatchStatus())
                .between(ObjUtil.isNotNull(queryVO.getDatetimeRange()),
                        "batch.create_dt", queryVO.getStartDt(), queryVO.getEndDt())
                .eq("batch.deleted", 0)
                .orderByDesc("batch.id");
        List<WorkflowBatchResponseDO> batchResponseDOList = workflowTaskBatchMapper.selectWorkflowBatchPageList(pageDTO,
                wrapper);

        List<WorkflowBatchResponseVO> batchResponseVOList =
                WorkflowWebConverter.INSTANCE.convertListToWorkflowBatchList(batchResponseDOList);

        return new PageResult<>(pageDTO, batchResponseVOList);
    }

    @Override
    public Boolean stop(Long id) {
        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchMapper.selectById(id);
        Assert.notNull(workflowTaskBatch, () -> new SnailJobServerException("workflow batch can not be null."));
        Assert.isTrue(JobTaskBatchStatusEnum.NOT_COMPLETE.contains(workflowTaskBatch.getTaskBatchStatus()),
            () -> new SnailJobServerException("workflow batch status completed."));

        workflowBatchHandler.stop(id, JobOperationReasonEnum.MANNER_STOP.getReason());
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public Boolean deleteByIds(Set<Long> ids) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        Assert.isTrue(ids.size() == workflowTaskBatchMapper.delete(new LambdaQueryWrapper<WorkflowTaskBatch>()
                        .eq(WorkflowTaskBatch::getNamespaceId, namespaceId)
                        .in(WorkflowTaskBatch::getId, ids)),
                () -> new SnailJobServerException("Failed to delete workflow task, please check if the task status is closed"));

        List<JobTaskBatch> jobTaskBatches = jobTaskBatchMapper.selectList(new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getNamespaceId, namespaceId)
                .in(JobTaskBatch::getWorkflowTaskBatchId, ids));

        if (CollUtil.isEmpty(jobTaskBatches)) {
            return Boolean.TRUE;
        }

        Set<Long> jobTaskBatchIds = StreamUtils.toSet(jobTaskBatches, JobTaskBatch::getId);
        jobHandler.deleteJobTaskBatchByIds(jobTaskBatchIds, namespaceId);

        return Boolean.TRUE;
    }

}
