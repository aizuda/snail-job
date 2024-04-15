package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.snail.job.common.core.util.JsonUtil;
import com.aizuda.snail.job.server.common.dto.CallbackConfig;
import com.aizuda.snail.job.server.common.dto.DecisionConfig;
import com.aizuda.snail.job.server.common.dto.JobTaskConfig;
import com.aizuda.snail.job.server.common.util.DateUtils;
import com.aizuda.snail.job.server.web.model.request.WorkflowRequestVO;
import com.aizuda.snail.job.server.web.model.response.WorkflowBatchResponseVO;
import com.aizuda.snail.job.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.snail.job.server.web.model.response.WorkflowResponseVO;
import com.aizuda.snail.job.template.datasource.persistence.dataobject.WorkflowBatchResponseDO;
import com.aizuda.snail.job.template.datasource.persistence.po.Workflow;
import com.aizuda.snail.job.template.datasource.persistence.po.WorkflowNode;
import com.aizuda.snail.job.template.datasource.persistence.po.WorkflowTaskBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author: xiaowoniu
 * @date : 2023-12-13
 * @since : 2.5.0
 */
@Mapper
public interface WorkflowConverter {

    WorkflowConverter INSTANCE = Mappers.getMapper(WorkflowConverter.class);

    Workflow toWorkflow(WorkflowRequestVO workflowRequestVO);

    WorkflowNode toWorkflowNode(WorkflowRequestVO.NodeInfo nodeInfo);

    WorkflowDetailResponseVO toWorkflowDetailResponseVO(Workflow workflow);

    List<WorkflowDetailResponseVO.NodeInfo> toNodeInfo(List<WorkflowNode> workflowNodes);

    @Mappings({
            @Mapping(target = "decision", expression = "java(WorkflowConverter.parseDecisionConfig(workflowNode))"),
            @Mapping(target = "callback", expression = "java(WorkflowConverter.parseCallbackConfig(workflowNode))"),
            @Mapping(target = "jobTask", expression = "java(WorkflowConverter.parseJobTaskConfig(workflowNode))")
    })
    WorkflowDetailResponseVO.NodeInfo toNodeInfo(WorkflowNode workflowNode);

    List<WorkflowResponseVO> toWorkflowResponseVO(List<Workflow> workflowList);

    @Mappings({
        @Mapping(target = "nextTriggerAt", expression = "java(WorkflowConverter.toLocalDateTime(workflow.getNextTriggerAt()))")
    })
    WorkflowResponseVO toWorkflowResponseVO(Workflow workflow);

    List<WorkflowBatchResponseVO> toWorkflowBatchResponseVO(List<WorkflowBatchResponseDO> workflowBatchResponseList);

    @Mappings({
            @Mapping(source = "workflowTaskBatch.groupName", target = "groupName"),
            @Mapping(source = "workflowTaskBatch.id", target = "id"),
            @Mapping(source = "workflowTaskBatch.createDt", target = "createDt"),
            @Mapping(target = "executionAt", expression = "java(WorkflowConverter.toLocalDateTime(workflowTaskBatch.getExecutionAt()))")
    })
    WorkflowBatchResponseVO toWorkflowBatchResponseVO(WorkflowTaskBatch workflowTaskBatch, Workflow workflow);

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


}
