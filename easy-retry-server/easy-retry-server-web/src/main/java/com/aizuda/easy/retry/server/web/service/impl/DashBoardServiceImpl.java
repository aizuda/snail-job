package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.dto.DistributeInstance;
import com.aizuda.easy.retry.server.common.dto.ServerNodeExtAttrs;
import com.aizuda.easy.retry.server.common.register.ServerRegister;
import com.aizuda.easy.retry.server.web.model.base.BaseQueryVO;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.enums.DateTypeEnum;
import com.aizuda.easy.retry.server.web.model.enums.RetryDateTypeEnum;
import com.aizuda.easy.retry.server.web.model.request.ServerNodeQueryVO;
import com.aizuda.easy.retry.server.web.model.response.*;
import com.aizuda.easy.retry.server.web.service.DashBoardService;
import com.aizuda.easy.retry.server.web.service.convert.*;
import com.aizuda.easy.retry.server.web.util.UserSessionUtils;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.ActivePodQuantityResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardRetryLinkeResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DispatchQuantityResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobSummaryMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetrySummaryMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: www.byteblogs.com
 * @date : 2022-04-22 20:19
 * @since 1.0.0
 */
@Service
@Slf4j
public class DashBoardServiceImpl implements DashBoardService {

    public static final String URL = "http://{0}:{1}/dashboard/consumer/bucket";

    @Autowired
    private ServerNodeMapper serverNodeMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JobSummaryMapper jobSummaryMapper;

    @Autowired
    private RetrySummaryMapper retrySummaryMapper;

    @Override
    public DashboardCardResponseVO taskRetryJob() {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        DashboardCardResponseVO dashboardCardResponseVO = new DashboardCardResponseVO();
        // 重试任务
        dashboardCardResponseVO.setRetryTask(RetrySummaryResponseVOConverter.INSTANCE.toRetryTask(retrySummaryMapper.retryTask(namespaceId)));
        // 定时任务
        dashboardCardResponseVO.setJobTask(JobSummaryResponseVOConverter.INSTANCE.toTaskJob(jobSummaryMapper.toJobTask(namespaceId)));
        // 重试任务柱状图
        dashboardCardResponseVO.setRetryTaskBarList(RetrySummaryResponseVOConverter.INSTANCE.toRetryTaskBar(retrySummaryMapper.retryTaskBarList(namespaceId)));
        // 在线Pods
        List<ActivePodQuantityResponseDO> activePodQuantityDO = serverNodeMapper.countActivePod(namespaceId);
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

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        DashboardRetryLineResponseVO dashboardRetryLineResponseVO = new DashboardRetryLineResponseVO();
        // 重试任务列表
        IPage<DashboardRetryLineResponseDO.Task> IPage = retrySummaryMapper.retryTaskList(namespaceId, new Page<>(baseQueryVO.getPage(), baseQueryVO.getSize()));
        List<DashboardRetryLineResponseVO.Task> taskList = JobSummaryResponseVOConverter.INSTANCE.toDashboardRetryLineResponseVO(IPage.getRecords());
        PageResult<List<DashboardRetryLineResponseVO.Task>> pageResult = new PageResult<>(new PageDTO(IPage.getCurrent(), IPage.getSize(), IPage.getTotal()), taskList);
        dashboardRetryLineResponseVO.setTaskList(pageResult);

        // 折线图
        RetryDateTypeEnum dateTypeEnum = RetryDateTypeEnum.valueOf(type);
        LocalDateTime startDateTime = dateTypeEnum.getStartTime().apply(StrUtil.isNotBlank(startTime) ? LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        LocalDateTime endDateTime = dateTypeEnum.getEndTime().apply(StrUtil.isNotBlank(endTime) ? LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        List<DashboardRetryLinkeResponseDO> dashboardRetryLinkeResponseDOList = retrySummaryMapper.retryLineList(namespaceId, type, startDateTime, endDateTime);
        List<DashboardRetryLinkeResponseVO> retryLinkeResponseVOList = DispatchQuantityResponseVOConverter.INSTANCE.toDashboardRetryLinkeResponseVO(dashboardRetryLinkeResponseDOList);
        dateTypeEnum.getConsumer().accept(retryLinkeResponseVOList);
        dashboardRetryLineResponseVO.setRetryLinkeResponseVOList(retryLinkeResponseVOList);

        // 排行榜
        List<DashboardRetryLineResponseDO.Rank> rankList = retrySummaryMapper.dashboardRank(namespaceId, groupName, startDateTime, endDateTime);
        List<DashboardRetryLineResponseVO.Rank> ranks = SceneQuantityRankResponseVOConverter.INSTANCE.toDashboardRetryLineResponseVORank(rankList);
        dashboardRetryLineResponseVO.setRankList(ranks);
        return dashboardRetryLineResponseVO;
    }

    @Override
    public DashboardRetryLineResponseVO jobLineList(BaseQueryVO baseQueryVO, String groupName, String type, String startTime, String endTime) {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        DashboardRetryLineResponseVO dashboardRetryLineResponseVO = new DashboardRetryLineResponseVO();
        // 重试任务列表
        IPage<DashboardRetryLineResponseDO.Task> IPage = jobSummaryMapper.jobTaskList(namespaceId, new Page<>(baseQueryVO.getPage(), baseQueryVO.getSize()));
        List<DashboardRetryLineResponseVO.Task> taskList = JobSummaryResponseVOConverter.INSTANCE.toDashboardRetryLineResponseVO(IPage.getRecords());
        PageResult<List<DashboardRetryLineResponseVO.Task>> pageResult = new PageResult<>(new PageDTO(IPage.getCurrent(), IPage.getSize(), IPage.getTotal()), taskList);
        dashboardRetryLineResponseVO.setTaskList(pageResult);

        // 折线图
        DateTypeEnum dateTypeEnum = DateTypeEnum.valueOf(type);
        LocalDateTime startDateTime = dateTypeEnum.getStartTime().apply(StrUtil.isNotBlank(startTime) ? LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        LocalDateTime endDateTime = dateTypeEnum.getEndTime().apply(StrUtil.isNotBlank(endTime) ? LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        List<DispatchQuantityResponseDO> dispatchQuantityResponseDOList = jobSummaryMapper.jobLineList(namespaceId, type, startDateTime, endDateTime);
        List<DispatchQuantityResponseVO> dispatchQuantityResponseVOList = DispatchQuantityResponseVOConverter.INSTANCE.toDispatchQuantityResponseVO(dispatchQuantityResponseDOList);
        dateTypeEnum.getConsumer().accept(dispatchQuantityResponseVOList);
        dashboardRetryLineResponseVO.setDispatchQuantityResponseVOList(dispatchQuantityResponseVOList);

        // 排行榜
        List<DashboardRetryLineResponseDO.Rank> rankList = jobSummaryMapper.dashboardRank(namespaceId, groupName, startDateTime, endDateTime);
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
                String format = MessageFormat.format(URL, serverNodeResponseVO.getHostIp(), serverNodeExtAttrs.getWebPort().toString());
                Result<List<Integer>> result = restTemplate.getForObject(format, Result.class);
                List<Integer> data = result.getData();
                if (!CollectionUtils.isEmpty(data)) {
                    serverNodeResponseVO.setConsumerBuckets(data.stream()
                            .sorted(Integer::compareTo)
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
                }
            } catch (Exception e) {
                LogUtils.error(log, "Failed to retrieve consumer group for node [{}:{}].", serverNodeResponseVO.getHostIp(), serverNodeExtAttrs.getWebPort());
            }
        }
        return new PageResult<>(serverNodePageDTO, serverNodeResponseVOS);
    }
}
