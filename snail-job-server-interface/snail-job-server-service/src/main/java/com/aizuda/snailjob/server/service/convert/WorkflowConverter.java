package com.aizuda.snailjob.server.service.convert;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.model.request.DecisionConfigRequest;
import com.aizuda.snailjob.model.request.JobTaskConfigRequest;
import com.aizuda.snailjob.server.common.dto.PointInTimeDTO;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.model.response.base.WorkflowDetailResponse;
import com.aizuda.snailjob.template.datasource.persistence.po.Workflow;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowNode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author: xiaowoniu
 * @date : 2023-12-13
 * @since : 2.5.0
 */
@Mapper
public interface WorkflowConverter {

    WorkflowConverter INSTANCE = Mappers.getMapper(WorkflowConverter.class);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(WorkflowConverter.toNotifyIds(workflow.getNotifyIds()))"),
            @Mapping(target = "triggerInterval", expression = "java(WorkflowConverter.toTriggerInterval(workflow))"),
            @Mapping(target = "ownerId", expression = "java(WorkflowConverter.getOwnerId(workflow))")
    })
    WorkflowDetailResponse convert(Workflow workflow);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(WorkflowConverter.toNotifyIds(workflow.getNotifyIds()))"),
            @Mapping(target = "triggerInterval", expression = "java(WorkflowConverter.toTriggerInterval(workflow))"),
            @Mapping(target = "ownerId", expression = "java(WorkflowConverter.getOwnerId(workflow))")
    })
    void fillCommonFields(Workflow workflow, @MappingTarget WorkflowDetailResponse target);

    List<WorkflowDetailResponse.NodeInfo> convertList(List<WorkflowNode> workflowNodes);

    @Mappings({
            @Mapping(target = "decision", expression = "java(WorkflowConverter.parseDecisionConfig(workflowNode))"),
            @Mapping(target = "jobTask", expression = "java(WorkflowConverter.parseJobTaskConfig(workflowNode))")
    })
    WorkflowDetailResponse.NodeInfo convert(WorkflowNode workflowNode);

    static Long getOwnerId(Workflow workflow) {
        return Objects.nonNull(workflow.getOwnerId()) && workflow.getOwnerId() > 0 ? workflow.getOwnerId() : null;
    }

    static LocalDateTime toLocalDateTime(Long nextTriggerAt) {
        return JobConverter.toLocalDateTime(nextTriggerAt);
    }

    static DecisionConfigRequest parseDecisionConfig(WorkflowNode workflowNode) {
        if (WorkflowNodeTypeEnum.DECISION.getType() == workflowNode.getNodeType()) {
            return JsonUtil.parseObject(workflowNode.getNodeInfo(), DecisionConfigRequest.class);
        }

        return null;
    }


    static JobTaskConfigRequest parseJobTaskConfig(WorkflowNode workflowNode) {
        if (WorkflowNodeTypeEnum.JOB_TASK.getType() == workflowNode.getNodeType()) {
            JobTaskConfigRequest jobTaskConfigRequest = new JobTaskConfigRequest();
            jobTaskConfigRequest.setJobId(workflowNode.getJobId());
            return jobTaskConfigRequest;
        }

        return null;
    }

    static Set<Long> toNotifyIds(String notifyIds) {
        if (StrUtil.isBlank(notifyIds)) {
            return new HashSet<>();
        }

        return new HashSet<>(JsonUtil.parseList(notifyIds, Long.class));
    }

    static String toNotifyIdsStr(Set<Long> notifyIds) {
        if (CollUtil.isEmpty(notifyIds)) {
            return StrUtil.EMPTY;
        }

        return JsonUtil.toJsonString(notifyIds);
    }

//    static String toTriggerInterval(WorkflowRequestVO requestVO) {
//        String triggerInterval = requestVO.getTriggerInterval();
//        if (StrUtil.isBlank(triggerInterval) || Objects.isNull(requestVO.getTriggerType())) {
//            return StrUtil.EMPTY;
//        }
//        return TriggerIntervalUtils.getPointInTimeStr(triggerInterval, requestVO.getTriggerType());
//    }

    static String toTriggerInterval(Workflow workflow) {
        String triggerInterval = workflow.getTriggerInterval();
        Integer triggerType = workflow.getTriggerType();

        if (WaitStrategies.WaitStrategyEnum.POINT_IN_TIME.getType().equals(triggerType)) {
            List<PointInTimeDTO> pointInTimeDTOS = JsonUtil.parseList(triggerInterval, PointInTimeDTO.class);
            List<String> timeStrList = pointInTimeDTOS
                    .stream()
                    .map(time -> DateUtils.format(DateUtils.toLocalDateTime(time.getTime())))
                    .toList();
            return JsonUtil.toJsonString(timeStrList);
        }

        return triggerInterval;

    }
}
