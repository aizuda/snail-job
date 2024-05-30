package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.common.core.annotation.OriginalControllerReturnValue;
import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.ExportJobVO;
import com.aizuda.snailjob.server.web.model.request.JobQueryVO;
import com.aizuda.snailjob.server.web.model.request.JobRequestVO;
import com.aizuda.snailjob.server.web.model.request.JobUpdateJobStatusRequestVO;
import com.aizuda.snailjob.server.web.model.response.JobResponseVO;
import com.aizuda.snailjob.server.web.service.JobService;
import com.aizuda.snailjob.server.web.util.ExportUtils;
import com.aizuda.snailjob.server.web.util.ImportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LoginRequired
    public void importScene(@RequestPart("file") MultipartFile file) throws IOException {
        jobService.importJobs(ImportUtils.parseList(file, JobRequestVO.class));
    }

    @PostMapping("/export")
    @LoginRequired
    @OriginalControllerReturnValue
    public ResponseEntity<String> exportGroup(@RequestBody ExportJobVO exportJobVO) {
        return ExportUtils.doExport(jobService.exportJobs(exportJobVO));
    }

}
