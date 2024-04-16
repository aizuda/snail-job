package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.SnailJobVersion;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统信息
 *
 * @author opensnail
 * @date 2023-05-07
 * @since 1.2.0
 */
@RestController
@RequestMapping("/system")
public class SystemInfoController {

    @GetMapping("version")
    public Result<String> version() {
        return new Result<>(SnailJobVersion.getVersion());
    }
}
