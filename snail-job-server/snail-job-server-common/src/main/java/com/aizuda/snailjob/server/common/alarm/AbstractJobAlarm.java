package com.aizuda.snailjob.server.common.alarm;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.dto.JobAlarmInfo;
import org.springframework.context.ApplicationEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:19:19
 * @since 2.5.0
 */
public abstract class AbstractJobAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, JobAlarmInfo> {

    @Override
    protected Map<Set<Long>, List<JobAlarmInfo>> convertAlarmDTO(List<JobAlarmInfo> jobAlarmInfoList, Set<Integer> notifyScene, Set<Long> notifyIds) {

        return StreamUtils.groupByKey(jobAlarmInfoList, jobAlarmInfo -> {

            Set<Long> jobNotifyIds =  StrUtil.isBlank(jobAlarmInfo.getNotifyIds()) ? new HashSet<>() : new HashSet<>(JsonUtil.parseList(jobAlarmInfo.getNotifyIds(), Long.class));

            notifyScene.add(jobAlarmInfo.getNotifyScene());
            notifyIds.addAll(jobNotifyIds);
            return jobNotifyIds;
        });
    }
}
