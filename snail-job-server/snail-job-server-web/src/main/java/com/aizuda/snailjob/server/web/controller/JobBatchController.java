package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobBatchQueryVO;
import com.aizuda.snailjob.server.common.vo.JobBatchResponseVO;
import com.aizuda.snailjob.server.web.service.JobBatchService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
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
    private final JobBatchService jobBatchService;

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

    @DeleteMapping("/ids")
    @LoginRequired
    public Boolean deleteJobBatchByIds(@RequestBody
                                       @NotEmpty(message = "ids cannot be null")
                                       @Size(max = 100, message = "Maximum {max} deletions")
                                       Set<Long> ids) {
        return jobBatchService.deleteJobBatchByIds(ids);
    }
}
