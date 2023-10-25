package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.NotifyConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.request.NotifyConfigRequestVO;
import com.aizuda.easy.retry.server.web.model.response.NotifyConfigResponseVO;
import com.aizuda.easy.retry.server.web.service.NotifyConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知配置接口
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-03 11:32
 */
@RestController
@RequestMapping("/notify-config")
public class NotifyConfigController {

    @Autowired
    private NotifyConfigService notifyConfigService;

    @LoginRequired
    @GetMapping("list")
    public PageResult<List<NotifyConfigResponseVO>> getNotifyConfigList(NotifyConfigQueryVO queryVO) {
        return notifyConfigService.getNotifyConfigList(queryVO);
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
}
