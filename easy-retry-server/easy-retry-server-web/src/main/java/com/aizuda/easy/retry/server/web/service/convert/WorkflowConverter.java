package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowBatchResponseVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.WorkflowBatchResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.po.Workflow;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowNode;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowTaskBatch;
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

    List<WorkflowResponseVO> toWorkflowResponseVO(List<Workflow> workflowList);

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
}
