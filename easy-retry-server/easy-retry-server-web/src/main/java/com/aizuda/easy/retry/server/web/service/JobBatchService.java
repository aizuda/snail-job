package com.aizuda.easy.retry.server.web.service;

import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.JobBatchQueryVO;
import com.aizuda.easy.retry.server.web.model.response.JobBatchResponseVO;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-12 09:54
 * @since ：2.4.0
 */
public interface JobBatchService {

    PageResult<List<JobBatchResponseVO>> getJobBatchPage(JobBatchQueryVO jobQueryVO);

    JobBatchResponseVO getJobBatchDetail(Long id);
}
