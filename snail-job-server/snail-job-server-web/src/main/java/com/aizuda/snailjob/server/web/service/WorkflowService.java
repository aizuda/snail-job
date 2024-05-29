package com.aizuda.snailjob.server.web.service;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.server.common.dto.DecisionConfig;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.WorkflowQueryVO;
import com.aizuda.snailjob.server.web.model.request.WorkflowRequestVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowResponseVO;
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

    Boolean deleteById(Long id);

    Boolean trigger(Long id);

    List<WorkflowResponseVO> getWorkflowNameList(String keywords, Long workflowId);

    Pair<Integer, String> checkNodeExpression(DecisionConfig decisionConfig);

    void importWorkflowTask(@Valid @NotEmpty(message = "导入数据不能为空") List<WorkflowRequestVO> requests);

    String exportWorkflowTask(Set<Long> workflowIds);
}
