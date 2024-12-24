package com.aizuda.snailjob.server.common.alarm;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.AlarmInfoConverter;
import com.aizuda.snailjob.server.common.dto.JobAlarmInfo;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
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
public abstract class AbstractJobAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, JobAlarmInfo> {

    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;

    @Override
    protected Map<Set<Long>, List<JobAlarmInfo>> convertAlarmDTO(List<JobAlarmInfo> jobAlarmInfoList, Set<Integer> notifyScene) {

        Map<Set<Long>, List<JobAlarmInfo>> jobAlarmInfoMap = new HashMap<>();
        jobAlarmInfoList.stream().forEach(i -> notifyScene.add(i.getNotifyScene()));

        Map<Long, JobAlarmInfo> jobAlarmInfoGroupMap = jobAlarmInfoList.stream().collect(Collectors.toMap(i -> i.getId(), Function.identity()));

        // 查询数据库
        QueryWrapper<JobTaskBatch> wrapper = new QueryWrapper<JobTaskBatch>()
                .in("batch.id", jobAlarmInfoList.stream().map(i -> i.getId()).collect(Collectors.toSet()))
                .eq("batch.deleted", 0);

        List<JobBatchResponseDO> jobBatchResponseDOList = jobTaskBatchMapper.selectJobBatchListByIds(wrapper);
        for (JobBatchResponseDO jobBatchResponseDO : jobBatchResponseDOList) {
            Set<Long> jobNotifyIds = StrUtil.isBlank(jobBatchResponseDO.getNotifyIds()) ? new HashSet<>() : new HashSet<>(JsonUtil.parseList(jobBatchResponseDO.getNotifyIds(), Long.class));
            for (Long jobNotifyId : jobNotifyIds) {

                JobAlarmInfo jobAlarmInfo = AlarmInfoConverter.INSTANCE.toJobAlarmInfo(jobBatchResponseDO);
                JobAlarmInfo alarmInfo = jobAlarmInfoGroupMap.get(jobBatchResponseDO.getId());
                jobAlarmInfo.setReason(alarmInfo.getReason());
                jobAlarmInfoMap.put(Collections.singleton(jobNotifyId), Lists.newArrayList(jobAlarmInfo));
            }
        }

        return jobAlarmInfoMap;
    }
}
