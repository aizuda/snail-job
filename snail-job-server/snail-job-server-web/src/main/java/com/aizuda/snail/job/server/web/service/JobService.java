package com.aizuda.snail.job.server.web.service;

import com.aizuda.snail.job.server.web.model.base.PageResult;
import com.aizuda.snail.job.server.web.model.request.JobQueryVO;
import com.aizuda.snail.job.server.web.model.request.JobRequestVO;
import com.aizuda.snail.job.server.web.model.request.JobUpdateJobStatusRequestVO;
import com.aizuda.snail.job.server.web.model.response.JobResponseVO;
import com.aizuda.snail.job.template.datasource.persistence.po.Job;

import java.util.List;

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

    Boolean updateJobStatus(JobUpdateJobStatusRequestVO jobRequestVO);

    Job updateJobResident(JobRequestVO jobRequestVO);

    Boolean deleteJobById(Long id);

    List<String> getTimeByCron(String cron);

    List<JobResponseVO> getJobNameList(String keywords, Long jobId, String groupName);

    boolean trigger(Long jobId);

    List<JobResponseVO> getJobList(String groupName);
}
