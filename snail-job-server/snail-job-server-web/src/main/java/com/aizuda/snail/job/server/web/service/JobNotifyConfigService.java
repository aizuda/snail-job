package com.aizuda.snail.job.server.web.service;

import com.aizuda.snail.job.server.web.model.base.PageResult;
import com.aizuda.snail.job.server.web.model.request.JobNotifyConfigQueryVO;
import com.aizuda.snail.job.server.web.model.request.JobNotifyConfigRequestVO;
import com.aizuda.snail.job.server.web.model.response.JobNotifyConfigResponseVO;

import java.util.List;

/**
 * @author: zuoJunLin
 * @date : 2023-12-02 12:54
 * @since ：2.5.0
 */
public interface JobNotifyConfigService {

    PageResult<List<JobNotifyConfigResponseVO>> getJobNotifyConfigList(JobNotifyConfigQueryVO queryVO);

    Boolean saveJobNotify(JobNotifyConfigRequestVO requestVO);

    Boolean updateJobNotify(JobNotifyConfigRequestVO requestVO);

    JobNotifyConfigResponseVO getJobNotifyConfigDetail(Long id);
}
