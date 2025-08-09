package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.service.service.JobBatchService;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobBatchQueryVO;
import com.aizuda.snailjob.server.web.model.request.JobBatchResponseWebVO;
import com.aizuda.snailjob.server.web.service.JobWebBatchService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:52
 * @since ： 2.4.0
 */
@RestController
@RequestMapping("/job/batch")
@RequiredArgsConstructor
public class JobBatchController {
    @Qualifier("jobWebBatchCommonService")
    private final JobBatchService jobBatchService;
    private final JobWebBatchService jobWebBatchService;

    @GetMapping("/list")
    @LoginRequired
    public PageResult<List<JobBatchResponseWebVO>> getJobBatchPage(JobBatchQueryVO jobQueryVO) {
        return jobWebBatchService.getJobBatchPage(jobQueryVO);
    }

    @GetMapping("{id}")
    @LoginRequired
    public JobBatchResponseWebVO getJobBatchDetail(@PathVariable("id") Long id) {
        return jobBatchService.getJobBatchById(id, JobBatchResponseWebVO.class);
    }

    @PostMapping("/stop/{taskBatchId}")
    @LoginRequired
    public Boolean stop(@PathVariable("taskBatchId") Long taskBatchId) {
        return jobWebBatchService.stop(taskBatchId);
    }


    @PostMapping("/retry/{taskBatchId}")
    @LoginRequired
    public Boolean retry(@PathVariable("taskBatchId") Long taskBatchId) {
        return jobWebBatchService.retry(taskBatchId);
    }

    @DeleteMapping("/ids")
    @LoginRequired
    public Boolean deleteJobBatchByIds(@RequestBody
                                       @NotEmpty(message = "ids cannot be null")
                                       @Size(max = 100, message = "Maximum {max} deletions")
                                       Set<Long> ids) {
        return jobWebBatchService.deleteJobBatchByIds(ids);
    }
}
