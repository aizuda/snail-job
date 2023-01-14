package com.x.retry.server.service;

import com.x.retry.server.web.model.base.PageResult;
import com.x.retry.server.web.model.request.RetryTaskLogQueryVO;
import com.x.retry.server.web.model.response.RetryTaskLogResponseVO;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2022-02-27
 * @since 2.0
 */
public interface RetryTaskLogService {

    PageResult<List<RetryTaskLogResponseVO>> getRetryTaskLogPage(RetryTaskLogQueryVO queryVO);

    RetryTaskLogResponseVO getRetryTaskLogById(Long id);

}
