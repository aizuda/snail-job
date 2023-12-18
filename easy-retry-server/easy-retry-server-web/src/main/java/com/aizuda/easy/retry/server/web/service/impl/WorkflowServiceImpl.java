package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.UserSessionVO;
import com.aizuda.easy.retry.server.web.model.request.WorkflowQueryVO;
import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO;
import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO.NodeConfig;
import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO.NodeInfo;
import com.aizuda.easy.retry.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowResponseVO;
import com.aizuda.easy.retry.server.web.service.WorkflowService;
import com.aizuda.easy.retry.server.web.service.convert.WorkflowConverter;
import com.aizuda.easy.retry.server.web.util.UserSessionUtils;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Workflow;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaowoniu
 * @date 2023-12-12 21:54:05
 * @since 2.6.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {
    private final WorkflowMapper workflowMapper;
    private final WorkflowNodeMapper workflowNodeMapper;
    private final static long root = -1;

    @Override
    @Transactional
    public boolean saveWorkflow(WorkflowRequestVO workflowRequestVO) {

        MutableGraph<Long> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        // 添加虚拟头节点
        graph.addNode(root);

        // 组装工作流信息
        Workflow workflow = WorkflowConverter.INSTANCE.toWorkflow(workflowRequestVO);
        // TODO 临时设置值
        workflow.setNextTriggerAt(1L);
        workflow.setFlowInfo(StrUtil.EMPTY);
        Assert.isTrue(1 == workflowMapper.insert(workflow), () -> new EasyRetryServerException("新增工作流失败"));

        // 获取DAG节点配置
        NodeConfig nodeConfig = workflowRequestVO.getNodeConfig();

        // 递归构建图
        buildGraph(Lists.newArrayList(root), workflowRequestVO.getGroupName(), workflow.getId(), nodeConfig, graph);

        log.info("图构建完成. graph:[{}]", graph);
        // 保存图信息
        workflow.setFlowInfo(JsonUtil.toJsonString(convertGraphToAdjacencyList(graph)));
        workflowMapper.updateById(workflow);

        return true;
    }

    @Override
    public WorkflowDetailResponseVO getWorkflowDetail(Long id) throws IOException {

        Workflow workflow = workflowMapper.selectById(id);
        if (Objects.isNull(workflow)) {
            return null;
        }

        WorkflowDetailResponseVO responseVO = WorkflowConverter.INSTANCE.toWorkflowDetailResponseVO(workflow);
        List<WorkflowNode> workflowNodes = workflowNodeMapper.selectList(new LambdaQueryWrapper<WorkflowNode>()
                .eq(WorkflowNode::getDeleted, 0)
                .eq(WorkflowNode::getWorkflowId, id));

        List<WorkflowDetailResponseVO.NodeInfo> nodeInfos = WorkflowConverter.INSTANCE.toNodeInfo(workflowNodes);

        Map<Long, WorkflowDetailResponseVO.NodeInfo> workflowNodeMap = nodeInfos.stream().collect(Collectors.toMap(WorkflowDetailResponseVO.NodeInfo::getId, i -> i));

        String flowInfo = workflow.getFlowInfo();
        try {
            MutableGraph<Long> graph = deserializeJsonToGraph(flowInfo);
            // 反序列化构建图
            WorkflowDetailResponseVO.NodeConfig config = buildNodeConfig(graph, root, new HashMap<>(), workflowNodeMap);
            responseVO.setNodeConfig(config);
        } catch (Exception e) {
            log.error("反序列化失败. json:[{}]", flowInfo, e);
            throw e;
        }

        return responseVO;
    }

    @Override
    public PageResult<List<WorkflowResponseVO>> listPage(final WorkflowQueryVO queryVO) {
        PageDTO<Workflow> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        LambdaQueryWrapper<Workflow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Workflow::getDeleted, StatusEnum.NO.getStatus());
        queryWrapper.eq(Workflow::getNamespaceId, userSessionVO.getNamespaceId());

        PageDTO<Workflow> page = workflowMapper.selectPage(pageDTO, queryWrapper);

        List<WorkflowResponseVO> jobResponseList = WorkflowConverter.INSTANCE.toWorkflowResponseVO(page.getRecords());

        return new PageResult<>(pageDTO, jobResponseList);
    }

    @Override
    public Boolean updateWorkflow(WorkflowRequestVO workflowRequestVO) {

        Assert.notNull(workflowRequestVO.getId(), () -> new EasyRetryServerException("工作流ID不能为空"));

        Assert.isTrue(workflowMapper.selectCount(new LambdaQueryWrapper<Workflow>()
                        .eq(Workflow::getId, workflowRequestVO.getId())) > 0,
                () -> new EasyRetryServerException("工作流不存在"));

        MutableGraph<Long> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        // 添加虚拟头节点
        graph.addNode(root);

        // 获取DAG节点配置
        NodeConfig nodeConfig = workflowRequestVO.getNodeConfig();

        // 递归构建图
        buildGraph(Lists.newArrayList(root), workflowRequestVO.getGroupName(), workflowRequestVO.getId(), nodeConfig, graph);

        log.info("图构建完成. graph:[{}]", graph);

        // 保存图信息
        Workflow workflow = new Workflow();
        workflow.setId(workflowRequestVO.getId());
        workflow.setFlowInfo(JsonUtil.toJsonString(convertGraphToAdjacencyList(graph)));
        Assert.isTrue(workflowMapper.updateById(workflow) > 0, () -> new EasyRetryServerException("更新失败"));

        return Boolean.TRUE;
    }

    // 从JSON反序列化为Guava图
    private static MutableGraph<Long> deserializeJsonToGraph(String jsonGraph) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // 将JSON字符串转换为Map<Long, Iterable<Long>>
        Map<Long, Iterable<Long>> adjacencyList = objectMapper.readValue(
                jsonGraph, new TypeReference<Map<Long, Iterable<Long>>>() {
                });

        // 创建Guava图并添加节点和边
        MutableGraph<Long> graph = GraphBuilder.directed().build();
        for (Map.Entry<Long, Iterable<Long>> entry : adjacencyList.entrySet()) {
            Long node = entry.getKey();
            Iterable<Long> successors = entry.getValue();

            graph.addNode(node);
            for (Long successor : successors) {
                graph.putEdge(node, successor);
            }
        }

        return graph;
    }

    private WorkflowDetailResponseVO.NodeConfig buildNodeConfig(MutableGraph<Long> graph,
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

        boolean mount = false;

        for (Long successor : successors) {
            Set<Long> predecessors = graph.predecessors(successor);
            WorkflowDetailResponseVO.NodeInfo nodeInfo = workflowNodeMap.get(successor);
            currentConfig.setNodeType(nodeInfo.getNodeType());
            currentConfig.getConditionNodes().add(nodeInfo);
            nodeConfigMap.put(successor, currentConfig);

            if (predecessors.size() >= 2) {
                WorkflowDetailResponseVO.NodeConfig parentNodeConfig = nodeConfigMap.get(new ArrayList<>(predecessors).get(0));
                parentNodeConfig.setChildNode(currentConfig);
                mount = false;
            } else {
                mount = true;
            }

            buildNodeConfig(graph, successor, nodeConfigMap, workflowNodeMap);
        }

        if (parentId != root && mount) {
            previousNodeInfo.setChildNode(currentConfig);
        }

        return currentConfig;
    }

    private Map<Long, Iterable<Long>> convertGraphToAdjacencyList(MutableGraph<Long> graph) {
        Map<Long, Iterable<Long>> adjacencyList = new HashMap<>();

        for (Long node : graph.nodes()) {
            adjacencyList.put(node, graph.successors(node));
        }

        return adjacencyList;
    }

    public void buildGraph(List<Long> parentIds, String groupName, Long workflowId, NodeConfig nodeConfig, MutableGraph<Long> graph) {

        if (Objects.isNull(nodeConfig)) {
            return;
        }

        // 获取节点信息
        List<NodeInfo> conditionNodes = nodeConfig.getConditionNodes();
        List<Long> parentIds1 = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(conditionNodes)) {
            for (final NodeInfo nodeInfo : conditionNodes) {
                WorkflowNode workflowNode = WorkflowConverter.INSTANCE.toWorkflowNode(nodeInfo);
                workflowNode.setWorkflowId(workflowId);
                workflowNode.setGroupName(groupName);
                workflowNode.setNodeType(nodeConfig.getNodeType());
                Assert.isTrue(1 == workflowNodeMapper.insert(workflowNode), () -> new EasyRetryServerException("新增工作流节点失败"));
                // 添加节点
                graph.addNode(workflowNode.getId());
                for (final Long parentId : parentIds) {
                    // 添加边
                    graph.putEdge(parentId, workflowNode.getId());
                }
                parentIds1.add(workflowNode.getId());
                buildGraph(Lists.newArrayList(workflowNode.getId()), groupName, workflowId, nodeInfo.getChildNode(), graph);
            }
        }

        buildGraph(parentIds1, groupName, workflowId, nodeConfig.getChildNode(), graph);
    }


}
