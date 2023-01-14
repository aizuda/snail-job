package com.x.retry.server.service;

import com.x.retry.server.web.model.base.PageResult;
import com.x.retry.server.model.dto.ConfigDTO;
import com.x.retry.server.web.model.request.GroupConfigQueryVO;
import com.x.retry.server.web.model.request.GroupConfigRequestVO;
import com.x.retry.server.web.model.response.GroupConfigResponseVO;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-22 14:53
 */
public interface GroupConfigService {

    Boolean addGroup(GroupConfigRequestVO groupConfigRequestVO);

    Boolean updateGroup(GroupConfigRequestVO groupConfigRequestVO);

    PageResult<List<GroupConfigResponseVO>> getGroupConfigForPage(GroupConfigQueryVO queryVO);

    GroupConfigResponseVO getGroupConfigByGroupName(String groupName);

    List<String> getAllGroupNameList();
}
