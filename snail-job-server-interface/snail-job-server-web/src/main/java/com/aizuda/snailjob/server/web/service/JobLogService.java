package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.request.JobLogQueryVO;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:54
 * @since ：2.4.0
 */
public interface JobLogService {

    void getJobLogPage(JobLogQueryVO jobQueryVO);

}
