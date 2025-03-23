package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.WorkflowBatchQueryVO;
import com.aizuda.snailjob.server.common.vo.WorkflowBatchResponseVO;
import com.aizuda.snailjob.server.common.vo.WorkflowDetailResponseVO;
import com.aizuda.snailjob.server.web.service.WorkflowBatchService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
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
    private final WorkflowBatchService workflowBatchService;

    @LoginRequired
    @GetMapping("/page/list")
    public PageResult<List<WorkflowBatchResponseVO>> listPage(WorkflowBatchQueryVO queryVO) {
        return workflowBatchService.listPage(queryVO);
    }

    @LoginRequired
    @GetMapping("{id}")
    public WorkflowDetailResponseVO getWorkflowBatchDetail(@PathVariable("id") Long id) {
        return workflowBatchService.getWorkflowBatchDetail(id);
    }

    @PostMapping("/stop/{id}")
    @LoginRequired
    public Boolean stop(@PathVariable("id") Long id) {
        return workflowBatchService.stop(id);
    }

    @DeleteMapping("/ids")
    @LoginRequired(role = RoleEnum.USER)
    public Boolean deleteByIds(@RequestBody
                               @NotEmpty(message = "ids不能为空")
                               @Size(max = 100, message = "最多删除 {max} 个")
                               Set<Long> ids) {
        return workflowBatchService.deleteByIds(ids);
    }
}
