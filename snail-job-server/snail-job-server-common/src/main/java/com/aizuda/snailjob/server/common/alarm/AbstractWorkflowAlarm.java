package com.aizuda.snailjob.server.common.alarm;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.dto.WorkflowAlarmInfo;
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
public abstract class AbstractWorkflowAlarm<E extends ApplicationEvent> extends AbstractAlarm<E, WorkflowAlarmInfo> {

    @Override
    protected Map<Set<Long>, List<WorkflowAlarmInfo>> convertAlarmDTO(List<WorkflowAlarmInfo> workflowAlarmInfoList, Set<Integer> notifyScene, Set<Long> notifyIds) {

        return StreamUtils.groupByKey(workflowAlarmInfoList, workflowAlarmInfo -> {

            Set<Long> workflowNotifyIds =  StrUtil.isBlank(workflowAlarmInfo.getNotifyIds()) ? new HashSet<>() : new HashSet<>(JsonUtil.parseList(workflowAlarmInfo.getNotifyIds(), Long.class));

            notifyScene.add(workflowAlarmInfo.getNotifyScene());
            notifyIds.addAll(workflowNotifyIds);
            return workflowNotifyIds;
        });
    }
}
