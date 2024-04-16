package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.WorkflowBatchQueryVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowBatchResponseVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.WorkflowBatchQueryVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowBatchResponseVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowDetailResponseVO;

import java.util.List;

/**
 * @author xiaowoniu
 * @date 2023-12-23 17:48:23
 * @since 2.6.0
 */
public interface WorkflowBatchService {

    PageResult<List<WorkflowBatchResponseVO>> listPage(WorkflowBatchQueryVO queryVO);

    WorkflowDetailResponseVO getWorkflowBatchDetail(Long id);

    Boolean stop(Long id);
}
