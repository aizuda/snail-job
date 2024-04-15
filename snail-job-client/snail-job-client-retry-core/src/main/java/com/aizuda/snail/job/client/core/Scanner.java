package com.aizuda.snail.job.client.core;

import com.aizuda.snail.job.client.core.retryer.RetryerInfo;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-02-10 09:13
 */
public interface Scanner {

	List<RetryerInfo> doScan();
}
