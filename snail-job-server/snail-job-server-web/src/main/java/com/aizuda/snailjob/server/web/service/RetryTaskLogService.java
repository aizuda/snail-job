package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.RetryTaskLogMessageQueryVO;
import com.aizuda.snailjob.server.web.model.request.RetryTaskQueryVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskLogMessageResponseVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskLogResponseVO;

import java.util.List;
import java.util.Set;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 2.0
 */
public interface RetryTaskLogService {

    PageResult<List<RetryTaskLogResponseVO>> getRetryTaskLogPage(RetryTaskQueryVO queryVO);

    RetryTaskLogMessageResponseVO getRetryTaskLogMessagePage(RetryTaskLogMessageQueryVO queryVO);

    RetryTaskLogResponseVO getRetryTaskLogById(Long id);

    boolean deleteById(Long id);

    boolean batchDelete(Set<Long> ids);
}
