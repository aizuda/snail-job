package com.aizuda.easy.retry.client.core;

import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-10 09:13
 */
public interface Scanner {

	List<RetryerInfo> doScan();
}
