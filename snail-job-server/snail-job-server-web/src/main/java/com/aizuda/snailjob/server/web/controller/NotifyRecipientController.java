package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.NotifyRecipientQueryVO;
import com.aizuda.snailjob.server.web.model.request.NotifyRecipientRequestVO;
import com.aizuda.snailjob.server.web.model.response.NotifyRecipientResponseVO;
import com.aizuda.snailjob.server.web.service.NotifyRecipientService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Boolean saveNotifyRecipient(@RequestBody @Validated NotifyRecipientRequestVO requestVO) {
        return notifyRecipientService.saveNotifyRecipient(requestVO);
    }

    @PutMapping
    public Boolean updateNotifyRecipient(@RequestBody @Validated NotifyRecipientRequestVO requestVO) {
        return notifyRecipientService.updateNotifyRecipient(requestVO);
    }

    @GetMapping("/page/list")
    public PageResult<List<NotifyRecipientResponseVO>> getNotifyRecipientList(NotifyRecipientQueryVO queryVO) {
        return notifyRecipientService.getNotifyRecipientList(queryVO);
    }
}
