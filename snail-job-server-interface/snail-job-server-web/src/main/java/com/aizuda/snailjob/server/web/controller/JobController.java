package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.common.core.annotation.OriginalControllerReturnValue;
import com.aizuda.snailjob.model.request.base.JobTriggerRequest;
import com.aizuda.snailjob.server.service.service.JobService;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.server.web.model.response.JobResponseWebVO;
import com.aizuda.snailjob.server.web.service.JobWebService;
import com.aizuda.snailjob.server.web.util.ExportUtils;
import com.aizuda.snailjob.server.web.util.ImportUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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
@RequiredArgsConstructor
public class JobController {

    @Qualifier("jobWebCommonService")
    private final JobService jobService;
    private final JobWebService jobWebService;

    @GetMapping("/page/list")
    @LoginRequired
    public PageResult<List<JobResponseWebVO>> getJobPage(JobQueryVO jobQueryVO) {
        return jobWebService.getJobPage(jobQueryVO);
    }

    @GetMapping("/list")
    @LoginRequired
    public List<JobResponseWebVO> getJobList(@RequestParam("groupName") String groupName) {
        return jobWebService.getJobList(groupName);
    }

    @GetMapping("{id}")
    @LoginRequired
    public JobResponseWebVO getJobDetail(@PathVariable("id") Long id) {
        return jobService.getJobById(id, JobResponseWebVO.class);
    }

    @PostMapping
    @LoginRequired
    public Long saveJob(@RequestBody @Validated JobRequestWebVO jobRequestWebVO) {
        return jobService.addJob(jobRequestWebVO);
    }

    @PutMapping
    @LoginRequired
    public Boolean updateJob(@RequestBody @Validated JobRequestWebVO jobRequestWebVO) {
        return jobService.updateJob(jobRequestWebVO);
    }

    @PutMapping("/status")
    @LoginRequired
    public Boolean updateJobStatus(@RequestBody @Validated StatusUpdateRequestWebVO jobRequestVO) {
        return jobService.updateJobStatus(jobRequestVO);
    }

    @DeleteMapping("/ids")
    @LoginRequired
    public Boolean deleteJobById(@RequestBody @NotEmpty(message = "ids cannot be null") Set<Long> ids) {
        return jobService.deleteJobByIds(ids);
    }

    @GetMapping("/cron")
    @LoginRequired
    public List<String> getTimeByCron(@RequestParam("cron") String cron) {
        return jobWebService.getTimeByCron(cron);
    }

    @GetMapping("/job-name/list")
    @LoginRequired
    public List<JobResponseWebVO> getJobNameList(
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "jobId", required = false) Long jobId,
            @RequestParam(value = "groupName", required = false) String groupName
    ) {
        return jobWebService.getJobNameList(keywords, jobId, groupName);
    }

    @PostMapping("/trigger")
    @LoginRequired
    public Boolean trigger(@RequestBody @Validated JobTriggerVO jobTrigger) {
        JobTriggerRequest triggerDTO = new JobTriggerRequest();
        triggerDTO.setJobId(jobTrigger.getJobId());
        triggerDTO.setTmpArgsStr(jobTrigger.getTmpArgsStr());
        return jobService.trigger(triggerDTO);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @LoginRequired
    public void importScene(@RequestPart("file") MultipartFile file) throws IOException {
        jobWebService.importJobs(ImportUtils.parseList(file, JobRequestWebVO.class));
    }

    @PostMapping("/export")
    @LoginRequired
    @OriginalControllerReturnValue
    public ResponseEntity<String> exportGroup(@RequestBody ExportJobVO exportJobVO) {
        return ExportUtils.doExport(jobWebService.exportJobs(exportJobVO));
    }

}
