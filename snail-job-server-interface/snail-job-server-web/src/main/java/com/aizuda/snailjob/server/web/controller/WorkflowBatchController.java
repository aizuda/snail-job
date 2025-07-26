package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.common.vo.WorkflowBatchResponseVO;
import com.aizuda.snailjob.server.service.service.WorkflowBatchService;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.WorkflowBatchQueryVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowDetailResponseWebVO;
import com.aizuda.snailjob.server.web.service.WorkflowWebBatchService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author xiaowoniu
 * @date 2023-12-23 17:47:51
 * @since 2.6.0
 */
@RestController
@RequestMapping("/workflow/batch")
@RequiredArgsConstructor
public class WorkflowBatchController {
    private final WorkflowWebBatchService workflowWebBatchService;
    @Qualifier("WorkflowWebBatchCommonService")
    private final WorkflowBatchService workflowBatchService;

    @LoginRequired
    @GetMapping("/page/list")
    public PageResult<List<WorkflowBatchResponseVO>> listPage(WorkflowBatchQueryVO queryVO) {
        return workflowWebBatchService.listPage(queryVO);
    }

    @LoginRequired
    @GetMapping("{id}")
    public WorkflowDetailResponseWebVO getWorkflowBatchDetail(@PathVariable("id") Long id) {
        return workflowBatchService.getWorkflowBatchById(id, WorkflowDetailResponseWebVO.class);
    }

    @PostMapping("/stop/{id}")
    @LoginRequired
    public Boolean stop(@PathVariable("id") Long id) {
        return workflowWebBatchService.stop(id);
    }

    @DeleteMapping("/ids")
    @LoginRequired(role = RoleEnum.USER)
    public Boolean deleteByIds(@RequestBody
                               @NotEmpty(message = "ids cannot be null")
                               @Size(max = 100, message = "Maximum {max} deletions")
                               Set<Long> ids) {
        return workflowWebBatchService.deleteByIds(ids);
    }
}
