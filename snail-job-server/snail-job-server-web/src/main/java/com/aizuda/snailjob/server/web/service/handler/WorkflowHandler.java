package com.aizuda.snailjob.server.web.service.handler;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.dto.CallbackConfig;
import com.aizuda.snailjob.server.common.dto.DecisionConfig;
import com.aizuda.snailjob.server.common.dto.JobTaskConfig;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.web.model.request.WorkflowRequestVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowDetailResponseVO.NodeConfig;
import com.aizuda.snailjob.server.web.model.response.WorkflowDetailResponseVO.NodeInfo;
import com.aizuda.snailjob.server.web.service.convert.WorkflowConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.graph.MutableGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

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
    public NodeConfig buildNodeConfig(MutableGraph<Long> graph,
                                      Long parentId,
                                      Map<Long, NodeConfig> nodeConfigMap,
                                      Map<Long, NodeInfo> workflowNodeMap) {

        Set<Long> successors = graph.successors(parentId);
        if (CollectionUtils.isEmpty(successors)) {
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
            conditionNodes = conditionNodes.stream()
                .sorted(Comparator.comparing(WorkflowRequestVO.NodeInfo::getPriorityLevel))
                .collect(Collectors.toList());
            for (final WorkflowRequestVO.NodeInfo nodeInfo : conditionNodes) {
                WorkflowNode workflowNode = WorkflowConverter.INSTANCE.convert(nodeInfo);
                workflowNode.setWorkflowId(workflowId);
                workflowNode.setGroupName(groupName);
                workflowNode.setNodeType(nodeConfig.getNodeType());
                workflowNode.setVersion(version);
                if (WorkflowNodeTypeEnum.DECISION.getType() == nodeConfig.getNodeType()) {
                    workflowNode.setJobId(SystemConstants.DECISION_JOB_ID);
                    DecisionConfig decision = nodeInfo.getDecision();
                    Assert.notNull(decision, () -> new SnailJobServerException("【{}】配置信息不能为空", nodeInfo.getNodeName()));
                    Assert.notBlank(decision.getNodeExpression(), () -> new SnailJobServerException("【{}】表达式不能为空", nodeInfo.getNodeName()));
                    Assert.notNull(decision.getDefaultDecision(), () -> new SnailJobServerException("【{}】默认决策不能为空", nodeInfo.getNodeName()));
                    Assert.notNull(decision.getExpressionType(), () -> new SnailJobServerException("【{}】表达式类型不能为空", nodeInfo.getNodeName()));
                    workflowNode.setNodeInfo(JsonUtil.toJsonString(decision));
                }

                if (WorkflowNodeTypeEnum.CALLBACK.getType() == nodeConfig.getNodeType()) {
                    workflowNode.setJobId(SystemConstants.CALLBACK_JOB_ID);
                    CallbackConfig callback = nodeInfo.getCallback();
                    Assert.notNull(callback, () -> new SnailJobServerException("【{}】配置信息不能为空", nodeInfo.getNodeName()));
                    Assert.notBlank(callback.getWebhook(), () -> new SnailJobServerException("【{}】webhook不能为空", nodeInfo.getNodeName()));
                    Assert.notNull(callback.getContentType(), () -> new SnailJobServerException("【{}】请求类型不能为空", nodeInfo.getNodeName()));
                    Assert.notBlank(callback.getSecret(), () -> new SnailJobServerException("【{}】秘钥不能为空", nodeInfo.getNodeName()));
                    workflowNode.setNodeInfo(JsonUtil.toJsonString(callback));
                }

                if (WorkflowNodeTypeEnum.JOB_TASK.getType() == nodeConfig.getNodeType()) {
                    JobTaskConfig jobTask = nodeInfo.getJobTask();
                    Assert.notNull(jobTask, () -> new SnailJobServerException("【{}】配置信息不能为空", nodeInfo.getNodeName()));
                    Assert.notNull(jobTask.getJobId(), () -> new SnailJobServerException("【{}】所属任务不能为空", nodeInfo.getNodeName()));
                    workflowNode.setJobId(jobTask.getJobId());
                }

                Assert.isTrue(1 == workflowNodeMapper.insert(workflowNode),
                    () -> new SnailJobServerException("新增工作流节点失败"));
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
                        throw new SnailJobServerException("决策节点不能作为叶子节点");
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
