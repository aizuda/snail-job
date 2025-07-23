package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.service.service.impl.AbstractJobBatchService;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobBatchQueryVO;
import com.aizuda.snailjob.server.web.model.request.JobBatchResponseVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.service.JobWebBatchService;
import com.aizuda.snailjob.server.web.service.convert.JobBatchResponseVOConverter;
import com.aizuda.snailjob.server.web.service.handler.JobHandler;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:55
 * @since ：2.4.0
 */
@Service("jobWebBatchService")
@RequiredArgsConstructor
public class JobWebBatchServiceImpl extends AbstractJobBatchService implements JobWebBatchService {
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final JobHandler jobHandler;

    @Override
    public PageResult<List<JobBatchResponseVO>> getJobBatchPage(final JobBatchQueryVO queryVO) {
        PageDTO<JobTaskBatch> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        List<String> groupNames = UserSessionUtils.getGroupNames(queryVO.getGroupName());

        // 如果当前用户为普通用户, 且计算后组名条件为空, 不能查询
        if (userSessionVO.isUser() && CollUtil.isEmpty(groupNames)) {
            return new PageResult<>(pageDTO, Collections.emptyList());
        }

        QueryWrapper<JobTaskBatch> wrapper = new QueryWrapper<JobTaskBatch>()
                .eq("batch.namespace_id", userSessionVO.getNamespaceId())
                .eq("batch.system_task_type", SyetemTaskTypeEnum.JOB.getType())
                .eq(queryVO.getJobId() != null, "batch.job_id", queryVO.getJobId())
                .in(CollUtil.isNotEmpty(groupNames), "batch.group_name", groupNames)
                .in(ObjUtil.isNotEmpty(queryVO.getTaskBatchStatus()), "batch.task_batch_status", queryVO.getTaskBatchStatus())
                .between(ObjUtil.isAllNotEmpty(queryVO.getStartDt(), queryVO.getEndDt()),
                        "batch.create_dt", queryVO.getStartDt(), queryVO.getEndDt())
                .eq("batch.deleted", 0)
                .orderByDesc("batch.id");
        List<JobBatchResponseDO> batchResponseDOList = jobTaskBatchMapper.selectJobBatchPageList(pageDTO, wrapper);
        List<JobBatchResponseVO> batchResponseVOList = JobBatchResponseVOConverter.INSTANCE.convertList(
                batchResponseDOList);
        return new PageResult<>(pageDTO, batchResponseVOList);
    }

//    @Override
//    public JobBatchResponseVO getJobBatchDetail(final Long id) {
//        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectById(id);
//        if (Objects.isNull(jobTaskBatch)) {
//            return null;
//        }
//
//        Job job = jobMapper.selectById(jobTaskBatch.getJobId());
//        JobBatchResponseVO jobBatchResponseVO = JobBatchResponseVOConverter.INSTANCE.convert(jobTaskBatch, job);
//
//        if (jobTaskBatch.getSystemTaskType().equals(SyetemTaskTypeEnum.WORKFLOW.getType())) {
//            WorkflowNode workflowNode = workflowNodeMapper.selectById(jobTaskBatch.getWorkflowNodeId());
//            jobBatchResponseVO.setNodeName(workflowNode.getNodeName());
//
//            // 回调节点
//            if (SystemConstants.CALLBACK_JOB_ID.equals(jobTaskBatch.getJobId())) {
//                jobBatchResponseVO.setCallback(JsonUtil.parseObject(workflowNode.getNodeInfo(), CallbackConfig.class));
//                jobBatchResponseVO.setExecutionAt(jobTaskBatch.getCreateDt());
//                return jobBatchResponseVO;
//            }
//
//            // 条件节点
//            if (SystemConstants.DECISION_JOB_ID.equals(jobTaskBatch.getJobId())) {
//                jobBatchResponseVO.setDecision(JsonUtil.parseObject(workflowNode.getNodeInfo(), DecisionConfig.class));
//                jobBatchResponseVO.setExecutionAt(jobTaskBatch.getCreateDt());
//                return jobBatchResponseVO;
//            }
//        }
//
//        return jobBatchResponseVO;
//    }

    @Override
    public boolean stop(Long taskBatchId) {
        return jobHandler.stop(taskBatchId);
    }

    @Override
    @Transactional
    public Boolean retry(Long taskBatchId) {
        return jobHandler.retry(taskBatchId);
    }

    @Override
    public Boolean deleteJobBatchByIds(Set<Long> ids) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        jobHandler.deleteJobTaskBatchByIds(ids, namespaceId);
        return Boolean.TRUE;
    }
}
