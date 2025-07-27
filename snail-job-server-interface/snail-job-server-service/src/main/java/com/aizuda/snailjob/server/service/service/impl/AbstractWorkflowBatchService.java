package com.aizuda.snailjob.server.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.model.response.base.JobBatchResponse;
import com.aizuda.snailjob.model.request.JobTaskConfigRequest;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.job.task.support.cache.MutableGraphCache;
import com.aizuda.snailjob.server.service.convert.JobBatchResponseConverter;
import com.aizuda.snailjob.server.service.convert.WorkflowConverter;
import com.aizuda.snailjob.model.response.base.WorkflowDetailResponse;
import com.aizuda.snailjob.server.service.handler.WorkflowHandler;
import com.aizuda.snailjob.server.service.service.WorkflowBatchService;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.snailjob.template.datasource.persistence.po.Workflow;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowNode;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.graph.MutableGraph;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
public abstract class AbstractWorkflowBatchService implements WorkflowBatchService {
    private static final Integer WORKFLOW_DECISION_FAILED_STATUS = 98;

    @Autowired
    protected WorkflowTaskBatchMapper workflowTaskBatchMapper;
    @Autowired
    protected WorkflowMapper workflowMapper;
    @Autowired
    protected WorkflowNodeMapper workflowNodeMapper;
    @Autowired
    protected JobTaskBatchMapper jobTaskBatchMapper;
    @Autowired
    protected JobMapper jobMapper;
    @Autowired
    protected WorkflowHandler workflowHandler;

    @Override
    public <T extends WorkflowDetailResponse> T getWorkflowBatchById(Long workflowBatchId, Class<T> clazz) {
        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchMapper.selectById(workflowBatchId);
        if (Objects.isNull(workflowTaskBatch)) {
            return null;
        }

        Workflow workflow = workflowMapper.selectById(workflowTaskBatch.getWorkflowId());

        T responseVO;
        try {
            responseVO = clazz.getDeclaredConstructor().newInstance();
            WorkflowConverter.INSTANCE.fillCommonFields(workflow, responseVO);
        } catch (Exception e) {
            throw new SnailJobServerException("Failed to get workflow batch by id [{}]", workflowBatchId, e);
        }

        responseVO.setWorkflowBatchStatus(workflowTaskBatch.getTaskBatchStatus());
        responseVO.setWfContext(workflowTaskBatch.getWfContext());
        List<WorkflowNode> workflowNodes = workflowNodeMapper.selectList(new LambdaQueryWrapper<WorkflowNode>()
                .eq(WorkflowNode::getDeleted, StatusEnum.NO.getStatus())
                .eq(WorkflowNode::getWorkflowId, workflow.getId()));

        List<Job> jobs = jobMapper.selectList(
                new LambdaQueryWrapper<Job>()
                        .in(Job::getId, StreamUtils.toSet(workflowNodes, WorkflowNode::getJobId)));

        Map<Long, Job> jobMap = StreamUtils.toIdentityMap(jobs, Job::getId);

        List<JobTaskBatch> alJobTaskBatchList = jobTaskBatchMapper.selectList(
                new LambdaQueryWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getWorkflowTaskBatchId, workflowBatchId)
                        .orderByDesc(JobTaskBatch::getId));

        Map<Long, List<JobTaskBatch>> jobTaskBatchMap = StreamUtils.groupByKey(alJobTaskBatchList,
                JobTaskBatch::getWorkflowNodeId);
        List<WorkflowDetailResponse.NodeInfo> nodeInfos = WorkflowConverter.INSTANCE.convertList(workflowNodes);

        String flowInfo = workflowTaskBatch.getFlowInfo();
        MutableGraph<Long> graph = MutableGraphCache.getOrDefault(workflowBatchId, flowInfo);

        Set<Long> allNoOperationNode = Sets.newHashSet();
        Map<Long, WorkflowDetailResponse.NodeInfo> workflowNodeMap = nodeInfos.stream()
                .peek(nodeInfo -> {

                    JobTaskConfigRequest jobTask = nodeInfo.getJobTask();
                    if (Objects.nonNull(jobTask)) {
                        jobTask.setJobName(jobMap.getOrDefault(jobTask.getJobId(), new Job()).getJobName());
                    }

                    List<JobTaskBatch> jobTaskBatchList = jobTaskBatchMap.get(nodeInfo.getId());
                    if (CollUtil.isNotEmpty(jobTaskBatchList)) {
                        jobTaskBatchList = jobTaskBatchList.stream()
                                .sorted(Comparator.comparingInt(JobTaskBatch::getTaskBatchStatus))
                                .collect(Collectors.toList());
                        nodeInfo.setJobBatchList(
                                JobBatchResponseConverter.INSTANCE.convertListToJobBatchList(jobTaskBatchList));

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
                                .anyMatch(AbstractWorkflowBatchService::isNoOperation)) {
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
                .collect(Collectors.toMap(WorkflowDetailResponse.NodeInfo::getId, Function.identity()));

        for (Long noOperationNodeId : allNoOperationNode) {
            WorkflowDetailResponse.NodeInfo nodeInfo = workflowNodeMap.get(noOperationNodeId);
            List<JobTaskBatch> jobTaskBatches = jobTaskBatchMap.get(nodeInfo.getId());

            if (CollUtil.isNotEmpty(jobTaskBatches)) {
                jobTaskBatches = jobTaskBatches.stream()
                        .sorted(Comparator.comparingInt(JobTaskBatch::getTaskBatchStatus))
                        .collect(Collectors.toList());
                nodeInfo.setJobBatchList(
                        JobBatchResponseConverter.INSTANCE.convertListToJobBatchList(jobTaskBatches));
            } else {
                JobBatchResponse jobBatchResponse = new JobBatchResponse();
                JobTaskConfigRequest jobTask = nodeInfo.getJobTask();
                if (Objects.nonNull(jobTask)) {
                    jobBatchResponse.setJobId(jobTask.getJobId());
                }
                nodeInfo.setJobBatchList(Lists.newArrayList(jobBatchResponse));
            }
        }

        try {
            // 反序列化构建图
            WorkflowDetailResponse.NodeConfig config = workflowHandler.buildNodeConfig(graph, SystemConstants.ROOT,
                    new HashMap<>(), workflowNodeMap);
            responseVO.setNodeConfig(config);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("Deserialization failed. json:[{}]", flowInfo, e);
            throw new SnailJobServerException("Failed to query workflow batch details");
        }

        return responseVO;
    }

    private static boolean isNoOperation(JobTaskBatch i) {
        return JobOperationReasonEnum.WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(i.getOperationReason())
                || i.getTaskBatchStatus() == JobTaskBatchStatusEnum.STOP.getStatus();
    }
}
