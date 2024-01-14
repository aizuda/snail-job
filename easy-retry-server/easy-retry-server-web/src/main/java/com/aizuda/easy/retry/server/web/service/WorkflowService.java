package com.aizuda.easy.retry.server.web.service;

import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.WorkflowQueryVO;
import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowResponseVO;

import java.io.IOException;
import java.util.List;

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

    Boolean deleteById(Long id);

    Boolean trigger(Long id);
}
