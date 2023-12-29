package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.WaitStrategy;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.strategy.WaitStrategies;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.common.util.GraphUtils;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.JobRequestVO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
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
    private final SystemProperties systemProperties;

    @Override
    @Transactional
    public boolean saveWorkflow(WorkflowRequestVO workflowRequestVO) {
        log.info("保存工作流信息：{}", JsonUtil.toJsonString(workflowRequestVO));
        MutableGraph<Long> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        // 添加虚拟头节点
        graph.addNode(SystemConstants.ROOT);

        // 组装工作流信息
        Workflow workflow = WorkflowConverter.INSTANCE.toWorkflow(workflowRequestVO);
        workflow.setNextTriggerAt(calculateNextTriggerAt(workflowRequestVO, DateUtils.toNowMilli()));
        workflow.setFlowInfo(StrUtil.EMPTY);
        workflow.setBucketIndex(HashUtil.bkdrHash(workflowRequestVO.getGroupName() + workflowRequestVO.getWorkflowName())
            % systemProperties.getBucketTotal());
        workflow.setNamespaceId(UserSessionUtils.currentUserSession().getNamespaceId());
        Assert.isTrue(1 == workflowMapper.insert(workflow), () -> new EasyRetryServerException("新增工作流失败"));

        // 获取DAG节点配置
        NodeConfig nodeConfig = workflowRequestVO.getNodeConfig();

        // 递归构建图
        buildGraph(Lists.newArrayList(SystemConstants.ROOT), new LinkedBlockingDeque<>(),
            workflowRequestVO.getGroupName(), workflow.getId(), nodeConfig, graph);

        log.info("图构建完成. graph:[{}]", graph);
        // 保存图信息
        workflow.setFlowInfo(JsonUtil.toJsonString(GraphUtils.serializeGraphToJson(graph)));
        workflowMapper.updateById(workflow);

        return true;
    }

    private static Long calculateNextTriggerAt(final WorkflowRequestVO workflowRequestVO, Long time) {
        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(workflowRequestVO.getTriggerType());
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(workflowRequestVO.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(time);
        return waitStrategy.computeTriggerTime(waitStrategyContext);
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
            .eq(WorkflowNode::getWorkflowId, id)
            .orderByAsc(WorkflowNode::getPriorityLevel));

        List<WorkflowDetailResponseVO.NodeInfo> nodeInfos = WorkflowConverter.INSTANCE.toNodeInfo(workflowNodes);

        Map<Long, WorkflowDetailResponseVO.NodeInfo> workflowNodeMap = nodeInfos.stream()
            .collect(Collectors.toMap(WorkflowDetailResponseVO.NodeInfo::getId, i -> i));

        String flowInfo = workflow.getFlowInfo();
        try {
            MutableGraph<Long> graph = GraphUtils.deserializeJsonToGraph(flowInfo);
            // 反序列化构建图
            WorkflowDetailResponseVO.NodeConfig config = buildNodeConfig(graph, SystemConstants.ROOT, new HashMap<>(),
                workflowNodeMap);
            responseVO.setNodeConfig(config);
        } catch (Exception e) {
            log.error("反序列化失败. json:[{}]", flowInfo, e);
            throw new EasyRetryServerException("查询工作流详情失败");
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
        queryWrapper.orderByDesc(Workflow::getId);
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
        graph.addNode(SystemConstants.ROOT);

        // 获取DAG节点配置
        NodeConfig nodeConfig = workflowRequestVO.getNodeConfig();

        // 递归构建图
        buildGraph(Lists.newArrayList(SystemConstants.ROOT), new LinkedBlockingDeque<>(),
            workflowRequestVO.getGroupName(), workflowRequestVO.getId(), nodeConfig, graph);

        log.info("图构建完成. graph:[{}]", graph);

        // 保存图信息
        Workflow workflow = new Workflow();
        workflow.setId(workflowRequestVO.getId());
        workflow.setFlowInfo(JsonUtil.toJsonString(GraphUtils.serializeGraphToJson(graph)));
        Assert.isTrue(workflowMapper.updateById(workflow) > 0, () -> new EasyRetryServerException("更新失败"));

        return Boolean.TRUE;
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

    public void buildGraph(List<Long> parentIds, LinkedBlockingDeque<Long> deque, String groupName, Long workflowId,
        NodeConfig nodeConfig, MutableGraph<Long> graph) {

        if (Objects.isNull(nodeConfig)) {
            return;
        }

        // 获取节点信息
        List<NodeInfo> conditionNodes = nodeConfig.getConditionNodes();
        if (!CollectionUtils.isEmpty(conditionNodes)) {
            for (final NodeInfo nodeInfo : conditionNodes) {
                WorkflowNode workflowNode = WorkflowConverter.INSTANCE.toWorkflowNode(nodeInfo);
                workflowNode.setWorkflowId(workflowId);
                workflowNode.setGroupName(groupName);
                workflowNode.setNodeType(nodeConfig.getNodeType());
                if (WorkflowNodeTypeEnum.CONDITION.getType() == nodeConfig.getNodeType()) {
                    workflowNode.setJobId(SystemConstants.CONDITION_JOB_ID);
                }

                Assert.isTrue(1 == workflowNodeMapper.insert(workflowNode),
                    () -> new EasyRetryServerException("新增工作流节点失败"));
                // 添加节点
                graph.addNode(workflowNode.getId());
                for (final Long parentId : parentIds) {
                    // 添加边
                    graph.putEdge(parentId, workflowNode.getId());
                }
                log.warn("workflowNodeId:[{}] parentIds:[{}]",
                    workflowNode.getId(), JsonUtil.toJsonString(parentIds));
                NodeConfig childNode = nodeInfo.getChildNode();
                if (Objects.nonNull(childNode) && !CollectionUtils.isEmpty(childNode.getConditionNodes())) {
                    buildGraph(Lists.newArrayList(workflowNode.getId()), deque, groupName, workflowId, childNode,
                        graph);
                } else {
                    // 叶子节点记录一下
                    deque.add(workflowNode.getId());
                }
            }
        }

        NodeConfig childNode = nodeConfig.getChildNode();
        if (Objects.nonNull(childNode) && !CollectionUtils.isEmpty(childNode.getConditionNodes())) {
            //  应该是conditionNodes里面叶子节点的选择
            List<Long> list = Lists.newArrayList();
            deque.drainTo(list);
            buildGraph(list, deque, groupName, workflowId, childNode, graph);
        }
    }


}
