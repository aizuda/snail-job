package com.aizuda.snailjob.server.web.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.annotation.OriginalControllerReturnValue;
import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobQueryVO;
import com.aizuda.snailjob.server.web.model.request.JobRequestVO;
import com.aizuda.snailjob.server.web.model.request.JobUpdateJobStatusRequestVO;
import com.aizuda.snailjob.server.web.model.response.JobResponseVO;
import com.aizuda.snailjob.server.web.service.JobService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    private static final List<String> FILE_EXTENSIONS = List.of("json");

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
    @LoginRequired(role = RoleEnum.ADMIN)
    public void importScene(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new SnailJobCommonException("请选择一个文件上传");
        }

        // 保存文件到服务器
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        if (!FILE_EXTENSIONS.contains(suffix)) {
            throw new SnailJobCommonException("文件类型错误");
        }

        JsonNode node = JsonUtil.toJson(file.getBytes());

        List<JobRequestVO> requestList = JsonUtil.parseList(JsonUtil.toJsonString(node),
                JobRequestVO.class);
        requestList.forEach(vo -> vo.setId(null));

        Assert.notEmpty(requestList, () -> new SnailJobServerException("导入数据不能为空"));

        // 校验参数是否合法
        requestList.forEach(vo ->{
            ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
            Validator validator = vf.getValidator();
            Set<ConstraintViolation<JobRequestVO>> set = validator.validate(vo);
            for (final ConstraintViolation<JobRequestVO> violation : set) {
                throw new SnailJobCommonException(violation.getMessage());
            }
        });

        jobService.importJobs(requestList);
    }

    @PostMapping("/export")
    @LoginRequired(role = RoleEnum.ADMIN)
    @OriginalControllerReturnValue
    public ResponseEntity<String> exportGroup(@RequestBody Set<Long> jobIds) {
        String jobsJson = jobService.exportJobs(jobIds);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 设置下载时的文件名称
        String fileName = String.format("job-%s.json", DateUtils.toNowFormat(DateUtils.PURE_DATETIME_MS_PATTERN));
        String disposition = "attachment; filename=" +
                new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, disposition);
        return ResponseEntity.ok()
                .headers(headers)
                .body(jobsJson);
    }

}
