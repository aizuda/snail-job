package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.template.datasource.persistence.po.JobExecutor;

import java.util.List;
import java.util.Set;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.web.service
 * @Project：snail-job
 * @Date：2025/6/3 13:19
 * @Filename：JobExecutorService
 */
public interface JobExecutorService {
    PageResult<List<JobExecutor>> getJobExecutorPage(JobExecutorQueryVO jobQueryVO);

    JobExecutor getJobExecutorDetail(Long id);

    Set<String> getJobExecutorList(JobExecutorQueryVO jobQueryVO);

    Boolean deleteJobExecutorByIds(Set<Long> ids);
}
