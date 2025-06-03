package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobExecutorQueryVO;
import com.aizuda.snailjob.server.web.service.JobExecutorService;
import com.aizuda.snailjob.template.datasource.persistence.po.JobExecutors;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.web.controller
 * @Project：snail-job
 * @Date：2025/6/3 13:17
 * @Filename：JobExecutorController
 * @since 1.6.0
 */
@RestController
@RequestMapping("/job/executor")
public class JobExecutorController {
    @Autowired
    private JobExecutorService jobExecutorService;

    @GetMapping("/page/list")
    @LoginRequired
    public PageResult<List<JobExecutors>> getJobPage(JobExecutorQueryVO executorQueryVO) {
        return jobExecutorService.getJobExecutorPage(executorQueryVO);
    }

    @GetMapping("/list")
    @LoginRequired
    public List<JobExecutors> getJobList(JobExecutorQueryVO executorQueryVO) {
        return jobExecutorService.getJobExecutorList(executorQueryVO);
    }

    @GetMapping("{id}")
    @LoginRequired
    public JobExecutors getJobExecutorsDetail(@PathVariable("id") Long id) {
        return jobExecutorService.getJobExecutorDetail(id);
    }

    @DeleteMapping("/ids")
    @LoginRequired
    public Boolean deleteJobExecutorsById(@RequestBody @NotEmpty(message = "ids cannot be null") Set<Long> ids) {
        return jobExecutorService.deleteJobExecutorByIds(ids);
    }
}
