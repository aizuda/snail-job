package com.aizuda.snail.job.server.web.controller;

import com.aizuda.snail.job.server.web.annotation.LoginRequired;
import com.aizuda.snail.job.server.web.model.base.PageResult;
import com.aizuda.snail.job.server.web.model.request.JobQueryVO;
import com.aizuda.snail.job.server.web.model.request.JobRequestVO;
import com.aizuda.snail.job.server.web.model.request.JobUpdateJobStatusRequestVO;
import com.aizuda.snail.job.server.web.model.response.JobResponseVO;
import com.aizuda.snail.job.server.web.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-10-11 22:18:29
 * @since 2.4.0
 */
@RestController
@RequestMapping("/job")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping("/page/list")
    @LoginRequired
    public PageResult<List<JobResponseVO>> getJobPage(JobQueryVO jobQueryVO) {
        return jobService.getJobPage(jobQueryVO);
    }

    @GetMapping("/list")
    @LoginRequired
    public List<JobResponseVO> getJobList(@RequestParam("groupName") String groupName) {
        return jobService.getJobList(groupName);
    }

    @GetMapping("{id}")
    @LoginRequired
    public JobResponseVO getJobDetail(@PathVariable("id") Long id) {
        return jobService.getJobDetail(id);
    }

    @PostMapping
    @LoginRequired
    public Boolean saveJob(@RequestBody @Validated JobRequestVO jobRequestVO) {
        return jobService.saveJob(jobRequestVO);
    }

    @PutMapping
    @LoginRequired
    public Boolean updateJob(@RequestBody @Validated JobRequestVO jobRequestVO) {
        return jobService.updateJob(jobRequestVO);
    }

    @PutMapping("/status")
    @LoginRequired
    public Boolean updateJobStatus(@RequestBody @Validated JobUpdateJobStatusRequestVO jobRequestVO) {
        return jobService.updateJobStatus(jobRequestVO);
    }

    @DeleteMapping("{id}")
    @LoginRequired
    public Boolean deleteJobById(@PathVariable("id") Long id) {
        return jobService.deleteJobById(id);
    }

    @GetMapping("/cron")
    @LoginRequired
    public List<String> getTimeByCron(@RequestParam("cron") String cron) {
        return jobService.getTimeByCron(cron);
    }

    @GetMapping("/job-name/list")
    @LoginRequired
    public List<JobResponseVO> getJobNameList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "jobId", required = false) Long jobId,
            @RequestParam(value = "groupName", required = false) String groupName
    ) {
        return jobService.getJobNameList(keywords, jobId, groupName);
    }

    @PostMapping("/trigger/{jobId}")
    @LoginRequired
    public Boolean trigger(@PathVariable("jobId") Long jobId) {
        return jobService.trigger(jobId);
    }
}
