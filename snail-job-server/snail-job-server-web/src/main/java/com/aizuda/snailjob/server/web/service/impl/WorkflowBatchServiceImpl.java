package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.dto.JobTaskConfig;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.job.task.support.cache.MutableGraphCache;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.request.WorkflowBatchQueryVO;
import com.aizuda.snailjob.server.web.model.response.JobBatchResponseVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowBatchResponseVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.snailjob.server.web.service.WorkflowBatchService;
import com.aizuda.snailjob.server.web.service.convert.JobBatchResponseVOConverter;
import com.aizuda.snailjob.server.web.service.convert.WorkflowConverter;
import com.aizuda.snailjob.server.web.service.handler.WorkflowHandler;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.WorkflowBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.*;
import com.aizuda.snailjob.template.datasource.persistence.po.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.graph.MutableGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xiaowoniu
 * @date 2023-12-23 17:48:31
 * @since 2.6.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowBatchServiceImpl implements WorkflowBatchService {

    private static final Integer NOT_HANDLE_STATUS = 99;
    private static final Integer WORKFLOW_DECISION_FAILED_STATUS = 98;
    private final WorkflowTaskBatchMapper workflowTaskBatchMapper;
    private final WorkflowMapper workflowMapper;
    private final WorkflowNodeMapper workflowNodeMapper;
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final WorkflowHandler workflowHandler;
    private final WorkflowBatchHandler workflowBatchHandler;
    private final JobMapper jobMapper;

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
            .likeRight(StrUtil.isNotBlank(queryVO.getWorkflowName()), "flow.workflow_name", queryVO.getWorkflowName())
            .between(ObjUtil.isNotNull(queryVO.getDatetimeRange()),
                "batch.create_dt", queryVO.getStartDt(), queryVO.getEndDt())
            .eq("batch.deleted", 0)
            .orderByDesc("batch.id");
        List<WorkflowBatchResponseDO> batchResponseDOList = workflowTaskBatchMapper.selectWorkflowBatchPageList(pageDTO,
            wrapper);

        List<WorkflowBatchResponseVO> batchResponseVOList =
            WorkflowConverter.INSTANCE.convertListToWorkflowBatchList(batchResponseDOList);

        return new PageResult<>(pageDTO, batchResponseVOList);
    }

    @Override
    public WorkflowDetailResponseVO getWorkflowBatchDetail(Long id) {

        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchMapper.selectOne(
            new LambdaQueryWrapper<WorkflowTaskBatch>()
                .eq(WorkflowTaskBatch::getId, id)
                .eq(WorkflowTaskBatch::getNamespaceId, UserSessionUtils.currentUserSession().getNamespaceId()));
        if (Objects.isNull(workflowTaskBatch)) {
            return null;
        }

        Workflow workflow = workflowMapper.selectById(workflowTaskBatch.getWorkflowId());

        WorkflowDetailResponseVO responseVO = WorkflowConverter.INSTANCE.convert(workflow);
        List<WorkflowNode> workflowNodes = workflowNodeMapper.selectList(new LambdaQueryWrapper<WorkflowNode>()
            .eq(WorkflowNode::getDeleted, StatusEnum.NO.getStatus())
            .eq(WorkflowNode::getWorkflowId, workflow.getId()));

        List<Job> jobs = jobMapper.selectList(
            new LambdaQueryWrapper<Job>()
                .in(Job::getId, StreamUtils.toSet(workflowNodes, WorkflowNode::getJobId)));

        Map<Long, Job> jobMap = StreamUtils.toIdentityMap(jobs, Job::getId);

        List<JobTaskBatch> alJobTaskBatchList = jobTaskBatchMapper.selectList(
            new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getWorkflowTaskBatchId, id)
                .orderByDesc(JobTaskBatch::getId));

        Map<Long, List<JobTaskBatch>> jobTaskBatchMap = StreamUtils.groupByKey(alJobTaskBatchList,
            JobTaskBatch::getWorkflowNodeId);
        List<WorkflowDetailResponseVO.NodeInfo> nodeInfos = WorkflowConverter.INSTANCE.convertList(workflowNodes);

        String flowInfo = workflowTaskBatch.getFlowInfo();
        MutableGraph<Long> graph = MutableGraphCache.getOrDefault(id, flowInfo);

        Set<Long> allNoOperationNode = Sets.newHashSet();
        Map<Long, WorkflowDetailResponseVO.NodeInfo> workflowNodeMap = nodeInfos.stream()
            .peek(nodeInfo -> {

                JobTaskConfig jobTask = nodeInfo.getJobTask();
                if (Objects.nonNull(jobTask)) {
                    jobTask.setJobName(jobMap.getOrDefault(jobTask.getJobId(), new Job()).getJobName());
                }

                List<JobTaskBatch> jobTaskBatchList = jobTaskBatchMap.get(nodeInfo.getId());
                if (CollUtil.isNotEmpty(jobTaskBatchList)) {
                    jobTaskBatchList = jobTaskBatchList.stream()
                        .sorted(Comparator.comparingInt(JobTaskBatch::getTaskBatchStatus))
                        .collect(Collectors.toList());
                    nodeInfo.setJobBatchList(
                        JobBatchResponseVOConverter.INSTANCE.convertListToJobBatchList(jobTaskBatchList));

                    // 取第最新的一条状态
                    JobTaskBatch jobTaskBatch = jobTaskBatchList.get(0);
                    if (JobOperationReasonEnum.WORKFLOW_DECISION_FAILED.getReason()
                        == jobTaskBatch.getOperationReason()) {
                        // 前端展示使用
                        nodeInfo.setTaskBatchStatus(WORKFLOW_DECISION_FAILED_STATUS);
                    } else {
                        nodeInfo.setTaskBatchStatus(jobTaskBatch.getTaskBatchStatus());
                    }

                    if (jobTaskBatchList.stream()
                        .filter(Objects::nonNull)
                        .anyMatch(WorkflowBatchServiceImpl::isNoOperation)) {
                        // 当前节点下面的所有节点都是无需处理的节点
                        Set<Long> allDescendants = MutableGraphCache.getAllDescendants(graph, nodeInfo.getId());
                        allNoOperationNode.addAll(allDescendants);
                    } else {
                        // 删除被误添加的节点
                        allNoOperationNode.remove(nodeInfo.getId());
                    }

                } else {
                    if (JobTaskBatchStatusEnum.NOT_SUCCESS.contains(workflowTaskBatch.getTaskBatchStatus())) {
                        allNoOperationNode.add(nodeInfo.getId());
                    }
                }
            })
            .collect(Collectors.toMap(WorkflowDetailResponseVO.NodeInfo::getId, Function.identity()));

        for (Long noOperationNodeId : allNoOperationNode) {
            WorkflowDetailResponseVO.NodeInfo nodeInfo = workflowNodeMap.get(noOperationNodeId);
            List<JobTaskBatch> jobTaskBatches = jobTaskBatchMap.get(nodeInfo.getId());

            if (CollUtil.isNotEmpty(jobTaskBatches)) {
                jobTaskBatches = jobTaskBatches.stream()
                    .sorted(Comparator.comparingInt(JobTaskBatch::getTaskBatchStatus))
                    .collect(Collectors.toList());
                nodeInfo.setJobBatchList(
                    JobBatchResponseVOConverter.INSTANCE.convertListToJobBatchList(jobTaskBatches));
            } else {
                JobBatchResponseVO jobBatchResponseVO = new JobBatchResponseVO();
                JobTaskConfig jobTask = nodeInfo.getJobTask();
                if (Objects.nonNull(jobTask)) {
                    jobBatchResponseVO.setJobId(jobTask.getJobId());
                }
                // 只为前端展示提供
                nodeInfo.setTaskBatchStatus(NOT_HANDLE_STATUS);
                jobBatchResponseVO.setTaskBatchStatus(NOT_HANDLE_STATUS);
                jobBatchResponseVO.setOperationReason(JobOperationReasonEnum.WORKFLOW_NODE_NO_REQUIRED.getReason());
                nodeInfo.setJobBatchList(Lists.newArrayList(jobBatchResponseVO));
            }
        }

        try {
            // 反序列化构建图
            WorkflowDetailResponseVO.NodeConfig config = workflowHandler.buildNodeConfig(graph, SystemConstants.ROOT,
                new HashMap<>(), workflowNodeMap);
            responseVO.setNodeConfig(config);
        } catch (Exception e) {
            log.error("反序列化失败. json:[{}]", flowInfo, e);
            throw new SnailJobServerException("查询工作流批次详情失败");
        }

        return responseVO;
    }

    @Override
    public Boolean stop(Long id) {
        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchMapper.selectById(id);
        Assert.notNull(workflowTaskBatch, () -> new SnailJobServerException("workflow batch can not be null."));

        workflowBatchHandler.stop(id, JobOperationReasonEnum.MANNER_STOP.getReason());
        return Boolean.TRUE;
    }

}
