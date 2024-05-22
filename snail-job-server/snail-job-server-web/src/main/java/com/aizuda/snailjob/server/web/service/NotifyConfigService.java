package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.NotifyConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.NotifyConfigRequestVO;
import com.aizuda.snailjob.server.web.model.response.NotifyConfigResponseVO;

import java.util.List;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2022-03-03 11:17
 */
public interface NotifyConfigService {

    PageResult<List<NotifyConfigResponseVO>> getNotifyConfigList(NotifyConfigQueryVO queryVO);

    Boolean saveNotify(NotifyConfigRequestVO requestVO);

    Boolean updateNotify(NotifyConfigRequestVO requestVO);

    NotifyConfigResponseVO getNotifyConfigDetail(Long id);

    Boolean updateStatus(Long id, Integer status);

    Boolean batchDeleteNotify(Set<Long> ids);
}
