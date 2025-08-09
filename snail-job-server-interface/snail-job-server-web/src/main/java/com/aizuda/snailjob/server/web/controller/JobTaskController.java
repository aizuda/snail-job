package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobTaskQueryVO;
import com.aizuda.snailjob.server.web.model.response.JobTaskResponseVO;
import com.aizuda.snailjob.server.web.service.JobTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:55
 * @since ： 2.4.0
 */
@RestController
@RequestMapping("/job/task")
@RequiredArgsConstructor
public class JobTaskController {
    private final JobTaskService jobTaskService;

    @GetMapping("/list")
    @LoginRequired
    public PageResult<List<JobTaskResponseVO>> getJobTaskPage(JobTaskQueryVO jobTaskQueryVO) {
        return jobTaskService.getJobTaskPage(jobTaskQueryVO);
    }

    @GetMapping("/tree/list")
    @LoginRequired
    public List<JobTaskResponseVO> getTreeJobTask(JobTaskQueryVO jobTaskQueryVO) {
        return jobTaskService.getTreeJobTask(jobTaskQueryVO);
    }

}
