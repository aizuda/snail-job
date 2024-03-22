package com.aizuda.easy.retry.server.web.service;

import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskLogMessageQueryVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskLogQueryVO;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskLogMessageResponseVO;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskLogResponseVO;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2022-02-27
 * @since 2.0
 */
public interface RetryTaskLogService {

    PageResult<List<RetryTaskLogResponseVO>> getRetryTaskLogPage(RetryTaskLogQueryVO queryVO);

    RetryTaskLogMessageResponseVO getRetryTaskLogMessagePage(RetryTaskLogMessageQueryVO queryVO);

    RetryTaskLogResponseVO getRetryTaskLogById(Long id);

}
