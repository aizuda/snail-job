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
import com.aizuda.easy.retry.server.web.model.response.JobResponseVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowResponseVO;
import com.aizuda.easy.retry.server.web.service.WorkflowService;
import com.aizuda.easy.retry.server.web.service.convert.JobResponseVOConverter;
import com.aizuda.easy.retry.server.web.service.convert.WorkflowConverter;
import com.aizuda.easy.retry.server.web.util.UserSessionUtils;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.Workflow;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
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
            // 反序列化构建图
            WorkflowDetailResponseVO.NodeConfig config = deserializeJsonToGraph(flowInfo, workflowNodeMap);
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

    // 从JSON反序列化为Guava图
    private static  WorkflowDetailResponseVO.NodeConfig deserializeJsonToGraph(String jsonGraph,
         Map<Long, WorkflowDetailResponseVO.NodeInfo> workflowNodeMap) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // 将JSON字符串转换为Map<String, Iterable<String>>
        Map<Long, Iterable<Long>> adjacencyList = objectMapper.readValue(
            jsonGraph, new TypeReference<Map<Long, Iterable<Long>>>() {});

        Map<Long, WorkflowDetailResponseVO.NodeConfig> configMap = new HashMap<>();
        WorkflowDetailResponseVO.NodeConfig rootConfig = new WorkflowDetailResponseVO.NodeConfig();

        // 创建Guava图并添加节点和边
        for (Map.Entry<Long, Iterable<Long>> entry : adjacencyList.entrySet()) {
            Long node = entry.getKey();
            Iterable<Long> successors = entry.getValue();
            WorkflowDetailResponseVO.NodeConfig previousConfig = configMap.getOrDefault(node, new WorkflowDetailResponseVO.NodeConfig());

            WorkflowDetailResponseVO.NodeConfig currentConfig = new WorkflowDetailResponseVO.NodeConfig();
            for (Long successor : successors) {
                WorkflowDetailResponseVO.NodeInfo nodeInfo = workflowNodeMap.get(successor);
                // 第一层节点
                if (node == root) {
                    rootConfig.setNodeType(nodeInfo.getNodeType());
                    List<WorkflowDetailResponseVO.NodeInfo> nodeInfos = Optional.ofNullable(
                        rootConfig.getConditionNodes()).orElse(Lists.newArrayList());
                    nodeInfos.add(nodeInfo);
                    rootConfig.setConditionNodes(nodeInfos);
                    configMap.put(nodeInfo.getId(), rootConfig);
                } else {
                    currentConfig.setNodeType(nodeInfo.getNodeType());
                    List<WorkflowDetailResponseVO.NodeInfo> nodeInfos = Optional.ofNullable(
                        currentConfig.getConditionNodes()).orElse(Lists.newArrayList());
                    nodeInfos.add(nodeInfo);
                    currentConfig.setConditionNodes(nodeInfos);
                    configMap.put(nodeInfo.getId(), currentConfig);
                    previousConfig.setChildNode(currentConfig);
                }
            }
        }

        return rootConfig;
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
