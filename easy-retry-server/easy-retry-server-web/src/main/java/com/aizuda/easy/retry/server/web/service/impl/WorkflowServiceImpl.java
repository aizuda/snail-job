package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.expression.ExpressionEngine;
import com.aizuda.easy.retry.common.core.expression.ExpressionFactory;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.WaitStrategy;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.dto.DecisionConfig;
import com.aizuda.easy.retry.server.common.dto.JobTaskConfig;
import com.aizuda.easy.retry.server.common.enums.ExpressionTypeEnum;
import com.aizuda.easy.retry.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.strategy.WaitStrategies;
import com.aizuda.easy.retry.server.common.util.CronUtils;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.common.util.GraphUtils;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowPartitionTaskDTO;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.support.WorkflowPrePareHandler;
import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.expression.ExpressionInvocationHandler;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.SceneConfigRequestVO;
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
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.GroupConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.Workflow;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
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
    @Lazy
    private final WorkflowPrePareHandler terminalWorkflowPrepareHandler;
    private final JobMapper jobMapper;
    private final AccessTemplate accessTemplate;

    @Override
    @Transactional
    public boolean saveWorkflow(WorkflowRequestVO workflowRequestVO) {
        log.info("保存工作流信息：{}", JsonUtil.toJsonString(workflowRequestVO));
        MutableGraph<Long> graph = createGraph();

        // 添加虚拟头节点
        graph.addNode(SystemConstants.ROOT);

        // 组装工作流信息
        Workflow workflow = WorkflowConverter.INSTANCE.toWorkflow(workflowRequestVO);
        workflow.setVersion(1);
        workflow.setNextTriggerAt(calculateNextTriggerAt(workflowRequestVO, DateUtils.toNowMilli()));
        workflow.setFlowInfo(StrUtil.EMPTY);
        workflow.setBucketIndex(
            HashUtil.bkdrHash(workflowRequestVO.getGroupName() + workflowRequestVO.getWorkflowName())
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
        workflow.setVersion(null);
        workflow.setFlowInfo(JsonUtil.toJsonString(GraphUtils.serializeGraphToJson(graph)));
        Assert.isTrue(1 == workflowMapper.updateById(workflow), () -> new EasyRetryServerException("保存工作流图失败"));
        return true;
    }

    private MutableGraph<Long> createGraph() {
        return GraphBuilder.directed().nodeOrder(ElementOrder.sorted((Comparator<Long>) (o1, o2) -> {
            if (o1 - o2 > 0) {
                return 1;
            } else if (o1 - o2 < 0) {
                return -1;
            } else {
                return 0;
            }
        })).incidentEdgeOrder(ElementOrder.stable()).allowsSelfLoops(false).build();
    }

    private static Long calculateNextTriggerAt(final WorkflowRequestVO workflowRequestVO, Long time) {
        checkExecuteInterval(workflowRequestVO);

        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(workflowRequestVO.getTriggerType());
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(workflowRequestVO.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(time);
        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }

    private static void checkExecuteInterval(WorkflowRequestVO requestVO) {
        if (Lists.newArrayList(WaitStrategies.WaitStrategyEnum.FIXED.getType(),
                WaitStrategies.WaitStrategyEnum.RANDOM.getType())
            .contains(requestVO.getTriggerType())) {
            if (Integer.parseInt(requestVO.getTriggerInterval()) < 10) {
                throw new EasyRetryServerException("触发间隔不得小于10");
            }
        } else if (requestVO.getTriggerType() == WaitStrategies.WaitStrategyEnum.CRON.getType()) {
            if (CronUtils.getExecuteInterval(requestVO.getTriggerInterval()) < 10 * 1000) {
                throw new EasyRetryServerException("触发间隔不得小于10");
            }
        }
    }

    @Override
    public WorkflowDetailResponseVO getWorkflowDetail(Long id) {

        Workflow workflow = workflowMapper.selectOne(
            new LambdaQueryWrapper<Workflow>()
                .eq(Workflow::getId, id)
                .eq(Workflow::getNamespaceId, UserSessionUtils.currentUserSession().getNamespaceId())
        );
        if (Objects.isNull(workflow)) {
            return null;
        }

        WorkflowDetailResponseVO responseVO = WorkflowConverter.INSTANCE.toWorkflowDetailResponseVO(workflow);
        List<WorkflowNode> workflowNodes = workflowNodeMapper.selectList(new LambdaQueryWrapper<WorkflowNode>()
            .eq(WorkflowNode::getDeleted, 0)
            .eq(WorkflowNode::getVersion, workflow.getVersion())
            .eq(WorkflowNode::getWorkflowId, id)
            .orderByAsc(WorkflowNode::getPriorityLevel));

        List<Long> jobIds = workflowNodes.stream().map(WorkflowNode::getJobId).collect(Collectors.toList());
        List<Job> jobs = jobMapper.selectList(new LambdaQueryWrapper<Job>()
            .in(Job::getId, new HashSet<>(jobIds)));

        Map<Long, Job> jobMap = jobs.stream().collect(Collectors.toMap(Job::getId, job -> job));

        List<WorkflowDetailResponseVO.NodeInfo> nodeInfos = WorkflowConverter.INSTANCE.toNodeInfo(workflowNodes);

        Map<Long, WorkflowDetailResponseVO.NodeInfo> workflowNodeMap = nodeInfos.stream()
            .peek(nodeInfo -> {
                JobTaskConfig jobTask = nodeInfo.getJobTask();
                if (Objects.nonNull(jobTask)) {
                    jobTask.setJobName(jobMap.getOrDefault(jobTask.getJobId(), new Job()).getJobName());
                }
            }).collect(Collectors.toMap(WorkflowDetailResponseVO.NodeInfo::getId, i -> i));

        String flowInfo = workflow.getFlowInfo();
        try {
            MutableGraph<Long> graph = GraphUtils.deserializeJsonToGraph(flowInfo);
            // 反序列化构建图
            WorkflowDetailResponseVO.NodeConfig config = workflowHandler.buildNodeConfig(graph, SystemConstants.ROOT,
                new HashMap<>(),
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

        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            queryWrapper.eq(Workflow::getGroupName, queryVO.getGroupName());
        }

        if (StrUtil.isNotBlank(queryVO.getWorkflowName())) {
            queryWrapper.like(Workflow::getWorkflowName, queryVO.getWorkflowName());
        }

        if (Objects.nonNull(queryVO.getWorkflowStatus())) {
            queryWrapper.eq(Workflow::getWorkflowStatus, queryVO.getWorkflowStatus());
        }

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

        MutableGraph<Long> graph = createGraph();

        // 添加虚拟头节点
        graph.addNode(SystemConstants.ROOT);

        // 获取DAG节点配置
        NodeConfig nodeConfig = workflowRequestVO.getNodeConfig();

        int version = workflow.getVersion();
        // 递归构建图
        workflowHandler.buildGraph(Lists.newArrayList(SystemConstants.ROOT), new LinkedBlockingDeque<>(),
            workflowRequestVO.getGroupName(), workflowRequestVO.getId(), nodeConfig, graph, version + 1);

        log.info("图构建完成. graph:[{}]", graph);

        // 保存图信息
        workflow = WorkflowConverter.INSTANCE.toWorkflow(workflowRequestVO);;
        workflow.setId(workflowRequestVO.getId());
        workflow.setVersion(version);
        workflow.setNextTriggerAt(calculateNextTriggerAt(workflowRequestVO, DateUtils.toNowMilli()));
        workflow.setFlowInfo(JsonUtil.toJsonString(GraphUtils.serializeGraphToJson(graph)));
        Assert.isTrue(workflowMapper.update(workflow, new LambdaQueryWrapper<Workflow>()
            .eq(Workflow::getId, workflow.getId())
            .eq(Workflow::getVersion, version)
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

    @Override
    public Boolean trigger(Long id) {
        Workflow workflow = workflowMapper.selectById(id);
        Assert.notNull(workflow, () -> new EasyRetryServerException("workflow can not be null."));

        long count = accessTemplate.getGroupConfigAccess().count(new LambdaQueryWrapper<GroupConfig>()
            .eq(GroupConfig::getGroupName, workflow.getGroupName())
            .eq(GroupConfig::getNamespaceId, workflow.getNamespaceId())
            .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus())
        );

        Assert.isTrue(count > 0, () -> new EasyRetryServerException("组:[{}]已经关闭，不支持手动执行.", workflow.getGroupName()));

        WorkflowTaskPrepareDTO prepareDTO = WorkflowTaskConverter.INSTANCE.toWorkflowTaskPrepareDTO(workflow);
        // 设置now表示立即执行
        prepareDTO.setNextTriggerAt(DateUtils.toNowMilli());
        prepareDTO.setTaskExecutorScene(JobTaskExecutorSceneEnum.MANUAL_WORKFLOW.getType());

        terminalWorkflowPrepareHandler.handler(prepareDTO);

        return Boolean.TRUE;
    }

    @Override
    public List<WorkflowResponseVO> getWorkflowNameList(String keywords, Long workflowId) {

        LambdaQueryWrapper<Workflow> queryWrapper = new LambdaQueryWrapper<Workflow>()
                .select(Workflow::getId, Workflow::getWorkflowName);
        if (StrUtil.isNotBlank(keywords)) {
            queryWrapper.like(Workflow::getWorkflowName, keywords.trim() + "%");
        }

        if (Objects.nonNull(workflowId)) {
            queryWrapper.eq(Workflow::getId, workflowId);
        }

        queryWrapper.eq(Workflow::getDeleted, StatusEnum.NO.getStatus())
            .orderByAsc(Workflow::getId); // SQLServer 分页必须 ORDER BY
        PageDTO<Workflow> pageDTO = new PageDTO<>(1, 20);
        PageDTO<Workflow> selectPage = workflowMapper.selectPage(pageDTO, queryWrapper);

        return WorkflowConverter.INSTANCE.toWorkflowResponseVO(selectPage.getRecords());
    }

    @Override
    public Pair<Integer, String> checkNodeExpression(DecisionConfig decisionConfig) {
        try {
            ExpressionEngine realExpressionEngine = ExpressionTypeEnum.valueOf(decisionConfig.getExpressionType());
            Assert.notNull(realExpressionEngine, () -> new EasyRetryServerException("表达式引擎不存在"));
            ExpressionInvocationHandler invocationHandler = new ExpressionInvocationHandler(realExpressionEngine);
            ExpressionEngine expressionEngine = ExpressionFactory.getExpressionEngine(invocationHandler);
            expressionEngine.eval(decisionConfig.getNodeExpression(), StrUtil.EMPTY);
        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("表达式异常. [{}]", decisionConfig.getNodeExpression(), e);
            return Pair.of(StatusEnum.NO.getStatus(), e.getMessage());
        }

        return Pair.of(StatusEnum.YES.getStatus(), StrUtil.EMPTY);
    }

}
