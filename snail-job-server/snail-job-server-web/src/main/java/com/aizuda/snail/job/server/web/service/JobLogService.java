package com.aizuda.snail.job.server.web.service;

import com.aizuda.snail.job.server.web.model.request.JobLogQueryVO;
import com.aizuda.snail.job.server.web.model.response.JobLogResponseVO;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:54
 * @since ：2.4.0
 */
public interface JobLogService {

    JobLogResponseVO getJobLogPage(JobLogQueryVO jobQueryVO);
}
