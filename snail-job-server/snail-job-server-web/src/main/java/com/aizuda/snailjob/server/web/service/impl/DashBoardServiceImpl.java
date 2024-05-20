package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.NodeTypeEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.NetUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.dto.DistributeInstance;
import com.aizuda.snailjob.server.common.dto.ServerNodeExtAttrs;
import com.aizuda.snailjob.server.common.enums.DashboardLineEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.enums.SystemModeEnum;
import com.aizuda.snailjob.server.common.register.ServerRegister;
import com.aizuda.snailjob.server.web.model.base.BaseQueryVO;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.enums.DateTypeEnum;
import com.aizuda.snailjob.server.web.model.request.ServerNodeQueryVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.DashboardCardResponseVO;
import com.aizuda.snailjob.server.web.model.response.DashboardLineResponseVO;
import com.aizuda.snailjob.server.web.model.response.DashboardRetryLineResponseVO;
import com.aizuda.snailjob.server.web.model.response.DashboardRetryLineResponseVO.Task;
import com.aizuda.snailjob.server.web.model.response.ServerNodeResponseVO;
import com.aizuda.snailjob.server.web.service.DashBoardService;
import com.aizuda.snailjob.server.web.service.convert.*;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.ActivePodQuantityResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.DashboardCardResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.DashboardLineResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobSummaryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetrySummaryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.*;
import com.aizuda.snailjob.template.datasource.utils.DbUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: opensnail
 * @date : 2022-04-22 20:19
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashBoardServiceImpl implements DashBoardService {

    private static final String DASHBOARD_CONSUMER_BUCKET = "/dashboard/consumer/bucket";

    private final ServerNodeMapper serverNodeMapper;
    private final RestTemplate restTemplate;
    private final JobSummaryMapper jobSummaryMapper;
    private final RetrySummaryMapper retrySummaryMapper;
    private final ServerProperties serverProperties;

    @Override
    public DashboardCardResponseVO taskRetryJob() {

        // 查询登录用户权限
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();
        List<String> groupNames = userSessionVO.isUser() ? userSessionVO.getGroupNames() : new ArrayList<>();
        DashboardCardResponseVO responseVO = new DashboardCardResponseVO();

        // 重试任务
        DashboardCardResponseDO.RetryTask retryTaskDO = retrySummaryMapper.retryTask(
                new LambdaQueryWrapper<RetrySummary>()
                        .eq(RetrySummary::getNamespaceId, namespaceId)
                        .in(CollUtil.isNotEmpty(groupNames), RetrySummary::getGroupName, groupNames));
        DashboardCardResponseVO.RetryTask retryTaskVO = RetrySummaryResponseVOConverter.INSTANCE.convert(retryTaskDO);
        responseVO.setRetryTask(retryTaskVO);

        // 定时任务
        DashboardCardResponseDO.JobTask jobTaskDO = jobSummaryMapper.toJobTask(
                new LambdaQueryWrapper<JobSummary>()
                        .eq(JobSummary::getSystemTaskType, SyetemTaskTypeEnum.JOB.getType())
                        .eq(JobSummary::getNamespaceId, namespaceId)
                        .in(CollUtil.isNotEmpty(groupNames), JobSummary::getGroupName, groupNames));
        DashboardCardResponseVO.JobTask jobTaskVO = JobSummaryResponseVOConverter.INSTANCE.convert(jobTaskDO);
        responseVO.setJobTask(jobTaskVO);

        // 工作流任务
        DashboardCardResponseDO.JobTask workFlowTaskDO = jobSummaryMapper.toJobTask(
                new LambdaQueryWrapper<JobSummary>()
                        .eq(JobSummary::getSystemTaskType, SyetemTaskTypeEnum.WORKFLOW.getType())
                        .eq(JobSummary::getNamespaceId, namespaceId)
                        .in(CollUtil.isNotEmpty(groupNames), JobSummary::getGroupName, groupNames));
        DashboardCardResponseVO.WorkFlowTask workFlowTaskVO = JobSummaryResponseVOConverter.INSTANCE.convertToWorkFlowTask(workFlowTaskDO);
        responseVO.setWorkFlowTask(workFlowTaskVO);

        // 重试任务柱状图
        HashMap<LocalDateTime, DashboardCardResponseVO.RetryTaskBar> retryTaskBarMap = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            DashboardCardResponseVO.RetryTaskBar retryTaskBar = new DashboardCardResponseVO.RetryTaskBar().setX(LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusDays(-i).toLocalDate().toString()).setTaskTotal(0L);
            retryTaskBarMap.put(LocalDateTimeUtil.beginOfDay(LocalDateTime.now().minusDays(i)), retryTaskBar);
        }
        List<DashboardCardResponseDO.RetryTask> retryTaskList = retrySummaryMapper.retryTaskBarList(
                new LambdaQueryWrapper<RetrySummary>()
                        .eq(RetrySummary::getNamespaceId, namespaceId)
                        .in(CollUtil.isNotEmpty(groupNames), RetrySummary::getGroupName, groupNames)
                        .orderByDesc(RetrySummary::getId));
        Map<LocalDateTime, LongSummaryStatistics> summaryStatisticsMap = retryTaskList.stream()
                .collect(Collectors.groupingBy(DashboardCardResponseDO.RetryTask::getTriggerAt,
                        Collectors.summarizingLong(i -> i.getMaxCountNum() + i.getRunningNum() + i.getSuspendNum() + i.getFinishNum())));
        for (Map.Entry<LocalDateTime, LongSummaryStatistics> map : summaryStatisticsMap.entrySet()) {
            if (retryTaskBarMap.containsKey(LocalDateTime.of(map.getKey().toLocalDate(), LocalTime.MIN))) {
                DashboardCardResponseVO.RetryTaskBar retryTaskBar = retryTaskBarMap.get(LocalDateTimeUtil.beginOfDay(map.getKey()));
                retryTaskBar.setX(map.getKey().toLocalDate().toString()).setTaskTotal(map.getValue().getSum());
            }
        }
        responseVO.setRetryTaskBarList(new ArrayList<>(retryTaskBarMap.values()));

        // 在线Pods
        List<ActivePodQuantityResponseDO> activePodQuantityDO = serverNodeMapper.countActivePod(
                new LambdaQueryWrapper<ServerNode>()
                        .in(ServerNode::getNamespaceId, Lists.newArrayList(userSessionVO.getNamespaceId(), ServerRegister.NAMESPACE_ID))
                        .groupBy(ServerNode::getNodeType));
        Map<Integer, Long> map = StreamUtils.toMap(activePodQuantityDO,
                ActivePodQuantityResponseDO::getNodeType, ActivePodQuantityResponseDO::getTotal);
        Long clientTotal = map.getOrDefault(NodeTypeEnum.CLIENT.getType(), 0L);
        Long serverTotal = map.getOrDefault(NodeTypeEnum.SERVER.getType(), 0L);
        responseVO.getOnLineService().setServerTotal(serverTotal);
        responseVO.getOnLineService().setClientTotal(clientTotal);
        responseVO.getOnLineService().setTotal(clientTotal + serverTotal);

        return responseVO;
    }

    @Override
    public DashboardRetryLineResponseVO retryLineList(BaseQueryVO baseQueryVO,
                                                      String groupName, String type,
                                                      String startTime, String endTime) {

        // 查询登录用户权限
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();
        List<String> groupNames = userSessionVO.isUser() ? userSessionVO.getGroupNames() : new ArrayList<>();
        DashboardRetryLineResponseVO responseVO = new DashboardRetryLineResponseVO();

        // 重试任务列表
        Page<Object> pager = new Page<>(baseQueryVO.getPage(), baseQueryVO.getSize());
        LambdaQueryWrapper<RetrySceneConfig> wrapper = new LambdaQueryWrapper<RetrySceneConfig>()

                .eq(RetrySceneConfig::getNamespaceId, namespaceId)
                .in(CollUtil.isNotEmpty(groupNames), RetrySceneConfig::getGroupName, groupNames);
        // 针对SQL Server的分页COUNT, 自定义statement ID
        if (DbTypeEnum.SQLSERVER == DbUtils.getDbType()) {
            pager.setSearchCount(false);
            pager.setTotal(retrySummaryMapper.countRetryTask(wrapper));
        }

        IPage<DashboardRetryLineResponseDO.Task> page = retrySummaryMapper.retryTaskList(wrapper, pager);
        List<DashboardRetryLineResponseVO.Task> taskList = JobSummaryResponseVOConverter.INSTANCE.convertList(page.getRecords());
        PageResult<List<Task>> pageResult = new PageResult<>(
                new PageDTO(pager.getCurrent(), pager.getSize(), pager.getTotal()),
                taskList);
        responseVO.setTaskList(pageResult);

        // 折线图
        DateTypeEnum dateTypeEnum = DateTypeEnum.valueOf(type);
        LocalDateTime startDateTime = dateTypeEnum.getStartTime().apply(
                StrUtil.isNotBlank(startTime) ?
                        LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
                        LocalDateTime.now());
        LocalDateTime endDateTime = dateTypeEnum.getEndTime().apply(
                StrUtil.isNotBlank(endTime) ?
                        LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
                        LocalDateTime.now());
        List<DashboardLineResponseDO> dashboardRetryLinkeResponseDOList = retrySummaryMapper.retryLineList(
                DashboardLineEnum.dateFormat(type),
                new LambdaQueryWrapper<RetrySummary>()
                        .in(CollUtil.isNotEmpty(groupNames), RetrySummary::getGroupName, groupNames)
                        .eq(StrUtil.isNotBlank(groupName), RetrySummary::getGroupName, groupName)
                        .eq(RetrySummary::getNamespaceId, namespaceId)
                        .between(RetrySummary::getTriggerAt, startDateTime, endDateTime));
        List<DashboardLineResponseVO> dashboardLineResponseVOList = DispatchQuantityResponseVOConverter.INSTANCE.convertList(dashboardRetryLinkeResponseDOList);
        dateTypeEnum.getConsumer().accept(dashboardLineResponseVOList);
        dashboardLineResponseVOList.sort(Comparator.comparing(a -> a.getCreateDt()));
        responseVO.setDashboardLineResponseDOList(dashboardLineResponseVOList);

        // 排行榜
        List<DashboardRetryLineResponseDO.Rank> rankList = retrySummaryMapper.dashboardRank(
                new LambdaQueryWrapper<RetrySummary>()
                        .in(CollUtil.isNotEmpty(groupNames), RetrySummary::getGroupName, groupNames)
                        .eq(StrUtil.isNotBlank(groupName), RetrySummary::getGroupName, groupNames)
                        .eq(RetrySummary::getNamespaceId, namespaceId)
                        .ge(RetrySummary::getTriggerAt, startDateTime)
                        .le(RetrySummary::getTriggerAt, endDateTime)
                        .groupBy(RetrySummary::getNamespaceId, RetrySummary::getGroupName, RetrySummary::getSceneName));
        List<DashboardRetryLineResponseVO.Rank> ranks = SceneQuantityRankResponseVOConverter.INSTANCE.convertList(rankList);
        responseVO.setRankList(ranks);
        return responseVO;
    }

    @Override
    public DashboardRetryLineResponseVO jobLineList(BaseQueryVO baseQueryVO,
                                                    String mode, String groupName, String type,
                                                    String startTime, String endTime) {

        // 查询登录用户权限
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();
        List<String> groupNames = userSessionVO.isUser() ? userSessionVO.getGroupNames() : new ArrayList<>();
        DashboardRetryLineResponseVO responseVO = new DashboardRetryLineResponseVO();

        // 重试任务列表
        Page<Object> pager = new Page<>(baseQueryVO.getPage(), baseQueryVO.getSize());

        // 任务类型
        Integer systemTaskType = SystemModeEnum.JOB.name().equals(mode) ? SyetemTaskTypeEnum.JOB.getType() : SyetemTaskTypeEnum.WORKFLOW.getType();
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<Job>()
                .eq(Job::getDeleted, 0)
                .eq(Job::getNamespaceId, namespaceId)
                .in(CollUtil.isNotEmpty(groupNames), Job::getGroupName, groupNames);
        // 针对SQL Server的分页COUNT, 自定义statement ID
        if (DbTypeEnum.SQLSERVER == DbUtils.getDbType()) {
            pager.setSearchCount(false);
            pager.setTotal(jobSummaryMapper.countJobTask(wrapper));
        }

        IPage<DashboardRetryLineResponseDO.Task> taskIPage = SystemModeEnum.JOB.name().equals(mode) ?
                jobSummaryMapper.jobTaskList(wrapper, pager) : jobSummaryMapper.workflowTaskList(wrapper, pager);
        List<DashboardRetryLineResponseVO.Task> taskList = JobSummaryResponseVOConverter.INSTANCE.convertList(taskIPage.getRecords());
        PageResult<List<DashboardRetryLineResponseVO.Task>> pageResult = new PageResult<>(
                new PageDTO(pager.getCurrent(), pager.getSize(), pager.getTotal()),
                taskList);
        responseVO.setTaskList(pageResult);

        // 折线图
        DateTypeEnum dateTypeEnum = DateTypeEnum.valueOf(type);
        LocalDateTime startDateTime = dateTypeEnum.getStartTime().apply(
                StrUtil.isNotBlank(startTime) ?
                        LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
                        LocalDateTime.now());
        LocalDateTime endDateTime = dateTypeEnum.getEndTime().apply(
                StrUtil.isNotBlank(endTime) ?
                        LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
                        LocalDateTime.now());
        List<DashboardLineResponseDO> dashboardLineResponseDOList = jobSummaryMapper.jobLineList(
                DashboardLineEnum.dateFormat(type),
                new LambdaQueryWrapper<JobSummary>()
                        .in(CollUtil.isNotEmpty(groupNames), JobSummary::getGroupName, groupNames)
                        .eq(StrUtil.isNotBlank(groupName), JobSummary::getGroupName, groupName)
                        .eq(JobSummary::getSystemTaskType, systemTaskType)
                        .eq(JobSummary::getNamespaceId, namespaceId)
                        .between(JobSummary::getTriggerAt, startDateTime, endDateTime));
        List<DashboardLineResponseVO> dashboardLineResponseVOList = DispatchQuantityResponseVOConverter.INSTANCE.convertList(dashboardLineResponseDOList);
        dateTypeEnum.getConsumer().accept(dashboardLineResponseVOList);
        dashboardLineResponseVOList.sort(Comparator.comparing(DashboardLineResponseVO::getCreateDt));
        responseVO.setDashboardLineResponseDOList(dashboardLineResponseVOList);

        // 排行榜
        List<DashboardRetryLineResponseDO.Rank> rankList = jobSummaryMapper.dashboardRank(
                systemTaskType,
                new LambdaQueryWrapper<JobSummary>()
                        .in(CollUtil.isNotEmpty(groupNames), JobSummary::getGroupName, groupNames)
                        .eq(StrUtil.isNotBlank(groupName), JobSummary::getGroupName, groupName)
                        .ge(JobSummary::getTriggerAt, startDateTime).le(JobSummary::getTriggerAt, endDateTime)
                        .eq(JobSummary::getSystemTaskType, systemTaskType)
                        .eq(JobSummary::getNamespaceId, namespaceId)
                        .groupBy(JobSummary::getNamespaceId, JobSummary::getGroupName, JobSummary::getBusinessId));
        List<DashboardRetryLineResponseVO.Rank> ranks = SceneQuantityRankResponseVOConverter.INSTANCE.convertList(rankList);
        responseVO.setRankList(ranks);
        return responseVO;
    }

    @Override
    public PageResult<List<ServerNodeResponseVO>> pods(ServerNodeQueryVO queryVO) {
        PageDTO<ServerNode> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<ServerNode> serverNodeLambdaQueryWrapper = new LambdaQueryWrapper<ServerNode>()
                .in(ServerNode::getNamespaceId, Lists.newArrayList(
                        UserSessionUtils.currentUserSession().getNamespaceId(), ServerRegister.NAMESPACE_ID))
                .eq(StrUtil.isNotBlank(queryVO.getGroupName()), ServerNode::getGroupName, queryVO.getGroupName())
                .ge(ServerNode::getExpireAt, LocalDateTime.now().minusSeconds(ServerRegister.DELAY_TIME + (ServerRegister.DELAY_TIME / 3)))
                .orderByDesc(ServerNode::getNodeType);
        PageDTO<ServerNode> serverNodePageDTO = serverNodeMapper.selectPage(pageDTO, serverNodeLambdaQueryWrapper);
        List<ServerNodeResponseVO> responseVOList = ServerNodeResponseVOConverter.INSTANCE.convertList(serverNodePageDTO.getRecords());

        for (final ServerNodeResponseVO serverNodeResponseVO : responseVOList) {
            if (NodeTypeEnum.CLIENT.getType().equals(serverNodeResponseVO.getNodeType())) {
                continue;
            }

            // 若是本地节点则直接从缓存中取
            if (ServerRegister.CURRENT_CID.equals(serverNodeResponseVO.getHostId())) {
                serverNodeResponseVO.setConsumerBuckets(DistributeInstance.INSTANCE.getConsumerBucket());
                continue;
            }
            if (StringUtils.isBlank(serverNodeResponseVO.getExtAttrs())) {
                continue;
            }
            ServerNodeExtAttrs serverNodeExtAttrs = JsonUtil.parseObject(serverNodeResponseVO.getExtAttrs(), ServerNodeExtAttrs.class);
            try {
                // 从远程节点取
                String url = NetUtil.getUrl(serverNodeResponseVO.getHostIp(), serverNodeExtAttrs.getWebPort(), serverProperties.getServlet().getContextPath());
                Result<List<Integer>> result = restTemplate.getForObject(url.concat(DASHBOARD_CONSUMER_BUCKET), Result.class);
                List<Integer> data = result.getData();
                if (!CollUtil.isEmpty(data)) {
                    serverNodeResponseVO.setConsumerBuckets(data.stream()
                            .sorted(Integer::compareTo)
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
                }
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("Failed to retrieve consumer group for node [{}:{}].", serverNodeResponseVO.getHostIp(), serverNodeExtAttrs.getWebPort());
            }
        }
        return new PageResult<>(serverNodePageDTO, responseVOList);
    }

}
