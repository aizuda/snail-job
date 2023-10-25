package com.aizuda.easy.retry.server.web.service;

import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.NotifyConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.request.NotifyConfigRequestVO;
import com.aizuda.easy.retry.server.web.model.response.NotifyConfigResponseVO;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 11:17
 */
public interface NotifyConfigService {

    PageResult<List<NotifyConfigResponseVO>> getNotifyConfigList(NotifyConfigQueryVO queryVO);

    Boolean saveNotify(NotifyConfigRequestVO requestVO);

    Boolean updateNotify(NotifyConfigRequestVO requestVO);

    NotifyConfigResponseVO getNotifyConfigDetail(Long id);
}
