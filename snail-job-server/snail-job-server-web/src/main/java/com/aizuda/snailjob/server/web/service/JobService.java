package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobQueryVO;
import com.aizuda.snailjob.server.web.model.request.JobRequestVO;
import com.aizuda.snailjob.server.web.model.request.JobUpdateJobStatusRequestVO;
import com.aizuda.snailjob.server.web.model.response.JobResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
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

    Boolean updateJobStatus(JobUpdateJobStatusRequestVO jobRequestVO);

    Job updateJobResident(JobRequestVO jobRequestVO);

    Boolean deleteJobById(Long id);

    List<String> getTimeByCron(String cron);

    List<JobResponseVO> getJobNameList(String keywords, Long jobId, String groupName);

    boolean trigger(Long jobId);

    List<JobResponseVO> getJobList(String groupName);

    void importJobs(@Valid @NotEmpty(message = "导入数据不能为空") List<JobRequestVO> requestList);

    String exportJobs(Set<Long> jobIds);

}
