package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.po.Workflow;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowNode;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

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
}
