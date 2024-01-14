package com.aizuda.easy.retry.server.web.service.handler;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.dto.CallbackConfig;
import com.aizuda.easy.retry.server.common.dto.DecisionConfig;
import com.aizuda.easy.retry.server.common.dto.JobTaskConfig;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.easy.retry.server.web.service.convert.WorkflowConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.graph.MutableGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author xiaowoniu
 * @date 2023-12-30 23:26:43
 * @since 2.6.0
 */
@Component("webWorkflowHandler")
@Slf4j
@RequiredArgsConstructor
public class WorkflowHandler {

    private final WorkflowNodeMapper workflowNodeMapper;

    /**
     * 根据给定的图、父节点ID、节点配置Map和工作流节点Map，构建节点配置
     *
     * @param graph           图
     * @param parentId        父节点ID
     * @param nodeConfigMap   节点配置Map
     * @param workflowNodeMap 工作流节点Map
     * @return 构建的节点配置
     */
    public WorkflowDetailResponseVO.NodeConfig buildNodeConfig(MutableGraph<Long> graph,
                                                               Long parentId,
                                                               Map<Long, WorkflowDetailResponseVO.NodeConfig> nodeConfigMap,
                                                               Map<Long, WorkflowDetailResponseVO.NodeInfo> workflowNodeMap) {

        Set<Long> successors = graph.successors(parentId);
        if (CollectionUtils.isEmpty(successors)) {
            return null;
        }

        WorkflowDetailResponseVO.NodeInfo previousNodeInfo = workflowNodeMap.get(parentId);
        WorkflowDetailResponseVO.NodeConfig currentConfig = new WorkflowDetailResponseVO.NodeConfig();
        currentConfig.setConditionNodes(Lists.newArrayList());

        // 是否挂载子节点
        boolean mount = false;

        for (Long successor : successors) {
            Set<Long> predecessors = graph.predecessors(successor);
            WorkflowDetailResponseVO.NodeInfo nodeInfo = workflowNodeMap.get(successor);
            currentConfig.setNodeType(nodeInfo.getNodeType());
            currentConfig.getConditionNodes().add(nodeInfo);
            nodeConfigMap.put(successor, currentConfig);

            if (predecessors.size() >= 2) {
                // 查找predecessors的公共祖先节点
                Map<Long, Set<Long>> sets = new HashMap<>();
                for (final Long predecessor : predecessors) {
                    Set<Long> set = Sets.newHashSet();
                    sets.put(predecessor, set);
                    findCommonAncestor(predecessor, set, graph);
                }

                Set<Long> intersection = sets.values().stream().findFirst().get();
                for (final Set<Long> value : sets.values()) {
                    intersection = Sets.intersection(value, intersection);
                }

                Long commonAncestor = new ArrayList<>(intersection).get(intersection.size() - 1);
                WorkflowDetailResponseVO.NodeConfig parentNodeConfig = nodeConfigMap.get(graph.successors(commonAncestor).stream().findFirst().get());
                parentNodeConfig.setChildNode(currentConfig);
                mount = false;
            } else {
                mount = true;
            }

            buildNodeConfig(graph, successor, nodeConfigMap, workflowNodeMap);
        }

        if (!parentId.equals(SystemConstants.ROOT) && mount) {
            previousNodeInfo.setChildNode(currentConfig);
        }

        currentConfig.getConditionNodes().sort(Comparator.comparing(WorkflowDetailResponseVO.NodeInfo::getPriorityLevel));
        return currentConfig;
    }

    private void findCommonAncestor(Long predecessor, Set<Long> set, MutableGraph<Long> graph) {

        Set<Long> predecessors = graph.predecessors(predecessor);
        if (CollectionUtils.isEmpty(predecessors)) {
            return;
        }

        set.addAll(predecessors);

        findCommonAncestor(new ArrayList<>(predecessors).get(0), set, graph);
    }

