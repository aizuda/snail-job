package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.WaitStrategy;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.strategy.WaitStrategies;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.common.util.GraphUtils;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.UserSessionVO;
import com.aizuda.easy.retry.server.web.model.request.WorkflowQueryVO;
import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO;
import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO.NodeConfig;
import com.aizuda.easy.retry.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowResponseVO;
import com.aizuda.easy.retry.server.web.service.WorkflowService;
import com.aizuda.easy.retry.server.web.service.convert.WorkflowConverter;
import com.aizuda.easy.retry.server.web.service.handler.WorkflowHandler;
import com.aizuda.easy.retry.server.web.util.UserSessionUtils;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Workflow;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final WorkflowHandler workflowHandler;

    @Override
    @Transactional
    public boolean saveWorkflow(WorkflowRequestVO workflowRequestVO) {
        log.info("保存工作流信息：{}", JsonUtil.toJsonString(workflowRequestVO));
        MutableGraph<Long> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        // 添加虚拟头节点
        graph.addNode(SystemConstants.ROOT);

        // 组装工作流信息
        Workflow workflow = WorkflowConverter.INSTANCE.toWorkflow(workflowRequestVO);
        workflow.setVersion(1);
        workflow.setNextTriggerAt(calculateNextTriggerAt(workflowRequestVO, DateUtils.toNowMilli()));
        workflow.setFlowInfo(StrUtil.EMPTY);
        workflow.setBucketIndex(HashUtil.bkdrHash(workflowRequestVO.getGroupName() + workflowRequestVO.getWorkflowName())
                % systemProperties.getBucketTotal());
        workflow.setNamespaceId(UserSessionUtils.currentUserSession().getNamespaceId());
        Assert.isTrue(1 == workflowMapper.insert(workflow), () -> new EasyRetryServerException("新增工作流失败"));

        // 获取DAG节点配置
        NodeConfig nodeConfig = workflowRequestVO.getNodeConfig();

        // 递归构建图
        workflowHandler.buildGraph(Lists.newArrayList(SystemConstants.ROOT),
                new LinkedBlockingDeque<>(),
                workflowRequestVO.getGroupName(),
                workflow.getId(), nodeConfig, graph,
                workflow.getVersion());

        log.info("图构建完成. graph:[{}]", graph);
        // 保存图信息
        workflow.setFlowInfo(JsonUtil.toJsonString(GraphUtils.serializeGraphToJson(graph)));
        Assert.isTrue(1 == workflowMapper.updateById(workflow), () -> new EasyRetryServerException("保存工作流图失败"));
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
    public WorkflowDetailResponseVO getWorkflowDetail(Long id) {

        Workflow workflow = workflowMapper.selectById(id);
        if (Objects.isNull(workflow)) {
            return null;
        }

        WorkflowDetailResponseVO responseVO = WorkflowConverter.INSTANCE.toWorkflowDetailResponseVO(workflow);
        List<WorkflowNode> workflowNodes = workflowNodeMapper.selectList(new LambdaQueryWrapper<WorkflowNode>()
                .eq(WorkflowNode::getDeleted, 0)
                .eq(WorkflowNode::getVersion, workflow.getVersion())
                .eq(WorkflowNode::getWorkflowId, id)
                .orderByAsc(WorkflowNode::getPriorityLevel));

        List<WorkflowDetailResponseVO.NodeInfo> nodeInfos = WorkflowConverter.INSTANCE.toNodeInfo(workflowNodes);

        Map<Long, WorkflowDetailResponseVO.NodeInfo> workflowNodeMap = nodeInfos.stream()
                .collect(Collectors.toMap(WorkflowDetailResponseVO.NodeInfo::getId, i -> i));

        String flowInfo = workflow.getFlowInfo();
        try {
            MutableGraph<Long> graph = GraphUtils.deserializeJsonToGraph(flowInfo);
            // 反序列化构建图
            WorkflowDetailResponseVO.NodeConfig config = workflowHandler.buildNodeConfig(graph, SystemConstants.ROOT, new HashMap<>(),
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
    @Transactional
    public Boolean updateWorkflow(WorkflowRequestVO workflowRequestVO) {

        Assert.notNull(workflowRequestVO.getId(), () -> new EasyRetryServerException("工作流ID不能为空"));

        Workflow workflow = workflowMapper.selectById(workflowRequestVO.getId());
        Assert.notNull(workflow, () -> new EasyRetryServerException("工作流不存在"));

        MutableGraph<Long> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        // 添加虚拟头节点
        graph.addNode(SystemConstants.ROOT);

        // 获取DAG节点配置
        NodeConfig nodeConfig = workflowRequestVO.getNodeConfig();

        // 递归构建图
        workflowHandler.buildGraph(Lists.newArrayList(SystemConstants.ROOT), new LinkedBlockingDeque<>(),
                workflowRequestVO.getGroupName(), workflowRequestVO.getId(), nodeConfig, graph, workflow.getVersion() + 1);

        log.info("图构建完成. graph:[{}]", graph);

        // 保存图信息
        workflow = new Workflow();
        workflow.setId(workflowRequestVO.getId());
        workflow.setVersion(workflow.getVersion() + 1);
        workflow.setFlowInfo(JsonUtil.toJsonString(GraphUtils.serializeGraphToJson(graph)));
        Assert.isTrue(workflowMapper.update(workflow, new LambdaQueryWrapper<Workflow>()
            .eq(Workflow::getId, workflow.getId())
            .eq(Workflow::getVersion, workflow.getVersion())
        ) > 0, () -> new EasyRetryServerException("更新失败"));

        return Boolean.TRUE;
    }

    @Override
    public Boolean updateStatus(Long id) {
        Workflow workflow = workflowMapper.selectOne(
                new LambdaQueryWrapper<Workflow>()
                        .select(Workflow::getId, Workflow::getWorkflowStatus)
                        .eq(Workflow::getId, id));
        Assert.notNull(workflow, () -> new EasyRetryServerException("工作流不存在"));

        if (Objects.equals(workflow.getWorkflowStatus(), StatusEnum.NO.getStatus())) {
            workflow.setWorkflowStatus(StatusEnum.YES.getStatus());
        } else {
            workflow.setWorkflowStatus(StatusEnum.NO.getStatus());
        }

        return 1 == workflowMapper.updateById(workflow);
    }

    @Override
    public Boolean deleteById(Long id) {
        Workflow workflow = new Workflow();
        workflow.setId(id);
        workflow.setDeleted(StatusEnum.YES.getStatus());
        return 1 == workflowMapper.updateById(workflow);
    }

}
