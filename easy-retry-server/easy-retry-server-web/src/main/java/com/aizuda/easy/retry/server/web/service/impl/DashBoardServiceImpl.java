package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.core.util.NetUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.dto.DistributeInstance;
import com.aizuda.easy.retry.server.common.dto.ServerNodeExtAttrs;
import com.aizuda.easy.retry.server.common.enums.DashboardLineEnum;
import com.aizuda.easy.retry.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.easy.retry.server.common.enums.SystemModeEnum;
import com.aizuda.easy.retry.server.common.register.ServerRegister;
import com.aizuda.easy.retry.server.web.model.base.BaseQueryVO;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.enums.DateTypeEnum;
import com.aizuda.easy.retry.server.web.model.request.ServerNodeQueryVO;
import com.aizuda.easy.retry.server.web.model.request.UserSessionVO;
import com.aizuda.easy.retry.server.web.model.response.DashboardCardResponseVO;
import com.aizuda.easy.retry.server.web.model.response.DashboardLineResponseVO;
import com.aizuda.easy.retry.server.web.model.response.DashboardRetryLineResponseVO;
import com.aizuda.easy.retry.server.web.model.response.ServerNodeResponseVO;
import com.aizuda.easy.retry.server.web.service.DashBoardService;
import com.aizuda.easy.retry.server.web.service.convert.*;
import com.aizuda.easy.retry.server.web.util.UserSessionUtils;
import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.ActivePodQuantityResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardCardResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardLineResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobSummaryMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetrySummaryMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.*;
import com.aizuda.easy.retry.template.datasource.utils.DbUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: www.byteblogs.com
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

    @Override
    public DashboardCardResponseVO taskRetryJob() {

        // 查询登录用户权限
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();
        List<String> groupNames = userSessionVO.isUser() ? userSessionVO.getGroupNames() : new ArrayList<>();
        DashboardCardResponseVO dashboardCardResponseVO = new DashboardCardResponseVO();
        // 重试任务
        LambdaQueryWrapper<RetrySummary> wrapper2 = new LambdaQueryWrapper<RetrySummary>()
                .eq(RetrySummary::getNamespaceId, namespaceId)
                .in(CollUtil.isNotEmpty(groupNames), RetrySummary::getGroupName, groupNames);
        DashboardCardResponseDO.RetryTask retryTaskDO = retrySummaryMapper.retryTask(wrapper2);
        DashboardCardResponseVO.RetryTask retryTaskVO = RetrySummaryResponseVOConverter.INSTANCE.toRetryTask(retryTaskDO);
        dashboardCardResponseVO.setRetryTask(retryTaskVO);
        // 定时任务
        LambdaQueryWrapper<JobSummary> wrapper = new LambdaQueryWrapper<JobSummary>()
                .eq(JobSummary::getSystemTaskType, SyetemTaskTypeEnum.JOB.getType())
                .eq(JobSummary::getNamespaceId, namespaceId)
                .in(CollUtil.isNotEmpty(groupNames), JobSummary::getGroupName, groupNames);
        DashboardCardResponseDO.JobTask jobTaskDO = jobSummaryMapper.toJobTask(wrapper);
        DashboardCardResponseVO.JobTask jobTaskVO = JobSummaryResponseVOConverter.INSTANCE.toTaskJob(jobTaskDO);
        dashboardCardResponseVO.setJobTask(jobTaskVO);
        // 工作流任务
        LambdaQueryWrapper<JobSummary> wrapper1 = new LambdaQueryWrapper<JobSummary>()
                .eq(JobSummary::getSystemTaskType, SyetemTaskTypeEnum.WORKFLOW.getType())
                .eq(JobSummary::getNamespaceId, namespaceId)
                .in(CollUtil.isNotEmpty(groupNames), JobSummary::getGroupName, groupNames);
        DashboardCardResponseDO.JobTask workFlowTaskDO = jobSummaryMapper.toJobTask(wrapper1);
        DashboardCardResponseVO.WorkFlowTask workFlowTaskVO = JobSummaryResponseVOConverter.INSTANCE.toWorkFlowTask(workFlowTaskDO);
        dashboardCardResponseVO.setWorkFlowTask(workFlowTaskVO);
        // 重试任务柱状图
        HashMap<LocalDateTime, DashboardCardResponseVO.RetryTaskBar> retryTaskBarMap = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            DashboardCardResponseVO.RetryTaskBar retryTaskBar = new DashboardCardResponseVO.RetryTaskBar().setX(LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusDays(-i).toLocalDate().toString()).setTaskTotal(0L);
            retryTaskBarMap.put(LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusDays(-i), retryTaskBar);
        }
        LambdaQueryWrapper<RetrySummary> wrapper3 = new LambdaQueryWrapper<RetrySummary>()
                .eq(RetrySummary::getNamespaceId, namespaceId)
                .in(CollUtil.isNotEmpty(groupNames), RetrySummary::getGroupName, groupNames)
                .orderByDesc(RetrySummary::getId);
        List<DashboardCardResponseDO.RetryTask> retryTaskList = retrySummaryMapper.retryTaskBarList(wrapper3);
        Map<LocalDateTime, LongSummaryStatistics> summaryStatisticsMap = retryTaskList.stream().collect(Collectors.groupingBy(DashboardCardResponseDO.RetryTask::getTriggerAt,
                Collectors.summarizingLong(i -> i.getMaxCountNum() + i.getRunningNum() + i.getSuspendNum() + i.getFinishNum())));
        for (Map.Entry<LocalDateTime, LongSummaryStatistics> map : summaryStatisticsMap.entrySet()) {
            if (retryTaskBarMap.containsKey(LocalDateTime.of(map.getKey().toLocalDate(), LocalTime.MIN))) {
                DashboardCardResponseVO.RetryTaskBar retryTaskBar = retryTaskBarMap.get(LocalDateTime.of(map.getKey().toLocalDate(), LocalTime.MIN));
                retryTaskBar.setX(map.getKey().toLocalDate().toString()).setTaskTotal(map.getValue().getSum());
            }
        }
        dashboardCardResponseVO.setRetryTaskBarList(new ArrayList<>(retryTaskBarMap.values()));
        // 在线Pods
        LambdaQueryWrapper<ServerNode> wrapper4 = new LambdaQueryWrapper<ServerNode>()
                .in(ServerNode::getNamespaceId, Lists.newArrayList(userSessionVO.getNamespaceId(), ServerRegister.NAMESPACE_ID))
                .groupBy(ServerNode::getNodeType);
        List<ActivePodQuantityResponseDO> activePodQuantityDO = serverNodeMapper.countActivePod(wrapper4);
        Map<Integer, Long> map = activePodQuantityDO.stream().collect(Collectors.toMap(ActivePodQuantityResponseDO::getNodeType, ActivePodQuantityResponseDO::getTotal));
        Long clientTotal = map.getOrDefault(NodeTypeEnum.CLIENT.getType(), 0L);
        Long serverTotal = map.getOrDefault(NodeTypeEnum.SERVER.getType(), 0L);
        dashboardCardResponseVO.getOnLineService().setServerTotal(serverTotal);
        dashboardCardResponseVO.getOnLineService().setClientTotal(clientTotal);
        dashboardCardResponseVO.getOnLineService().setTotal(clientTotal + serverTotal);

        return dashboardCardResponseVO;
    }

    @Override
    public DashboardRetryLineResponseVO retryLineList(BaseQueryVO baseQueryVO, String groupName, String type, String startTime, String endTime) {

        // 查询登录用户权限
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();
        List<String> groupNames = userSessionVO.isUser() ? userSessionVO.getGroupNames() : new ArrayList<>();
        DashboardRetryLineResponseVO dashboardRetryLineResponseVO = new DashboardRetryLineResponseVO();
        // 重试任务列表
        Page<Object> pager = new Page<>(baseQueryVO.getPage(), baseQueryVO.getSize());
        // 针对SQL Server的分页COUNT, 自定义statement ID
        if (DbTypeEnum.SQLSERVER == DbUtils.getDbType()) {
            pager.setCountId("sqlServer_jobTaskList_Count");
        }
        LambdaQueryWrapper<SceneConfig> wrapper = new LambdaQueryWrapper<SceneConfig>()
                .eq(SceneConfig::getNamespaceId, namespaceId)
                .in(CollUtil.isNotEmpty(groupNames), SceneConfig::getGroupName, groupNames);
        IPage<DashboardRetryLineResponseDO.Task> IPage = retrySummaryMapper.retryTaskList(wrapper, pager);
        List<DashboardRetryLineResponseVO.Task> taskList = JobSummaryResponseVOConverter.INSTANCE.toDashboardRetryLineResponseVO(IPage.getRecords());
        PageResult<List<DashboardRetryLineResponseVO.Task>> pageResult = new PageResult<>(new PageDTO(IPage.getCurrent(), IPage.getSize(), IPage.getTotal()), taskList);
        dashboardRetryLineResponseVO.setTaskList(pageResult);

        // 折线图
        DateTypeEnum dateTypeEnum = DateTypeEnum.valueOf(type);
        LocalDateTime startDateTime = dateTypeEnum.getStartTime().apply(StrUtil.isNotBlank(startTime) ? LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        LocalDateTime endDateTime = dateTypeEnum.getEndTime().apply(StrUtil.isNotBlank(endTime) ? LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        LambdaQueryWrapper<RetrySummary> wrapper1 = new LambdaQueryWrapper<RetrySummary>()
                .in(CollUtil.isNotEmpty(groupNames), RetrySummary::getGroupName, groupNames)
                .eq(StrUtil.isNotBlank(groupName), RetrySummary::getGroupName, groupName)
                .eq(RetrySummary::getNamespaceId, namespaceId)
                .between(RetrySummary::getTriggerAt, startDateTime, endDateTime);
        List<DashboardLineResponseDO> dashboardRetryLinkeResponseDOList = retrySummaryMapper.retryLineList(DashboardLineEnum.dateFormat(type), wrapper1);
        List<DashboardLineResponseVO> dashboardLineResponseVOList = DispatchQuantityResponseVOConverter.INSTANCE.toDashboardLineResponseVO(dashboardRetryLinkeResponseDOList);
        dateTypeEnum.getConsumer().accept(dashboardLineResponseVOList);
        dashboardLineResponseVOList.sort(Comparator.comparing(a -> a.getCreateDt()));
        dashboardRetryLineResponseVO.setDashboardLineResponseDOList(dashboardLineResponseVOList);

        // 排行榜
        LambdaQueryWrapper<RetrySummary> wrapper2 = new LambdaQueryWrapper<RetrySummary>()
                .in(CollUtil.isNotEmpty(groupNames), RetrySummary::getGroupName, groupNames)
                .eq(StrUtil.isNotBlank(groupName), RetrySummary::getGroupName, groupNames)
                .eq(RetrySummary::getNamespaceId, namespaceId)
                .ge(RetrySummary::getTriggerAt, startDateTime)
                .le(RetrySummary::getTriggerAt, endDateTime)
                .groupBy(RetrySummary::getNamespaceId, RetrySummary::getGroupName, RetrySummary::getSceneName);
        List<DashboardRetryLineResponseDO.Rank> rankList = retrySummaryMapper.dashboardRank(wrapper2);
        List<DashboardRetryLineResponseVO.Rank> ranks = SceneQuantityRankResponseVOConverter.INSTANCE.toDashboardRetryLineResponseVORank(rankList);
        dashboardRetryLineResponseVO.setRankList(ranks);
        return dashboardRetryLineResponseVO;
    }

    @Override
    public DashboardRetryLineResponseVO jobLineList(BaseQueryVO baseQueryVO, String mode, String groupName, String type, String startTime, String endTime) {

        // 查询登录用户权限
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();
        List<String> groupNames = userSessionVO.isUser() ? userSessionVO.getGroupNames() : new ArrayList<>();
        DashboardRetryLineResponseVO dashboardRetryLineResponseVO = new DashboardRetryLineResponseVO();
        // 重试任务列表
        Page<Object> pager = new Page<>(baseQueryVO.getPage(), baseQueryVO.getSize());
        // 针对SQL Server的分页COUNT, 自定义statement ID
        if (DbTypeEnum.SQLSERVER == DbUtils.getDbType()) {
            pager.setCountId("sqlServer_jobTaskList_Count");
        }
        // 任务类型
        Integer systemTaskType = SystemModeEnum.JOB.name().equals(mode) ? SyetemTaskTypeEnum.JOB.getType() : SyetemTaskTypeEnum.WORKFLOW.getType();
        LambdaQueryWrapper<Job> wrapper1 = new LambdaQueryWrapper<Job>()
                .eq(Job::getNamespaceId, namespaceId)
                .in(CollUtil.isNotEmpty(groupNames), Job::getGroupName, groupNames);
        IPage<DashboardRetryLineResponseDO.Task> IPage = jobSummaryMapper.jobTaskList(wrapper1, pager);
        List<DashboardRetryLineResponseVO.Task> taskList = JobSummaryResponseVOConverter.INSTANCE.toDashboardRetryLineResponseVO(IPage.getRecords());
        PageResult<List<DashboardRetryLineResponseVO.Task>> pageResult = new PageResult<>(new PageDTO(IPage.getCurrent(), IPage.getSize(), IPage.getTotal()), taskList);
        dashboardRetryLineResponseVO.setTaskList(pageResult);

        // 折线图
        DateTypeEnum dateTypeEnum = DateTypeEnum.valueOf(type);
        LocalDateTime startDateTime = dateTypeEnum.getStartTime().apply(StrUtil.isNotBlank(startTime) ? LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        LocalDateTime endDateTime = dateTypeEnum.getEndTime().apply(StrUtil.isNotBlank(endTime) ? LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        LambdaQueryWrapper<JobSummary> queryWrapper = new LambdaQueryWrapper<JobSummary>()
                .in(CollUtil.isNotEmpty(groupNames), JobSummary::getGroupName, groupNames)
                .eq(StrUtil.isNotBlank(groupName), JobSummary::getGroupName, groupName)
                .eq(JobSummary::getSystemTaskType, systemTaskType)
                .eq(JobSummary::getNamespaceId, namespaceId)
                .between(JobSummary::getTriggerAt, startDateTime, endDateTime);
        List<DashboardLineResponseDO> dashboardLineResponseDOList = jobSummaryMapper.jobLineList(DashboardLineEnum.dateFormat(type), queryWrapper);
        List<DashboardLineResponseVO> dashboardLineResponseVOList = DispatchQuantityResponseVOConverter.INSTANCE.toDashboardLineResponseVO(dashboardLineResponseDOList);
        dateTypeEnum.getConsumer().accept(dashboardLineResponseVOList);
        dashboardLineResponseVOList.sort(Comparator.comparing(a -> a.getCreateDt()));
        dashboardRetryLineResponseVO.setDashboardLineResponseDOList(dashboardLineResponseVOList);

        // 排行榜
        LambdaQueryWrapper<JobSummary> wrapper = new LambdaQueryWrapper<JobSummary>()
                .in(CollUtil.isNotEmpty(groupNames), JobSummary::getGroupName, groupNames)
                .eq(StrUtil.isNotBlank(groupName), JobSummary::getGroupName, groupName)
                .ge(JobSummary::getTriggerAt, startDateTime).le(JobSummary::getTriggerAt, endDateTime)
                .eq(JobSummary::getSystemTaskType, systemTaskType)
                .eq(JobSummary::getNamespaceId, namespaceId)
                .groupBy(JobSummary::getNamespaceId, JobSummary::getGroupName, JobSummary::getBusinessId);
        List<DashboardRetryLineResponseDO.Rank> rankList = jobSummaryMapper.dashboardRank(systemTaskType, wrapper);
        List<DashboardRetryLineResponseVO.Rank> ranks = SceneQuantityRankResponseVOConverter.INSTANCE.toDashboardRetryLineResponseVORank(rankList);
        dashboardRetryLineResponseVO.setRankList(ranks);
        return dashboardRetryLineResponseVO;
    }

    @Override
    public PageResult<List<ServerNodeResponseVO>> pods(ServerNodeQueryVO queryVO) {
        PageDTO<ServerNode> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<ServerNode> serverNodeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        serverNodeLambdaQueryWrapper.in(ServerNode::getNamespaceId, Lists.newArrayList(
                UserSessionUtils.currentUserSession().getNamespaceId(), ServerRegister.NAMESPACE_ID
        ));
        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            serverNodeLambdaQueryWrapper.eq(ServerNode::getGroupName, queryVO.getGroupName());
        }

        serverNodeLambdaQueryWrapper.ge(ServerNode::getExpireAt, LocalDateTime.now().minusSeconds(ServerRegister.DELAY_TIME + (ServerRegister.DELAY_TIME / 3)));
        PageDTO<ServerNode> serverNodePageDTO = serverNodeMapper.selectPage(pageDTO, serverNodeLambdaQueryWrapper.orderByDesc(ServerNode::getNodeType));

        List<ServerNodeResponseVO> serverNodeResponseVOS = ServerNodeResponseVOConverter.INSTANCE.toServerNodeResponseVO(serverNodePageDTO.getRecords());

        for (final ServerNodeResponseVO serverNodeResponseVO : serverNodeResponseVOS) {
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
                String url = NetUtil.getUrl(serverNodeResponseVO.getHostIp(), serverNodeExtAttrs.getWebPort(), serverNodeResponseVO.getContextPath());
                Result<List<Integer>> result = restTemplate.getForObject(url.concat(DASHBOARD_CONSUMER_BUCKET), Result.class);
                List<Integer> data = result.getData();
                if (!CollectionUtils.isEmpty(data)) {
                    serverNodeResponseVO.setConsumerBuckets(data.stream()
                            .sorted(Integer::compareTo)
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
                }
            } catch (Exception e) {
                EasyRetryLog.LOCAL.error("Failed to retrieve consumer group for node [{}:{}].", serverNodeResponseVO.getHostIp(), serverNodeExtAttrs.getWebPort());
            }
        }
        return new PageResult<>(serverNodePageDTO, serverNodeResponseVOS);
    }

}
