package com.aizuda.snailjob.server.web.controller;

import cn.hutool.core.lang.tree.Tree;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobTaskQueryVO;
import com.aizuda.snailjob.server.web.model.response.JobTaskResponseVO;
import com.aizuda.snailjob.server.web.service.JobTaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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

    @DeleteMapping("/ids")
    @LoginRequired
    public Boolean deleteJobTaskById(@RequestBody @Valid @NotEmpty(message = "ids不能为空") Set<Long> ids) {
        return jobTaskService.deleteJobTaskById(ids);
    }
}
