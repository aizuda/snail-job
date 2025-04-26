package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.common.vo.JobLogQueryVO;
import com.aizuda.snailjob.server.common.vo.JobLogResponseVO;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:54
 * @since ：2.4.0
 */
public interface JobLogService {

    JobLogResponseVO getJobLogPage(JobLogQueryVO jobQueryVO);
}
