package com.aizuda.snailjob.client.job.core;


import com.aizuda.snailjob.client.job.core.dto.JobExecutorInfo;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-02-10 09:13
 */
public interface Scanner {

    List<JobExecutorInfo> doScan();
}
