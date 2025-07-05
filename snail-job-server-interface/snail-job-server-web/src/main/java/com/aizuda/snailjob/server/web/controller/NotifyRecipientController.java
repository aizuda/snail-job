package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.common.core.annotation.OriginalControllerReturnValue;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.ExportNotifyRecipientVO;
import com.aizuda.snailjob.server.web.model.request.NotifyRecipientQueryVO;
import com.aizuda.snailjob.server.web.model.request.NotifyRecipientRequestVO;
import com.aizuda.snailjob.server.web.model.response.CommonLabelValueResponseVO;
import com.aizuda.snailjob.server.web.model.response.NotifyRecipientResponseVO;
import com.aizuda.snailjob.server.web.service.NotifyRecipientService;
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
 * <p>
 * 告警通知接收人 前端控制器
 * </p>
 *
 * @author xiaowoniu
 * @since 2024-04-17
 */
@RestController
@RequestMapping("/notify-recipient")
@RequiredArgsConstructor
public class NotifyRecipientController {
    private final NotifyRecipientService notifyRecipientService;

    @PostMapping
    @LoginRequired
    public Boolean saveNotifyRecipient(@RequestBody @Validated NotifyRecipientRequestVO requestVO) {
        return notifyRecipientService.saveNotifyRecipient(requestVO);
    }

    @PutMapping
    @LoginRequired
    public Boolean updateNotifyRecipient(@RequestBody @Validated NotifyRecipientRequestVO requestVO) {
        return notifyRecipientService.updateNotifyRecipient(requestVO);
    }

    @GetMapping("/page/list")
    @LoginRequired
    public PageResult<List<NotifyRecipientResponseVO>> getNotifyRecipientPageList(NotifyRecipientQueryVO queryVO) {
        return notifyRecipientService.getNotifyRecipientPageList(queryVO);
    }

    @GetMapping("/list")
    @LoginRequired
    public List<CommonLabelValueResponseVO> getNotifyRecipientList() {
        return notifyRecipientService.getNotifyRecipientList();
    }

    @DeleteMapping("/ids")
    @LoginRequired
    public Boolean batchDeleteByIds(@RequestBody @NotEmpty(message = "ids cannot be null") Set<Long> ids) {
        return notifyRecipientService.batchDeleteByIds(ids);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LoginRequired(role = RoleEnum.ADMIN)
    public void importScene(@RequestPart("file") MultipartFile file) throws IOException {
        notifyRecipientService.importNotifyRecipient(ImportUtils.parseList(file, NotifyRecipientRequestVO.class));
    }

    @PostMapping("/export")
    @LoginRequired
    @OriginalControllerReturnValue
    public ResponseEntity<String> exportGroup(@RequestBody ExportNotifyRecipientVO exportNotifyRecipientVO) {
        return ExportUtils.doExport(notifyRecipientService.exportNotifyRecipient(exportNotifyRecipientVO));
    }
}
