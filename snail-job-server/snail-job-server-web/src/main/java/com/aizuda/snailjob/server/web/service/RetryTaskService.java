package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.RetryTaskLogMessageQueryVO;
import com.aizuda.snailjob.server.web.model.request.RetryTaskQueryVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskLogMessageResponseVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskResponseVO;

import java.util.List;
import java.util.Set;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 2.0
 */
public interface RetryTaskService {

    PageResult<List<RetryTaskResponseVO>> getRetryTaskLogPage(RetryTaskQueryVO queryVO);

    RetryTaskLogMessageResponseVO getRetryTaskLogMessagePage(RetryTaskLogMessageQueryVO queryVO);

    void getRetryTaskLogMessagePageV2(RetryTaskLogMessageQueryVO queryVO);

    RetryTaskResponseVO getRetryTaskById(Long id);

    boolean deleteById(Long id);

    boolean batchDelete(Set<Long> ids);

    Boolean stopById(Long id);
}
