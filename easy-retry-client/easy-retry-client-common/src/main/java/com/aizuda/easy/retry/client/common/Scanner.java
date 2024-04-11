package com.aizuda.easy.retry.client.common;

import com.aizuda.easy.retry.client.common.netty.server.EndPointInfo;

import java.util.List;

/**
 *
 * @author: opensnail
 * @date : 2024-04-11 22:34
 */
public interface Scanner {

	List<EndPointInfo> doScan();
}
