package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO;
import com.aizuda.easy.retry.server.web.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    @PostMapping
    public Boolean saveWorkflow(@RequestBody WorkflowRequestVO workflowRequestVO) {
        return workflowService.saveWorkflow(workflowRequestVO);
    }

    @PutMapping
    public void updateWorkflow() {

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

    public void getWorkflowDetail() {

    }

}
