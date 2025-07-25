package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobBatchQueryVO;
import com.aizuda.snailjob.server.web.model.request.JobBatchResponseWebVO;

import java.util.List;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:54
 * @since ：2.4.0
 */
public interface JobWebBatchService {

    PageResult<List<JobBatchResponseWebVO>> getJobBatchPage(JobBatchQueryVO jobQueryVO);

//    JobBatchResponseVO getJobBatchDetail(Long id);

    boolean stop(Long taskBatchId);

    Boolean retry(Long taskBatchId);

    Boolean deleteJobBatchByIds(Set<Long> ids);
}