    /**
     * 根据给定的父节点ID、队列、工作流组名、工作流ID、节点配置、图构建图
     *
     * @param parentIds  父节点ID列表
     * @param deque      队列
     * @param groupName  工作流组名
     * @param workflowId 工作流ID
     * @param nodeConfig 节点配置
     * @param graph      图
     * @param version    版本号
     */
    public void buildGraph(List<Long> parentIds, LinkedBlockingDeque<Long> deque,
                           String groupName, Long workflowId,
                           WorkflowRequestVO.NodeConfig nodeConfig, MutableGraph<Long> graph, Integer version) {

        if (Objects.isNull(nodeConfig)) {
            return;
        }

        // 获取节点信息
        List<WorkflowRequestVO.NodeInfo> conditionNodes = nodeConfig.getConditionNodes();
        if (!CollectionUtils.isEmpty(conditionNodes)) {
            for (final WorkflowRequestVO.NodeInfo nodeInfo : conditionNodes) {
                WorkflowNode workflowNode = WorkflowConverter.INSTANCE.toWorkflowNode(nodeInfo);
                workflowNode.setWorkflowId(workflowId);
                workflowNode.setGroupName(groupName);
                workflowNode.setNodeType(nodeConfig.getNodeType());
                workflowNode.setVersion(version);
                if (WorkflowNodeTypeEnum.DECISION.getType() == nodeConfig.getNodeType()) {
                    workflowNode.setJobId(SystemConstants.DECISION_JOB_ID);
                    DecisionConfig decision = nodeInfo.getDecision();
                    Assert.notNull(decision, () -> new EasyRetryServerException("【{}】配置信息不能为空", nodeInfo.getNodeName()));
                    Assert.notBlank(decision.getNodeExpression(), ()-> new EasyRetryServerException("【{}】表达式不能为空", nodeInfo.getNodeName()));
                    workflowNode.setNodeInfo(JsonUtil.toJsonString(decision));
                }

                if (WorkflowNodeTypeEnum.CALLBACK.getType() == nodeConfig.getNodeType()) {
                    workflowNode.setJobId(SystemConstants.CALLBACK_JOB_ID);
                    CallbackConfig callback = nodeInfo.getCallback();
                    Assert.notNull(callback, () -> new EasyRetryServerException("【{}】配置信息不能为空", nodeInfo.getNodeName()));
                    Assert.notBlank(callback.getWebhook(), () -> new EasyRetryServerException("【{}】webhook不能为空", nodeInfo.getNodeName()));
                    Assert.notNull(callback.getContentType(), () -> new EasyRetryServerException("【{}】请求类型不能为空", nodeInfo.getNodeName()));
                    Assert.notBlank(callback.getSecret(), () -> new EasyRetryServerException("【{}】秘钥不能为空", nodeInfo.getNodeName()));
                    workflowNode.setNodeInfo(JsonUtil.toJsonString(callback));
                }

                if (WorkflowNodeTypeEnum.JOB_TASK.getType() == nodeConfig.getNodeType()) {
                    JobTaskConfig jobTask = nodeInfo.getJobTask();
                    Assert.notNull(jobTask, () -> new EasyRetryServerException("【{}】配置信息不能为空", nodeInfo.getNodeName()));
                    Assert.notNull(jobTask.getJobId(), () -> new EasyRetryServerException("【{}】所属任务不能为空", nodeInfo.getNodeName()));
                    workflowNode.setJobId(jobTask.getJobId());
                }

                Assert.isTrue(1 == workflowNodeMapper.insert(workflowNode),
                        () -> new EasyRetryServerException("新增工作流节点失败"));
                // 添加节点
                graph.addNode(workflowNode.getId());
                for (final Long parentId : parentIds) {
                    // 添加边
                    graph.putEdge(parentId, workflowNode.getId());
                }
                WorkflowRequestVO.NodeConfig childNode = nodeInfo.getChildNode();
                if (Objects.nonNull(childNode) && !CollectionUtils.isEmpty(childNode.getConditionNodes())) {
                    buildGraph(Lists.newArrayList(workflowNode.getId()), deque, groupName, workflowId, childNode,
                            graph, version);
                } else {
                    if (WorkflowNodeTypeEnum.DECISION.getType() == nodeConfig.getNodeType()) {
                        throw new EasyRetryServerException("决策节点不能作为叶子节点");
                    }

                    // 叶子节点记录一下
                    deque.add(workflowNode.getId());
                }
            }
        }

        WorkflowRequestVO.NodeConfig childNode = nodeConfig.getChildNode();
        if (Objects.nonNull(childNode) && !CollectionUtils.isEmpty(childNode.getConditionNodes())) {
            //  应该是conditionNodes里面叶子节点的选择
            List<Long> list = Lists.newArrayList();
            deque.drainTo(list);
            buildGraph(list, deque, groupName, workflowId, childNode, graph, version);
        }
    }


}
