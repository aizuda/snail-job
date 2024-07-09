package com.aizuda.snailjob.server.web.controller;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.common.core.annotation.OriginalControllerReturnValue;
import com.aizuda.snailjob.server.common.dto.DecisionConfig;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.CheckDecisionVO;
import com.aizuda.snailjob.server.web.model.request.ExportWorkflowVO;
import com.aizuda.snailjob.server.web.model.request.WorkflowQueryVO;
import com.aizuda.snailjob.server.web.model.request.WorkflowRequestVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowResponseVO;
import com.aizuda.snailjob.server.web.service.WorkflowService;
import com.aizuda.snailjob.server.web.util.ExportUtils;
import com.aizuda.snailjob.server.web.util.ImportUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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
    @LoginRequired(role = RoleEnum.USER)
    public Boolean updateStatus(@PathVariable("id") Long id) {
        return workflowService.updateStatus(id);
    }

    @DeleteMapping("/ids")
    @LoginRequired(role = RoleEnum.USER)
    public Boolean deleteByIds(@RequestBody @Valid @NotEmpty(message = "ids不能为空") Set<Long> ids) {
        return workflowService.deleteByIds(ids);
    }

    @PostMapping("/trigger/{id}")
    @LoginRequired(role = RoleEnum.USER)
    public Boolean trigger(@PathVariable("id") Long id) {
        return workflowService.trigger(id);
    }

    @GetMapping("/workflow-name/list")
    @LoginRequired(role = RoleEnum.USER)
    public List<WorkflowResponseVO> getWorkflowNameList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "workflowId", required = false) Long workflowId,
            @RequestParam(value = "groupName", required = false) String groupName) {
        return workflowService.getWorkflowNameList(keywords, workflowId, groupName);
    }

    @PostMapping("/check-node-expression")
    @LoginRequired(role = RoleEnum.USER)
    public Pair<Integer, String> checkNodeExpression(@RequestBody @Validated CheckDecisionVO checkDecisionVO) {
        return workflowService.checkNodeExpression(checkDecisionVO);
    }

    @LoginRequired
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importScene(@RequestPart("file") MultipartFile file) throws IOException {
        // 写入数据
        workflowService.importWorkflowTask(ImportUtils.parseList(file, WorkflowRequestVO.class));
    }

    @LoginRequired
    @PostMapping("/export")
    @OriginalControllerReturnValue
    public ResponseEntity<String> export(@RequestBody ExportWorkflowVO exportWorkflowVO) {
        return ExportUtils.doExport(workflowService.exportWorkflowTask(exportWorkflowVO));
    }

}
