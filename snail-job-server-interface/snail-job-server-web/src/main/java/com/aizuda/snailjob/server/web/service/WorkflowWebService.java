package com.aizuda.snailjob.server.web.service;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.server.web.model.request.WorkflowRequestVO;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.server.web.model.response.WorkflowResponseVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowDetailResponseWebVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * @author xiaowoniu
 * @date 2023-12-12 21:53:59
 * @since 2.6.0
 */
public interface WorkflowWebService {

    boolean saveWorkflow(WorkflowRequestVO workflowRequestVO);

    WorkflowDetailResponseWebVO getWorkflowDetail(Long id);

    PageResult<List<WorkflowResponseVO>> listPage(WorkflowQueryVO queryVO);

    Boolean updateWorkflow(WorkflowRequestVO workflowRequestVO);

    List<WorkflowResponseVO> getWorkflowNameList(String keywords, Long workflowId, String groupName);

    Pair<Integer, Object> checkNodeExpression(CheckDecisionVO decisionVO);

    void importWorkflowTask(@Valid @NotEmpty(message = "Import data cannot be empty") List<WorkflowRequestVO> requests);

    String exportWorkflowTask(ExportWorkflowVO exportWorkflowVO);

}
