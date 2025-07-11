package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.expression.ExpressionEngine;
import com.aizuda.snailjob.common.core.expression.ExpressionFactory;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.JobTaskConfig;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.enums.ExpressionTypeEnum;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.*;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.common.vo.request.WorkflowRequestVO;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.support.WorkflowPrePareHandler;
import com.aizuda.snailjob.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.snailjob.server.job.task.support.expression.ExpressionInvocationHandler;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.server.common.vo.request.WorkflowRequestVO.NodeConfig;
import com.aizuda.snailjob.server.common.vo.WorkflowDetailResponseVO;
import com.aizuda.snailjob.server.common.vo.WorkflowResponseVO;
import com.aizuda.snailjob.server.web.service.WorkflowService;
import com.aizuda.snailjob.server.common.convert.WorkflowConverter;
import com.aizuda.snailjob.server.web.service.handler.GroupHandler;
import com.aizuda.snailjob.server.common.handler.WorkflowHandler;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.*;
import com.aizuda.snailjob.template.datasource.persistence.po.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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
@Validated
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowMapper workflowMapper;
    private final WorkflowNodeMapper workflowNodeMapper;
    private final SystemProperties systemProperties;
    private final WorkflowHandler workflowHandler;
    private final WorkflowPrePareHandler terminalWorkflowPrepareHandler;
    private final JobMapper jobMapper;
    private final AccessTemplate accessTemplate;
    private final GroupHandler groupHandler;
    private final JobSummaryMapper jobSummaryMapper;
    private final SystemUserMapper systemUserMapper;

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
                throw new SnailJobServerException("Trigger interval must not be less than 10");
            }
        } else if (Objects.equals(requestVO.getTriggerType(), WaitStrategies.WaitStrategyEnum.CRON.getType())) {
            if (CronUtils.getExecuteInterval(requestVO.getTriggerInterval()) < 10 * 1000) {
                throw new SnailJobServerException("Trigger interval must not be less than 10");
            }
        }
    }

    @Override
    @Transactional
    public boolean saveWorkflow(WorkflowRequestVO workflowRequestVO) {
        log.info("Saved workflow information: {}", JsonUtil.toJsonString(workflowRequestVO));
        MutableGraph<Long> graph = createGraph();

        // 添加虚拟头节点
        graph.addNode(SystemConstants.ROOT);

        // 组装工作流信息
        Workflow workflow = WorkflowConverter.INSTANCE.convert(workflowRequestVO);

        checkTriggerInterval(workflowRequestVO);

        workflow.setVersion(1);
        workflowRequestVO.setTriggerInterval(workflow.getTriggerInterval());
        workflow.setNextTriggerAt(calculateNextTriggerAt(workflowRequestVO, DateUtils.toNowMilli()));
        workflow.setFlowInfo(StrUtil.EMPTY);
        workflow.setBucketIndex(
                HashUtil.bkdrHash(workflowRequestVO.getGroupName() + workflowRequestVO.getWorkflowName())
                        % systemProperties.getBucketTotal());
        workflow.setNamespaceId(UserSessionUtils.currentUserSession().getNamespaceId());
        workflow.setId(null);
        Assert.isTrue(1 == workflowMapper.insert(workflow), () -> new SnailJobServerException("Failed to add workflow"));

        // 获取DAG节点配置
        NodeConfig nodeConfig = workflowRequestVO.getNodeConfig();

        // 递归构建图
        workflowHandler.buildGraph(Lists.newArrayList(SystemConstants.ROOT),
                new LinkedBlockingDeque<>(),
                workflowRequestVO.getGroupName(),
                workflow.getId(), nodeConfig, graph,
                workflow.getVersion(), workflow.getNamespaceId());
        log.info("Graph construction complete. graph:[{}]", graph);

        // 保存图信息
        workflow.setVersion(null);
        workflow.setOwnerId(Optional.ofNullable(workflowRequestVO.getOwnerId()).orElse(0L));
        workflow.setFlowInfo(JsonUtil.toJsonString(GraphUtils.serializeGraphToJson(graph)));
        Assert.isTrue(1 == workflowMapper.updateById(workflow), () -> new SnailJobServerException("Failed to save workflow graph"));
        return true;
    }

    private void checkTriggerInterval(WorkflowRequestVO workflowRequestVO) {
        TriggerIntervalUtils.checkTriggerInterval(workflowRequestVO.getTriggerInterval(), workflowRequestVO.getTriggerType());
    }

    private MutableGraph<Long> createGraph() {
        return GraphBuilder.directed()
                .nodeOrder(ElementOrder.sorted(Long::compare))
                .incidentEdgeOrder(ElementOrder.stable())
                .allowsSelfLoops(false)
                .build();
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

        return doGetWorkflowDetail(workflow);
    }

    @Override
    public PageResult<List<WorkflowResponseVO>> listPage(final WorkflowQueryVO queryVO) {
        PageDTO<Workflow> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        List<String> groupNames = UserSessionUtils.getGroupNames(queryVO.getGroupName());

        PageDTO<Workflow> page = workflowMapper.selectPage(pageDTO,
                new LambdaQueryWrapper<Workflow>()
                        .eq(Workflow::getDeleted, StatusEnum.NO.getStatus())
                        .eq(Workflow::getNamespaceId, userSessionVO.getNamespaceId())
                        .in(CollUtil.isNotEmpty(groupNames), Workflow::getGroupName, groupNames)
                        .like(StrUtil.isNotBlank(queryVO.getWorkflowName()), Workflow::getWorkflowName, queryVO.getWorkflowName())
                        .eq(Objects.nonNull(queryVO.getWorkflowStatus()), Workflow::getWorkflowStatus, queryVO.getWorkflowStatus())
                        .eq(Objects.nonNull(queryVO.getOwnerId()), Workflow::getOwnerId, queryVO.getOwnerId())
                        .orderByDesc(Workflow::getId));

        List<WorkflowResponseVO> workflowResponseVOList = WorkflowConverter.INSTANCE.convertListToWorkflowList(page.getRecords());
        for (WorkflowResponseVO responseVO : workflowResponseVOList) {
            if (Objects.nonNull(responseVO.getOwnerId()) && responseVO.getOwnerId() > 0) {
                SystemUser systemUser = systemUserMapper.selectById(responseVO.getOwnerId());
                responseVO.setOwnerName(systemUser.getUsername());
            }
        }
        return new PageResult<>(pageDTO, workflowResponseVOList);
    }

    @Override
    @Transactional
    public Boolean updateWorkflow(WorkflowRequestVO workflowRequestVO) {

        Assert.notNull(workflowRequestVO.getId(), () -> new SnailJobServerException("Workflow ID cannot be null"));

        Workflow workflow = workflowMapper.selectById(workflowRequestVO.getId());
        Assert.notNull(workflow, () -> new SnailJobServerException("Workflow does not exist"));

        MutableGraph<Long> graph = createGraph();

        // 添加虚拟头节点
        graph.addNode(SystemConstants.ROOT);

        // 获取DAG节点配置
        NodeConfig nodeConfig = workflowRequestVO.getNodeConfig();

        int version = workflow.getVersion();
        // 递归构建图
        workflowHandler.buildGraph(Lists.newArrayList(SystemConstants.ROOT), new LinkedBlockingDeque<>(),
                workflowRequestVO.getGroupName(), workflowRequestVO.getId(), nodeConfig, graph, version + 1, workflow.getNamespaceId());

        log.info("Graph construction complete. graph:[{}]", graph);

        // 保存图信息
        workflow = WorkflowConverter.INSTANCE.convert(workflowRequestVO);

        checkTriggerInterval(workflowRequestVO);

        workflow.setId(workflowRequestVO.getId());
        workflow.setVersion(version);
        workflowRequestVO.setTriggerInterval(workflow.getTriggerInterval());
        workflow.setNextTriggerAt(calculateNextTriggerAt(workflowRequestVO, DateUtils.toNowMilli()));
        workflow.setFlowInfo(JsonUtil.toJsonString(GraphUtils.serializeGraphToJson(graph)));
        // 不允许更新组
        workflow.setGroupName(null);
        workflow.setOwnerId(Optional.ofNullable(workflowRequestVO.getOwnerId()).orElse(0L));

        LambdaUpdateWrapper<Workflow> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Workflow::getId, workflow.getId());
        updateWrapper.eq(Workflow::getVersion, version);

        Assert.isTrue(workflowMapper.update(workflow, updateWrapper) > 0,
                () -> new SnailJobServerException("Update failed"));

        return Boolean.TRUE;
    }

    @Override
    public Boolean updateStatus(Long id) {
        Workflow workflow = workflowMapper.selectOne(
                new LambdaQueryWrapper<Workflow>()
                        .select(Workflow::getId, Workflow::getWorkflowStatus)
                        .eq(Workflow::getId, id));
        Assert.notNull(workflow, () -> new SnailJobServerException("Workflow does not exist"));

        if (Objects.equals(workflow.getWorkflowStatus(), StatusEnum.NO.getStatus())) {
            workflow.setWorkflowStatus(StatusEnum.YES.getStatus());
        } else {
            workflow.setWorkflowStatus(StatusEnum.NO.getStatus());
        }

        return 1 == workflowMapper.updateById(workflow);
    }

    @Override
    public Boolean trigger(WorkflowTriggerVO triggerVO) {
        Workflow workflow = workflowMapper.selectById(triggerVO.getWorkflowId());
        Assert.notNull(workflow, () -> new SnailJobServerException("workflow can not be null."));

        long count = accessTemplate.getGroupConfigAccess().count(
                new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getGroupName, workflow.getGroupName())
                        .eq(GroupConfig::getNamespaceId, workflow.getNamespaceId())
                        .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus())
        );

        Assert.isTrue(count > 0,
                () -> new SnailJobServerException("Group [{}] is closed, manual execution is not supported.", workflow.getGroupName()));

        WorkflowTaskPrepareDTO prepareDTO = WorkflowTaskConverter.INSTANCE.toWorkflowTaskPrepareDTO(workflow);
        // 设置now表示立即执行
        prepareDTO.setNextTriggerAt(DateUtils.toNowMilli());
        prepareDTO.setTaskExecutorScene(JobTaskExecutorSceneEnum.MANUAL_WORKFLOW.getType());
        String tmpWfContext = triggerVO.getTmpWfContext();
        if (StrUtil.isNotBlank(tmpWfContext) && !JsonUtil.isEmptyJson(tmpWfContext)) {
            prepareDTO.setWfContext(tmpWfContext);
        }
        terminalWorkflowPrepareHandler.handler(prepareDTO);

        return Boolean.TRUE;
    }

    @Override
    public List<WorkflowResponseVO> getWorkflowNameList(String keywords, Long workflowId, String groupName) {
        PageDTO<Workflow> selectPage = workflowMapper.selectPage(
                new PageDTO<>(1, 100),
                new LambdaQueryWrapper<Workflow>()
                        .select(Workflow::getId, Workflow::getWorkflowName)
                        .like(StrUtil.isNotBlank(keywords), Workflow::getWorkflowName, StrUtil.trim(keywords))
                        .eq(Objects.nonNull(workflowId), Workflow::getId, workflowId)
                        .eq(StrUtil.isNotBlank(groupName), Workflow::getGroupName, groupName)
                        .eq(Workflow::getDeleted, StatusEnum.NO.getStatus())
                        .orderByDesc(Workflow::getId));

        return WorkflowConverter.INSTANCE.convertListToWorkflowList(selectPage.getRecords());
    }

    @Override
    public Pair<Integer, Object> checkNodeExpression(CheckDecisionVO decisionVO) {
        try {
            ExpressionEngine realExpressionEngine = ExpressionTypeEnum.valueOf(decisionVO.getExpressionType());
            Assert.notNull(realExpressionEngine, () -> new SnailJobServerException("Expression engine does not exist"));
            ExpressionInvocationHandler invocationHandler = new ExpressionInvocationHandler(realExpressionEngine);
            ExpressionEngine expressionEngine = ExpressionFactory.getExpressionEngine(invocationHandler);
            Object eval = expressionEngine.eval(decisionVO.getNodeExpression(), decisionVO.getCheckContent());
            return Pair.of(StatusEnum.YES.getStatus(), eval);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("Expression exception. [{}]", decisionVO.getNodeExpression(), e);
            return Pair.of(StatusEnum.NO.getStatus(), e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importWorkflowTask(List<WorkflowRequestVO> requests) {

        batchSaveWorkflowTask(requests, UserSessionUtils.currentUserSession().getNamespaceId());
    }

    @Override
    public String exportWorkflowTask(ExportWorkflowVO exportVO) {

        List<WorkflowDetailResponseVO> resultList = new ArrayList<>();
        PartitionTaskUtils.process(startId -> {
            List<Workflow> workflowList = workflowMapper.selectPage(new PageDTO<>(0, 100, Boolean.FALSE),
                    new LambdaQueryWrapper<Workflow>()
                            .eq(Workflow::getNamespaceId, UserSessionUtils.currentUserSession().getNamespaceId())
                            .eq(Workflow::getDeleted, StatusEnum.NO.getStatus())
                            .eq(StrUtil.isNotBlank(exportVO.getGroupName()), Workflow::getGroupName, exportVO.getGroupName())
                            .eq(Objects.nonNull(exportVO.getWorkflowStatus()), Workflow::getWorkflowStatus,
                                    exportVO.getWorkflowStatus())
                            .likeRight(StrUtil.isNotBlank(exportVO.getWorkflowName()), Workflow::getWorkflowName,
                                    exportVO.getWorkflowName())
                            .in(CollUtil.isNotEmpty(exportVO.getWorkflowIds()), Workflow::getId, exportVO.getWorkflowIds())
                            .ge(Workflow::getId, startId)
                            .orderByAsc(Workflow::getId)
            ).getRecords();
            return workflowList.stream()
                    .map(this::doGetWorkflowDetail)
                    .map(WorkflowPartitionTask::new)
                    .collect(Collectors.toList());
        }, partitionTasks -> {
            List<WorkflowPartitionTask> workflowPartitionTasks = (List<WorkflowPartitionTask>) partitionTasks;
            resultList.addAll(StreamUtils.toList(workflowPartitionTasks, WorkflowPartitionTask::getResponseVO));
        }, 0);

        return JsonUtil.toJsonString(resultList);
    }

    @Override
    @Transactional
    public Boolean deleteByIds(Set<Long> ids) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        Assert.isTrue(ids.size() == workflowMapper.delete(
                new LambdaQueryWrapper<Workflow>()
                        .eq(Workflow::getNamespaceId, namespaceId)
                        .eq(Workflow::getWorkflowStatus, StatusEnum.NO.getStatus())
                        .in(Workflow::getId, ids)
        ), () -> new SnailJobServerException("Failed to delete workflow task, please check if the task status is closed"));

        List<JobSummary> jobSummaries = jobSummaryMapper.selectList(new LambdaQueryWrapper<JobSummary>()
                .select(JobSummary::getId)
                .in(JobSummary::getBusinessId, ids)
                .eq(JobSummary::getNamespaceId, namespaceId)
                .eq(JobSummary::getSystemTaskType, SyetemTaskTypeEnum.WORKFLOW.getType())
        );
        if (CollUtil.isNotEmpty(jobSummaries)) {
            Assert.isTrue(jobSummaries.size() ==
                            jobSummaryMapper.deleteByIds(StreamUtils.toSet(jobSummaries, JobSummary::getId)),
                    () -> new SnailJobServerException("Summary table deletion failed")
            );
        }
        return Boolean.TRUE;
    }

    private void batchSaveWorkflowTask(final List<WorkflowRequestVO> workflowRequestVOList, final String namespaceId) {

        Set<String> groupNameSet = StreamUtils.toSet(workflowRequestVOList, WorkflowRequestVO::getGroupName);
        groupHandler.validateGroupExistence(groupNameSet, namespaceId);

        for (final WorkflowRequestVO workflowRequestVO : workflowRequestVOList) {
            checkExecuteInterval(workflowRequestVO);
            workflowRequestVO.setId(null);
            saveWorkflow(workflowRequestVO);
        }
    }

    private WorkflowDetailResponseVO doGetWorkflowDetail(final Workflow workflow) {
        WorkflowDetailResponseVO responseVO = WorkflowConverter.INSTANCE.convert(workflow);
        List<WorkflowNode> workflowNodes = workflowNodeMapper.selectList(new LambdaQueryWrapper<WorkflowNode>()
                .eq(WorkflowNode::getDeleted, 0)
                .eq(WorkflowNode::getVersion, workflow.getVersion())
                .eq(WorkflowNode::getWorkflowId, workflow.getId())
                .orderByAsc(WorkflowNode::getPriorityLevel));

        List<Long> jobIds = StreamUtils.toList(workflowNodes, WorkflowNode::getJobId);
        List<Job> jobs = jobMapper.selectList(new LambdaQueryWrapper<Job>()
                .in(Job::getId, new HashSet<>(jobIds)));

        Map<Long, Job> jobMap = StreamUtils.toIdentityMap(jobs, Job::getId);

        List<WorkflowDetailResponseVO.NodeInfo> nodeInfos = WorkflowConverter.INSTANCE.convertList(workflowNodes);

        Map<Long, WorkflowDetailResponseVO.NodeInfo> workflowNodeMap = nodeInfos.stream()
                .peek(nodeInfo -> {
                    JobTaskConfig jobTask = nodeInfo.getJobTask();
                    if (Objects.nonNull(jobTask)) {
                        jobTask.setJobName(jobMap.getOrDefault(jobTask.getJobId(), new Job()).getJobName());
                        jobTask.setLabels(jobMap.getOrDefault(jobTask.getJobId(), new Job()).getLabels());
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
            log.error("Deserialization failed. json:[{}]", flowInfo, e);
            throw new SnailJobServerException("Failed to query workflow details");
        }
        return responseVO;
    }


    @EqualsAndHashCode(callSuper = true)
    @Getter
    private static class WorkflowPartitionTask extends PartitionTask {

        private final WorkflowDetailResponseVO responseVO;

        public WorkflowPartitionTask(@NotNull WorkflowDetailResponseVO responseVO) {
            this.responseVO = responseVO;
            setId(responseVO.getId());
        }
    }
}
