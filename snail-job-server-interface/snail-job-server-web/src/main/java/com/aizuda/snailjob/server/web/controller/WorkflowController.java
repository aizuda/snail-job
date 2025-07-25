package com.aizuda.snailjob.server.web.controller;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.common.core.annotation.OriginalControllerReturnValue;
import com.aizuda.snailjob.server.common.vo.request.WorkflowRequestVO;
import com.aizuda.snailjob.server.service.dto.JobTriggerDTO;
import com.aizuda.snailjob.server.service.service.WorkflowService;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.server.common.vo.WorkflowDetailResponseVO;
import com.aizuda.snailjob.server.common.vo.WorkflowResponseVO;
import com.aizuda.snailjob.server.web.model.response.WorkflowDetailResponseWebVO;
import com.aizuda.snailjob.server.web.service.WorkflowWebService;
import com.aizuda.snailjob.server.web.util.ExportUtils;
import com.aizuda.snailjob.server.web.util.ImportUtils;
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

    private final WorkflowWebService workflowWebService;
    private final WorkflowService workflowService;

    @PostMapping
    @LoginRequired(role = RoleEnum.USER)
    public Boolean saveWorkflow(@RequestBody @Validated WorkflowRequestVO workflowRequestVO) {
        return workflowWebService.saveWorkflow(workflowRequestVO);
    }

    @GetMapping("/page/list")
    @LoginRequired(role = RoleEnum.USER)
    public PageResult<List<WorkflowResponseVO>> listPage(WorkflowQueryVO queryVO) {
        return workflowWebService.listPage(queryVO);
    }

    @PutMapping
    @LoginRequired(role = RoleEnum.USER)
    public Boolean updateWorkflow(@RequestBody @Validated WorkflowRequestVO workflowRequestVO) {
        return workflowWebService.updateWorkflow(workflowRequestVO);
    }

    @GetMapping("{id}")
    @LoginRequired(role = RoleEnum.USER)
    public WorkflowDetailResponseWebVO getWorkflowDetail(@PathVariable("id") Long id) throws IOException {
        return workflowWebService.getWorkflowDetail(id);
    }

    @PutMapping("/update/status")
    @LoginRequired(role = RoleEnum.USER)
    public Boolean updateStatus(@RequestBody @Validated StatusUpdateRequestWebVO requestVO) {
        return workflowService.updateWorkFlowStatus(requestVO);
    }

    @DeleteMapping("/ids")
    @LoginRequired(role = RoleEnum.USER)
    public Boolean deleteByIds(@RequestBody @NotEmpty(message = "ids cannot be null") Set<Long> ids) {
        return workflowService.deleteWorkflowByIds(ids);
    }

    @PostMapping("/trigger")
    @LoginRequired(role = RoleEnum.USER)
    public Boolean trigger(@RequestBody @Validated WorkflowTriggerVO triggerVO) {
        JobTriggerDTO triggerBaseDTO = new JobTriggerDTO();
        triggerBaseDTO.setTmpArgsStr(triggerVO.getTmpWfContext());
        triggerBaseDTO.setJobId(triggerVO.getWorkflowId());
        return workflowService.triggerWorkFlow(triggerBaseDTO);
    }

    @GetMapping("/workflow-name/list")
    @LoginRequired(role = RoleEnum.USER)
    public List<WorkflowResponseVO> getWorkflowNameList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "workflowId", required = false) Long workflowId,
            @RequestParam(value = "groupName", required = false) String groupName) {
        return workflowWebService.getWorkflowNameList(keywords, workflowId, groupName);
    }

    @PostMapping("/check-node-expression")
    @LoginRequired(role = RoleEnum.USER)
    public Pair<Integer, Object> checkNodeExpression(@RequestBody @Validated CheckDecisionVO checkDecisionVO) {
        return workflowWebService.checkNodeExpression(checkDecisionVO);
    }

    @LoginRequired
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importScene(@RequestPart("file") MultipartFile file) throws IOException {
        // 写入数据
        workflowWebService.importWorkflowTask(ImportUtils.parseList(file, WorkflowRequestVO.class));
    }

    @LoginRequired
    @PostMapping("/export")
    @OriginalControllerReturnValue
    public ResponseEntity<String> export(@RequestBody ExportWorkflowVO exportWorkflowVO) {
        return ExportUtils.doExport(workflowWebService.exportWorkflowTask(exportWorkflowVO));
    }

}
