package com.aizuda.snailjob.server.common.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.model.request.CallbackConfig;
import com.aizuda.snailjob.model.request.DecisionConfig;
import com.aizuda.snailjob.server.common.convert.WorkflowConverter;
import com.aizuda.snailjob.model.request.JobTaskConfig;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.vo.request.WorkflowRequestVO;
import com.aizuda.snailjob.server.common.vo.WorkflowDetailResponseVO;
import com.aizuda.snailjob.server.common.vo.WorkflowDetailResponseVO.NodeConfig;
import com.aizuda.snailjob.server.common.vo.WorkflowDetailResponseVO.NodeInfo;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.graph.MutableGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * @author xiaowoniu
 * @date 2023-12-30 23:26:43
 * @since 2.6.0
 */
@Component("webWorkflowHandlerOpenApi")
@Slf4j
@RequiredArgsConstructor
@Deprecated
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
    public NodeConfig buildNodeConfig(MutableGraph<Long> graph,
                                      Long parentId,
                                      Map<Long, NodeConfig> nodeConfigMap,
                                      Map<Long, NodeInfo> workflowNodeMap) {

        Set<Long> successors = graph.successors(parentId);
        if (CollUtil.isEmpty(successors)) {
            return null;
        }

        NodeInfo previousNodeInfo = workflowNodeMap.get(parentId);
        NodeConfig currentConfig = new NodeConfig();
        currentConfig.setConditionNodes(Lists.newArrayList());

        // 是否挂载子节点
        boolean mount = false;

        for (Long successor : Sets.newTreeSet(successors)) {
            Set<Long> predecessors = graph.predecessors(successor);
            NodeInfo nodeInfo = workflowNodeMap.get(successor);
            currentConfig.setNodeType(nodeInfo.getNodeType());
            currentConfig.getConditionNodes().add(nodeInfo);
            nodeConfigMap.put(successor, currentConfig);

            if (predecessors.size() >= 2) {
                // 查找predecessors的公共祖先节点
                Map<Long, Set<Long>> sets = new HashMap<>();
                for (final Long predecessor : predecessors) {
                    Set<Long> set = Sets.newTreeSet();
                    sets.put(predecessor, set);
                    findCommonAncestor(predecessor, set, graph);
                }

                Set<Long> intersection = sets.values().stream().findFirst().get();
                for (final Set<Long> value : sets.values()) {
                    intersection = Sets.intersection(value, intersection);
                }

                Long commonAncestor = intersection.stream().toList().get(intersection.size() - 1);
                NodeConfig parentNodeConfig = nodeConfigMap.get(
                        Sets.newTreeSet(graph.successors(commonAncestor)).stream().findFirst().get());
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
        if (CollUtil.isEmpty(predecessors)) {
            return;
        }

        set.addAll(predecessors);

        findCommonAncestor(new ArrayList<>(predecessors).get(0), set, graph);
    }

    /**
     * 根据给定的父节点ID、队列、工作流组名、工作流ID、节点配置、图构建图
     *
     * @param parentIds   父节点ID列表
     * @param deque       队列
     * @param groupName   工作流组名
     * @param workflowId  工作流ID
     * @param nodeConfig  节点配置
     * @param graph       图
     * @param version     版本号
     * @param namespaceId
     */
    public void buildGraph(List<Long> parentIds, LinkedBlockingDeque<Long> deque, String groupName, Long workflowId,
                           WorkflowRequestVO.NodeConfig nodeConfig, MutableGraph<Long> graph, Integer version, String namespaceId) {

        if (Objects.isNull(nodeConfig)) {
            return;
        }

        LinkedBlockingDeque<Long> tempDeque = null;
        // 获取节点信息
        List<WorkflowRequestVO.NodeInfo> conditionNodes = nodeConfig.getConditionNodes();
        if (CollUtil.isNotEmpty(conditionNodes)) {
            // 一定存在汇合的子节点
            if (Objects.nonNull(nodeConfig.getChildNode())) {
                tempDeque = new LinkedBlockingDeque<>();
            }
            conditionNodes = conditionNodes.stream()
                    .sorted(Comparator.comparing(WorkflowRequestVO.NodeInfo::getPriorityLevel))
                    .collect(Collectors.toList());
            for (final WorkflowRequestVO.NodeInfo nodeInfo : conditionNodes) {
                WorkflowNode workflowNode = WorkflowConverter.INSTANCE.convert(nodeInfo);
                workflowNode.setWorkflowId(workflowId);
                workflowNode.setGroupName(groupName);
                workflowNode.setNodeType(nodeConfig.getNodeType());
                workflowNode.setVersion(version);
                workflowNode.setNamespaceId(namespaceId);
                if (WorkflowNodeTypeEnum.DECISION.getType() == nodeConfig.getNodeType()) {
                    workflowNode.setJobId(SystemConstants.DECISION_JOB_ID);
                    DecisionConfig decision = nodeInfo.getDecision();
                    Assert.notNull(decision, () -> new SnailJobServerException("Configuration information for [{}] cannot be empty", nodeInfo.getNodeName()));
                    Assert.notBlank(decision.getNodeExpression(), () -> new SnailJobServerException("Expression for [{}] cannot be empty", nodeInfo.getNodeName()));
                    Assert.notNull(decision.getDefaultDecision(), () -> new SnailJobServerException("Default decision for [{}] cannot be empty", nodeInfo.getNodeName()));
                    Assert.notNull(decision.getExpressionType(), () -> new SnailJobServerException("Expression type for [{}] cannot be empty", nodeInfo.getNodeName()));
                    workflowNode.setNodeInfo(JsonUtil.toJsonString(decision));
                }

                if (WorkflowNodeTypeEnum.CALLBACK.getType() == nodeConfig.getNodeType()) {
                    workflowNode.setJobId(SystemConstants.CALLBACK_JOB_ID);
                    CallbackConfig callback = nodeInfo.getCallback();
                    Assert.notNull(callback, () -> new SnailJobServerException("Configuration information for [{}] cannot be empty", nodeInfo.getNodeName()));
                    Assert.notBlank(callback.getWebhook(), () -> new SnailJobServerException("Webhook for [{}] cannot be empty", nodeInfo.getNodeName()));
                    Assert.notNull(callback.getContentType(), () -> new SnailJobServerException("Request type for [{}] cannot be empty", nodeInfo.getNodeName()));
                    Assert.notBlank(callback.getSecret(), () -> new SnailJobServerException("Secret key for [{}] cannot be empty", nodeInfo.getNodeName()));
                    workflowNode.setNodeInfo(JsonUtil.toJsonString(callback));
                }

                if (WorkflowNodeTypeEnum.JOB_TASK.getType() == nodeConfig.getNodeType()) {
                    JobTaskConfig jobTask = nodeInfo.getJobTask();
                    Assert.notNull(jobTask, () -> new SnailJobServerException("Configuration information for [{}] cannot be empty", nodeInfo.getNodeName()));
                    Assert.notNull(jobTask.getJobId(), () -> new SnailJobServerException("Associated task for [{}] cannot be empty", nodeInfo.getNodeName()));
                    workflowNode.setJobId(jobTask.getJobId());
                }

                Assert.isTrue(1 == workflowNodeMapper.insert(workflowNode),
                        () -> new SnailJobServerException("Adding new workflow node failed"));
                // 添加节点
                graph.addNode(workflowNode.getId());
                for (final Long parentId : parentIds) {
                    // 添加边
                    graph.putEdge(parentId, workflowNode.getId());
                }
                WorkflowRequestVO.NodeConfig childNode = nodeInfo.getChildNode();
                if (Objects.nonNull(childNode) && CollUtil.isNotEmpty(childNode.getConditionNodes())) {
                    buildGraph(Lists.newArrayList(workflowNode.getId()),
                            Objects.isNull(tempDeque) ? deque : tempDeque,
                            groupName, workflowId, childNode, graph, version, namespaceId);
                } else {
                    if (WorkflowNodeTypeEnum.DECISION.getType() == nodeConfig.getNodeType()) {
                        throw new SnailJobServerException("Decision nodes or successor nodes of decision nodes cannot be leaf nodes");
                    }

                    // 若当前节点无子任何子节点记录一下, 后续存在公共子节点时需要用到
                    if (Objects.nonNull(tempDeque)) {
                        tempDeque.add(workflowNode.getId());
                    } else {
                        // 当前节点无汇合的子节点放到公共的队列
                        deque.add(workflowNode.getId());
                    }

                }
            }
        }

        WorkflowRequestVO.NodeConfig childNode = nodeConfig.getChildNode();
        // 如果存在公共子节点则在这里处理
        if (Objects.nonNull(childNode) && CollUtil.isNotEmpty(childNode.getConditionNodes())) {
            //  是conditionNodes里面叶子节点的
            List<Long> list = Lists.newArrayList();
            if (Objects.nonNull(tempDeque)) {
                tempDeque.drainTo(list);
            }

            buildGraph(list, deque, groupName, workflowId, childNode, graph, version, namespaceId);
        }
    }


}
