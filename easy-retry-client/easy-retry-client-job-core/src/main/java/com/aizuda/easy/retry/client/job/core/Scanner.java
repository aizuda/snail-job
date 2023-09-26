package com.aizuda.easy.retry.client.job.core;


import com.aizuda.easy.retry.client.job.core.dto.JobExecutorInfo;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-10 09:13
 */
public interface Scanner {

	List<JobExecutorInfo> doScan();
}
