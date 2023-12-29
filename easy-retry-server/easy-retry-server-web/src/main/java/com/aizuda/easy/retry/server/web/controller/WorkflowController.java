package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import com.aizuda.easy.retry.server.web.annotation.RoleEnum;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.WorkflowQueryVO;
import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowResponseVO;
import com.aizuda.easy.retry.server.web.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Role;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @author xiaowoniu
 * @date 2023-12-12 21:50:46
 * @since 2.6.0
 */
@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    @LoginRequired(role = RoleEnum.USER)
    public Boolean saveWorkflow(@RequestBody @Validated WorkflowRequestVO workflowRequestVO) {
        return workflowService.saveWorkflow(workflowRequestVO);
    }

    @GetMapping("/page/list")
    @LoginRequired(role = RoleEnum.USER)
    public PageResult<List<WorkflowResponseVO>> listPage(WorkflowQueryVO queryVO) {
        return workflowService.listPage(queryVO);
    }

    @PutMapping
    @LoginRequired(role = RoleEnum.USER)
    public Boolean updateWorkflow(@RequestBody @Validated WorkflowRequestVO workflowRequestVO) {
        return workflowService.updateWorkflow(workflowRequestVO);
    }

    @GetMapping("{id}")
    @LoginRequired(role = RoleEnum.USER)
    public WorkflowDetailResponseVO getWorkflowDetail(@PathVariable("id") Long id) throws IOException {
        return workflowService.getWorkflowDetail(id);
    }

    @PutMapping("/update/status/{id}")
    public Boolean updateStatus(@PathVariable("id") Long id) {
        return workflowService.updateStatus(id);
    }

    @DeleteMapping("/{id}")
    public Boolean deleteById(@PathVariable("id") Long id) {
        return workflowService.deleteById(id);
    }

    @PostMapping("/start")
    public void startWorkflow() {

    }

    @PostMapping("/stop")
    public void stopWorkflow() {

    }

    @PostMapping("/pause")
    public void pauseWorkflow() {

    }

    @PostMapping("/resume")
    public void resumeWorkflow() {

    }


}
