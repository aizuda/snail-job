package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.JobNotifyConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.request.JobNotifyConfigRequestVO;
import com.aizuda.easy.retry.server.web.model.response.JobNotifyConfigResponseVO;
import com.aizuda.easy.retry.server.web.service.JobNotifyConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Job通知配置接口
 *
 * @author: zuoJunLin
 * @date : 2023-12-02 11:32
 * @since ： 2.5.0
 */
@RestController
@RequestMapping("/job/notify/config")
public class JobNotifyConfigController {

    @Autowired
    private JobNotifyConfigService jobNotifyConfigService;

    @LoginRequired
    @GetMapping("/page/list")
    public PageResult<List<JobNotifyConfigResponseVO>> getNotifyConfigList(JobNotifyConfigQueryVO queryVO) {
        return jobNotifyConfigService.getJobNotifyConfigList(queryVO);
    }

    @LoginRequired
    @GetMapping("{id}")
    public JobNotifyConfigResponseVO getJobNotifyConfigDetail(@PathVariable("id") Long id) {
        return jobNotifyConfigService.getJobNotifyConfigDetail(id);
    }


    @LoginRequired
    @PostMapping
    public Boolean saveNotify(@RequestBody @Validated JobNotifyConfigRequestVO requestVO) {
        return jobNotifyConfigService.saveJobNotify(requestVO);
    }

    @LoginRequired
    @PutMapping
    public Boolean updateNotify(@RequestBody @Validated JobNotifyConfigRequestVO requestVO) {
        return jobNotifyConfigService.updateJobNotify(requestVO);
    }
}
