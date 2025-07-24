package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.WorkflowBatchQueryVO;
import com.aizuda.snailjob.server.common.vo.WorkflowBatchResponseVO;
import com.aizuda.snailjob.server.common.vo.WorkflowDetailResponseVO;

import java.util.List;
import java.util.Set;

/**
 * @author xiaowoniu
 * @date 2023-12-23 17:48:23
 * @since 2.6.0
 */
public interface WorkflowWebBatchService {

    PageResult<List<WorkflowBatchResponseVO>> listPage(WorkflowBatchQueryVO queryVO);

    Boolean stop(Long id);

    Boolean deleteByIds(Set<Long> ids);
}
