package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.server.web.model.response.JobResponseVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Set;

/**
 * @author opensnail
 * @date 2023-10-11 22:20:23
 * @since 2.4.0
 */
public interface JobService {
    PageResult<List<JobResponseVO>> getJobPage(JobQueryVO jobQueryVO);

    JobResponseVO getJobDetail(Long id);

    boolean saveJob(JobRequestVO jobRequestVO);

    boolean updateJob(JobRequestVO jobRequestVO);

    Boolean updateJobStatus(JobStatusUpdateRequestVO jobRequestVO);

    List<String> getTimeByCron(String cron);

    List<JobResponseVO> getJobNameList(String keywords, Long jobId, String groupName);

    boolean trigger(JobTriggerVO jobTrigger);

    List<JobResponseVO> getJobList(String groupName);

    void importJobs(@Valid @NotEmpty(message = "Import data cannot be empty") List<JobRequestVO> requestList);

    String exportJobs(ExportJobVO exportJobVO);

    Boolean deleteJobByIds(Set<Long> ids);
}
