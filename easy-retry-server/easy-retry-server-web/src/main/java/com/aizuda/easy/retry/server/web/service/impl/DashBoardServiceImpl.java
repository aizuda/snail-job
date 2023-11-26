package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.common.dto.DistributeInstance;
import com.aizuda.easy.retry.server.common.dto.ServerNodeExtAttrs;
import com.aizuda.easy.retry.server.common.register.ServerRegister;
import com.aizuda.easy.retry.server.web.service.convert.DispatchQuantityResponseVOConverter;
import com.aizuda.easy.retry.server.web.service.convert.SceneQuantityRankResponseVOConverter;
import com.aizuda.easy.retry.server.web.util.UserSessionUtils;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.ActivePodQuantityResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DispatchQuantityResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.SceneQuantityRankResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.easy.retry.template.datasource.persistence.po.ServerNode;
import com.aizuda.easy.retry.server.web.service.convert.ServerNodeResponseVOConverter;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.ServerNodeQueryVO;
import com.aizuda.easy.retry.server.web.model.response.ServerNodeResponseVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.server.web.service.DashBoardService;
import com.aizuda.easy.retry.server.web.model.enums.DateTypeEnum;
import com.aizuda.easy.retry.server.web.model.response.ActivePodQuantityResponseVO;
import com.aizuda.easy.retry.server.web.model.response.DispatchQuantityResponseVO;
import com.aizuda.easy.retry.server.web.model.response.SceneQuantityRankResponseVO;
import com.aizuda.easy.retry.server.web.model.response.TaskQuantityResponseVO;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
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
public class DashBoardServiceImpl implements DashBoardService {
    public static final String URL = "http://{0}:{1}/dashboard/consumer/bucket";

    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;

    @Autowired
    private RetryTaskLogMessageMapper retryTaskLogMessageMapper;

    @Autowired
    private ServerNodeMapper serverNodeMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public TaskQuantityResponseVO countTask() {

        TaskQuantityResponseVO taskQuantityResponseVO = new TaskQuantityResponseVO();
        taskQuantityResponseVO.setTotal(retryTaskLogMapper.countTaskTotal());

        taskQuantityResponseVO.setFinish(retryTaskLogMapper.countTaskByRetryStatus(RetryStatusEnum.FINISH.getStatus()));
        taskQuantityResponseVO.setMaxRetryCount(retryTaskLogMapper.countTaskByRetryStatus(RetryStatusEnum.MAX_COUNT.getStatus()));
        taskQuantityResponseVO.setRunning(taskQuantityResponseVO.getTotal() - taskQuantityResponseVO.getFinish() - taskQuantityResponseVO.getMaxRetryCount());

        return taskQuantityResponseVO;
    }

    @Override
    public DispatchQuantityResponseVO countDispatch() {
        DispatchQuantityResponseVO dispatchQuantityResponseVO = new DispatchQuantityResponseVO();

        // 任务的总调度量
        Long total = retryTaskLogMessageMapper.selectCount(null);
        dispatchQuantityResponseVO.setTotal(total);

        if (total == 0) {
            return dispatchQuantityResponseVO;
        }

        Long success = retryTaskLogMapper.selectCount(new LambdaQueryWrapper<RetryTaskLog>()
                .eq(RetryTaskLog::getRetryStatus, RetryStatusEnum.FINISH.getStatus()));
        dispatchQuantityResponseVO.setSuccessPercent(BigDecimal.valueOf(success).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));

