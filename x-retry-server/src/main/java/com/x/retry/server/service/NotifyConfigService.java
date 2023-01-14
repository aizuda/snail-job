package com.x.retry.server.service;

import com.x.retry.server.web.model.response.NotifyConfigResponseVO;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 11:17
 */
public interface NotifyConfigService {

    List<NotifyConfigResponseVO> getNotifyConfigList(String groupName);

}
