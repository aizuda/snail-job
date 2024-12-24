package com.aizuda.snailjob.server.common.alarm;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.AlarmInfoConverter;
import com.aizuda.snailjob.server.common.dto.WorkflowAlarmInfo;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.WorkflowBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:19:19
 * @since 2.5.0
 */
public abstract class AbstractWorkflowAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, WorkflowAlarmInfo> {

    @Autowired
    private WorkflowTaskBatchMapper workflowTaskBatchMapper;

    @Override
    protected Map<Long, List<WorkflowAlarmInfo>> convertAlarmDTO(List<WorkflowAlarmInfo> workflowAlarmInfoList, Set<Integer> notifyScene) {

        Map<Long, List<WorkflowAlarmInfo>> workflowAlarmInfoMap = new HashMap<>();
        workflowAlarmInfoList.forEach(i -> notifyScene.add(i.getNotifyScene()));

        Map<Long, WorkflowAlarmInfo> workflowAlarmInfoGroupMap = workflowAlarmInfoList.stream().collect(Collectors.toMap(i -> i.getId(), Function.identity()));

        List<WorkflowBatchResponseDO> workflowBatchResponseDOList = workflowTaskBatchMapper.selectWorkflowBatchList(
                new QueryWrapper<WorkflowTaskBatch>()
                        .in("batch.id", workflowAlarmInfoList.stream().map(i -> i.getId()).collect(Collectors.toSet()))
                        .eq("batch.deleted", 0));

        for (WorkflowBatchResponseDO workflowBatchResponseDO : workflowBatchResponseDOList) {
            Set<Long> workflowNotifyIds = StrUtil.isBlank(workflowBatchResponseDO.getNotifyIds()) ? new HashSet<>() : new HashSet<>(JsonUtil.parseList(workflowBatchResponseDO.getNotifyIds(), Long.class));
            for (Long workflowNotifyId : workflowNotifyIds) {

                WorkflowAlarmInfo workflowAlarmInfo = AlarmInfoConverter.INSTANCE.toWorkflowAlarmInfo(workflowBatchResponseDO);
                WorkflowAlarmInfo alarmInfo = workflowAlarmInfoGroupMap.get(workflowAlarmInfo.getId());
                workflowAlarmInfo.setReason(alarmInfo.getReason());

                List<WorkflowAlarmInfo> workflowAlarmInfos = workflowAlarmInfoMap.getOrDefault(workflowNotifyId, Lists.newArrayList());
                workflowAlarmInfos.add(workflowAlarmInfo);
                workflowAlarmInfoMap.put(workflowNotifyId ,workflowAlarmInfos);
            }
        }

        return workflowAlarmInfoMap;
    }
}
