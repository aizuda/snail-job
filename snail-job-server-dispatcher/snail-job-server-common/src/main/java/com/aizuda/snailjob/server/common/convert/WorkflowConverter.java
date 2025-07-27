package com.aizuda.snailjob.server.common.convert;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.model.request.CallbackConfig;
import com.aizuda.snailjob.model.request.DecisionConfig;
import com.aizuda.snailjob.model.request.JobTaskConfig;
import com.aizuda.snailjob.server.common.dto.PointInTimeDTO;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.util.TriggerIntervalUtils;
import com.aizuda.snailjob.server.common.vo.WorkflowBatchResponseVO;
import com.aizuda.snailjob.server.common.vo.WorkflowDetailResponseVO;
import com.aizuda.snailjob.server.common.vo.WorkflowResponseVO;
import com.aizuda.snailjob.server.common.vo.request.WorkflowRequestVO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.WorkflowBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.po.Workflow;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowNode;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
@Deprecated
public interface WorkflowConverter {

    WorkflowConverter INSTANCE = Mappers.getMapper(WorkflowConverter.class);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(WorkflowConverter.toNotifyIdsStr(workflowRequestVO.getNotifyIds()))"),
            @Mapping(target = "triggerInterval", expression = "java(WorkflowConverter.toTriggerInterval(workflowRequestVO))")
    })
    Workflow convert(WorkflowRequestVO workflowRequestVO);

    WorkflowNode convert(WorkflowRequestVO.NodeInfo nodeInfo);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(WorkflowConverter.toNotifyIds(workflow.getNotifyIds()))"),
            @Mapping(target = "triggerInterval", expression = "java(WorkflowConverter.toTriggerInterval(workflow))"),
            @Mapping(target = "ownerId", expression = "java(WorkflowConverter.getOwnerId(workflow))")
    })
    WorkflowDetailResponseVO convert(Workflow workflow);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(WorkflowConverter.toNotifyIds(workflow.getNotifyIds()))"),
            @Mapping(target = "triggerInterval", expression = "java(WorkflowConverter.toTriggerInterval(workflow))"),
    })
    WorkflowRequestVO convertToWorkflowRequestVo(Workflow workflow);

    List<WorkflowDetailResponseVO.NodeInfo> convertList(List<WorkflowNode> workflowNodes);

    @Mappings({
            @Mapping(target = "decision", expression = "java(WorkflowConverter.parseDecisionConfig(workflowNode))"),
            @Mapping(target = "callback", expression = "java(WorkflowConverter.parseCallbackConfig(workflowNode))"),
            @Mapping(target = "jobTask", expression = "java(WorkflowConverter.parseJobTaskConfig(workflowNode))")
    })
    WorkflowDetailResponseVO.NodeInfo convert(WorkflowNode workflowNode);

    List<WorkflowResponseVO> convertListToWorkflowList(List<Workflow> workflowList);

    @Mappings({
            @Mapping(target = "nextTriggerAt", expression = "java(WorkflowConverter.toLocalDateTime(workflow.getNextTriggerAt()))"),
            @Mapping(target = "notifyIds", expression = "java(WorkflowConverter.toNotifyIds(workflow.getNotifyIds()))"),
            @Mapping(target = "triggerInterval", expression = "java(WorkflowConverter.toTriggerInterval(workflow))")
    })
    WorkflowResponseVO convertToWorkflow(Workflow workflow);

    List<WorkflowBatchResponseVO> convertListToWorkflowBatchList(List<WorkflowBatchResponseDO> workflowBatchResponseList);

    @Mappings({
            @Mapping(source = "workflowTaskBatch.groupName", target = "groupName"),
            @Mapping(source = "workflowTaskBatch.id", target = "id"),
            @Mapping(source = "workflowTaskBatch.createDt", target = "createDt"),
            @Mapping(target = "executionAt", expression = "java(WorkflowConverter.toLocalDateTime(workflowTaskBatch.getExecutionAt()))")
    })
    WorkflowBatchResponseVO convert(WorkflowTaskBatch workflowTaskBatch, Workflow workflow);

    static Long getOwnerId(Workflow workflow) {
        return Objects.nonNull(workflow.getOwnerId()) && workflow.getOwnerId() > 0 ? workflow.getOwnerId() : null;
    }

    static LocalDateTime toLocalDateTime(Long nextTriggerAt) {
        if (Objects.isNull(nextTriggerAt) || nextTriggerAt == 0) {
            return null;
        }

        return DateUtils.toLocalDateTime(nextTriggerAt);
    }

    static DecisionConfig parseDecisionConfig(WorkflowNode workflowNode) {
        if (WorkflowNodeTypeEnum.DECISION.getType() == workflowNode.getNodeType()) {
            return JsonUtil.parseObject(workflowNode.getNodeInfo(), DecisionConfig.class);
        }

        return null;
    }

    static CallbackConfig parseCallbackConfig(WorkflowNode workflowNode) {
        if (WorkflowNodeTypeEnum.CALLBACK.getType() == workflowNode.getNodeType()) {
            return JsonUtil.parseObject(workflowNode.getNodeInfo(), CallbackConfig.class);
        }

        return null;
    }

    static JobTaskConfig parseJobTaskConfig(WorkflowNode workflowNode) {
        if (WorkflowNodeTypeEnum.JOB_TASK.getType() == workflowNode.getNodeType()) {
            JobTaskConfig jobTaskConfig = new JobTaskConfig();
            jobTaskConfig.setJobId(workflowNode.getJobId());
            return jobTaskConfig;
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

    static String toTriggerInterval(WorkflowRequestVO requestVO) {
        String triggerInterval = requestVO.getTriggerInterval();
        if (StrUtil.isBlank(triggerInterval) || Objects.isNull(requestVO.getTriggerType())) {
            return StrUtil.EMPTY;
        }
        return TriggerIntervalUtils.getPointInTimeStr(triggerInterval, requestVO.getTriggerType());
    }

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