        return dispatchQuantityResponseVO;
    }

    @Override
    public ActivePodQuantityResponseVO countActivePod() {

        ActivePodQuantityResponseVO activePodQuantityResponseVO = new ActivePodQuantityResponseVO();

        List<ActivePodQuantityResponseDO> activePodQuantityDO = serverNodeMapper.countActivePod(UserSessionUtils.currentUserSession().getNamespaceId());
        Map<Integer, Long> map = activePodQuantityDO.stream().collect(Collectors.toMap(ActivePodQuantityResponseDO::getNodeType, ActivePodQuantityResponseDO::getTotal));
        Long clientTotal = map.getOrDefault(NodeTypeEnum.CLIENT.getType(), 0L);
        Long serverTotal = map.getOrDefault(NodeTypeEnum.SERVER.getType(), 0L);
        activePodQuantityResponseVO.setServerTotal(serverTotal);
        activePodQuantityResponseVO.setClientTotal(clientTotal);

        activePodQuantityResponseVO.setTotal(clientTotal + serverTotal);

        return activePodQuantityResponseVO;
    }

    @Override
    public List<SceneQuantityRankResponseVO> rankSceneQuantity(String groupName, String type, String startTime, String endTime) {
        LocalDateTime startDateTime = LocalDateTime.now();
        if (StrUtil.isNotBlank(startTime)) {
            startDateTime = LocalDateTime.of(LocalDate.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalTime.MIN);
        }

        LocalDateTime endDateTime = LocalDateTime.now();
        if (StrUtil.isNotBlank(endTime)) {
            endDateTime = LocalDateTime.of(LocalDate.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalTime.MAX);
        }

        DateTypeEnum dateTypeEnum = DateTypeEnum.valueOf(type.toUpperCase());
        startDateTime = dateTypeEnum.getStartTime().apply(startDateTime);
        endDateTime = dateTypeEnum.getEndTime().apply(endDateTime);

        List<SceneQuantityRankResponseDO> dispatchQuantityResponseDOS = retryTaskLogMapper.rankSceneQuantity(groupName, startDateTime, endDateTime);
        return SceneQuantityRankResponseVOConverter.INSTANCE.toSceneQuantityRankResponseVO(dispatchQuantityResponseDOS);
    }

    @Override
    public List<DispatchQuantityResponseVO> lineDispatchQuantity(String groupName, String type, String startTime, String endTime) {

        DateTypeEnum dateTypeEnum = DateTypeEnum.valueOf(type.toUpperCase());

        LocalDateTime startDateTime = LocalDateTime.now();
        if (StrUtil.isNotBlank(startTime)) {
            startDateTime = LocalDateTime.of(LocalDate.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalTime.MIN);
        }

        LocalDateTime endDateTime = LocalDateTime.now();
        if (StrUtil.isNotBlank(endTime)) {
            endDateTime = LocalDateTime.of(LocalDate.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalTime.MAX);
        }

        startDateTime = dateTypeEnum.getStartTime().apply(startDateTime);
        endDateTime = dateTypeEnum.getEndTime().apply(endDateTime);

        List<DispatchQuantityResponseDO> totalDispatchQuantityResponseList = retryTaskLogMapper.lineDispatchQuantity(groupName, null, type, startDateTime, endDateTime);

        List<DispatchQuantityResponseDO> successDispatchQuantityResponseList = retryTaskLogMapper.lineDispatchQuantity(groupName, RetryStatusEnum.FINISH.getStatus(), type, startDateTime, endDateTime);
        Map<String, DispatchQuantityResponseDO> successDispatchQuantityResponseVOMap = successDispatchQuantityResponseList.stream().collect(Collectors.toMap(DispatchQuantityResponseDO::getCreateDt, i -> i));
        for (DispatchQuantityResponseDO dispatchQuantityResponseDO : totalDispatchQuantityResponseList) {

            DispatchQuantityResponseDO quantityResponseVO = successDispatchQuantityResponseVOMap.get(dispatchQuantityResponseDO.getCreateDt());
            if (Objects.isNull(quantityResponseVO)) {
                dispatchQuantityResponseDO.setSuccess(0L);
            } else {
                dispatchQuantityResponseDO.setSuccess(quantityResponseVO.getTotal());
            }

            dispatchQuantityResponseDO.setFail(dispatchQuantityResponseDO.getTotal() - dispatchQuantityResponseDO.getSuccess());

        }

        List<DispatchQuantityResponseVO> dispatchQuantityResponse = DispatchQuantityResponseVOConverter.INSTANCE.toDispatchQuantityResponse(totalDispatchQuantityResponseList);

        dateTypeEnum.getConsumer().accept(dispatchQuantityResponse);

        return dispatchQuantityResponse.stream().sorted(Comparator.comparing(DispatchQuantityResponseVO::getCreateDt)).collect(Collectors.toList());
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

            ServerNodeExtAttrs serverNodeExtAttrs = JsonUtil
                    .parseObject(serverNodeResponseVO.getExtAttrs(), ServerNodeExtAttrs.class);

            try {

                // 从远程节点取
                String format = MessageFormat
                        .format(URL, serverNodeResponseVO.getHostIp(), serverNodeExtAttrs.getWebPort().toString());
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
