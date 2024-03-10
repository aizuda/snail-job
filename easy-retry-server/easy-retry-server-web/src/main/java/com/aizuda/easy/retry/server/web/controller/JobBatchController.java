package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.JobBatchQueryVO;
import com.aizuda.easy.retry.server.web.model.response.JobBatchResponseVO;
import com.aizuda.easy.retry.server.web.service.JobBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-12 09:52
 * @since ： 2.4.0
 */
@RestController
@RequestMapping("/job/batch")
public class JobBatchController {

    @Autowired
    private JobBatchService jobBatchService;

    @GetMapping("/list")
    @LoginRequired
    public PageResult<List<JobBatchResponseVO>> getJobBatchPage(JobBatchQueryVO jobQueryVO) {
        return jobBatchService.getJobBatchPage(jobQueryVO);
    }

    @GetMapping("{id}")
    @LoginRequired
    public JobBatchResponseVO getJobBatchDetail(@PathVariable("id") Long id) {
        return jobBatchService.getJobBatchDetail(id);
    }

    @PostMapping("/stop/{taskBatchId}")
    @LoginRequired
    public Boolean stop(@PathVariable("taskBatchId") Long taskBatchId) {
        return jobBatchService.stop(taskBatchId);
    }


    @PostMapping("/retry/{taskBatchId}")
    @LoginRequired
    public Boolean retry(@PathVariable("taskBatchId") Long taskBatchId) {
        return jobBatchService.retry(taskBatchId);
    }

}
