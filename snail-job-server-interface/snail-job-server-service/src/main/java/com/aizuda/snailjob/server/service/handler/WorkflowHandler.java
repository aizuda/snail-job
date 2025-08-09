package com.aizuda.snailjob.server.service.handler;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.model.response.base.WorkflowDetailResponse;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.graph.MutableGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author xiaowoniu
 * @date 2023-12-30 23:26:43
 * @since 2.6.0
 */
@Component
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
    public WorkflowDetailResponse.NodeConfig buildNodeConfig(MutableGraph<Long> graph,
                                                             Long parentId,
                                                             Map<Long, WorkflowDetailResponse.NodeConfig> nodeConfigMap,
                                                             Map<Long, WorkflowDetailResponse.NodeInfo> workflowNodeMap) {

        Set<Long> successors = graph.successors(parentId);
        if (CollUtil.isEmpty(successors)) {
            return null;
        }

        WorkflowDetailResponse.NodeInfo previousNodeInfo = workflowNodeMap.get(parentId);
        WorkflowDetailResponse.NodeConfig currentConfig = new WorkflowDetailResponse.NodeConfig();
        currentConfig.setConditionNodes(Lists.newArrayList());

        // 是否挂载子节点
        boolean mount = false;

        for (Long successor : Sets.newTreeSet(successors)) {
            Set<Long> predecessors = graph.predecessors(successor);
            WorkflowDetailResponse.NodeInfo nodeInfo = workflowNodeMap.get(successor);
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
                WorkflowDetailResponse.NodeConfig parentNodeConfig = nodeConfigMap.get(
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

        currentConfig.getConditionNodes().sort(Comparator.comparing(WorkflowDetailResponse.NodeInfo::getPriorityLevel));
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

}
