package com.aizuda.snail.job.server.web.service;

import com.aizuda.snail.job.server.web.model.base.PageResult;
import com.aizuda.snail.job.server.web.model.request.NotifyConfigQueryVO;
import com.aizuda.snail.job.server.web.model.request.NotifyConfigRequestVO;
import com.aizuda.snail.job.server.web.model.response.NotifyConfigResponseVO;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-03-03 11:17
 */
public interface NotifyConfigService {

    PageResult<List<NotifyConfigResponseVO>> getNotifyConfigList(NotifyConfigQueryVO queryVO);

    Boolean saveNotify(NotifyConfigRequestVO requestVO);

    Boolean updateNotify(NotifyConfigRequestVO requestVO);

    NotifyConfigResponseVO getNotifyConfigDetail(Long id);
}
