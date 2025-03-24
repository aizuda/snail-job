package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.common.vo.JobLogQueryVO;
import com.aizuda.snailjob.server.common.vo.JobLogResponseVO;
import com.aizuda.snailjob.server.web.service.JobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:56
 * @since ： 2.4.0
 */
@RestController
@RequestMapping("/job")
public class JobLogController {

    @Autowired
    private JobLogService jobLogService;

    @GetMapping("/log/list")
    @LoginRequired
    public JobLogResponseVO getJobLogPage(JobLogQueryVO jobQueryVO) {
        return jobLogService.getJobLogPage(jobQueryVO);
    }

}
