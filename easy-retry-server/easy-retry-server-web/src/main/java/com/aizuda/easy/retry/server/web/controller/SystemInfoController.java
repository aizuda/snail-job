package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.EasyRetryVersion;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统信息
 *
 * @author www.byteblogs.com
 * @date 2023-05-07
 * @since 1.2.0
 */
@RestController
@RequestMapping("/system")
public class SystemInfoController {

    @GetMapping("version")
    public Result<String> version() {
        return new Result<>(EasyRetryVersion.getVersion());
    }
}
