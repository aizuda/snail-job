package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.NotifyConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.NotifyConfigRequestVO;
import com.aizuda.snailjob.server.web.model.response.NotifyConfigResponseVO;
import com.aizuda.snailjob.server.web.service.NotifyConfigService;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 通知配置接口
 *
 * @author: opensnail
 * @date : 2022-03-03 11:32
 */
@RestController
@RequestMapping("/notify-config")
@RequiredArgsConstructor
public class NotifyConfigController {
    private final NotifyConfigService notifyConfigService;

    @LoginRequired
    @GetMapping("list")
    public PageResult<List<NotifyConfigResponseVO>> getNotifyConfigList(NotifyConfigQueryVO queryVO) {
        return notifyConfigService.getNotifyConfigList(queryVO);
    }

    @LoginRequired
    @GetMapping("/all/{systemTaskType}")
    public List<NotifyConfig> getNotifyConfigBySystemTaskTypeList(@PathVariable("systemTaskType") Integer systemTaskType) {
        return notifyConfigService.getNotifyConfigBySystemTaskTypeList(systemTaskType);
    }

    @LoginRequired
    @GetMapping("{id}")
    public NotifyConfigResponseVO getNotifyConfigDetail(@PathVariable("id") Long id) {
        return notifyConfigService.getNotifyConfigDetail(id);
    }

    @LoginRequired
    @PostMapping
    public Boolean saveNotify(@RequestBody @Validated NotifyConfigRequestVO requestVO) {
        return notifyConfigService.saveNotify(requestVO);
    }

    @LoginRequired
    @PutMapping
    public Boolean updateNotify(@RequestBody @Validated NotifyConfigRequestVO requestVO) {
        return notifyConfigService.updateNotify(requestVO);
    }

    @LoginRequired
    @PutMapping("/{id}/status/{status}")
    public Boolean updateStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status) {
        return notifyConfigService.updateStatus(id, status);
    }

    @LoginRequired
    @DeleteMapping("ids")
    public Boolean batchDeleteNotify(@RequestBody @NotEmpty(message = "ids不能为空") Set<Long> ids) {
        return notifyConfigService.batchDeleteNotify(ids);
    }
}
