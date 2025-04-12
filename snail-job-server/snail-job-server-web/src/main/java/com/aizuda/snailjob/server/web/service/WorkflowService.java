package com.aizuda.snailjob.server.web.service;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.server.common.vo.request.WorkflowRequestVO;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.server.common.vo.WorkflowDetailResponseVO;
import com.aizuda.snailjob.server.common.vo.WorkflowResponseVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author xiaowoniu
 * @date 2023-12-12 21:53:59
 * @since 2.6.0
 */
public interface WorkflowService {

    boolean saveWorkflow(WorkflowRequestVO workflowRequestVO);

    WorkflowDetailResponseVO getWorkflowDetail(Long id) throws IOException;

    PageResult<List<WorkflowResponseVO>> listPage(WorkflowQueryVO queryVO);

    Boolean updateWorkflow(WorkflowRequestVO workflowRequestVO);

    Boolean updateStatus(Long id);

    Boolean trigger(WorkflowTriggerVO triggerVO);

    List<WorkflowResponseVO> getWorkflowNameList(String keywords, Long workflowId, String groupName);

    Pair<Integer, Object> checkNodeExpression(CheckDecisionVO decisionVO);

    void importWorkflowTask(@Valid @NotEmpty(message = "Import data cannot be empty") List<WorkflowRequestVO> requests);

    String exportWorkflowTask(ExportWorkflowVO exportWorkflowVO
    );

    Boolean deleteByIds(Set<Long> ids);
}
