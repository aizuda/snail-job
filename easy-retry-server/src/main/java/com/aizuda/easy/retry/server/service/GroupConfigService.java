package com.aizuda.easy.retry.server.service;

import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigRequestVO;
import com.aizuda.easy.retry.server.web.model.response.GroupConfigResponseVO;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-22 14:53
 */
public interface GroupConfigService {

    Boolean addGroup(GroupConfigRequestVO groupConfigRequestVO);

    Boolean updateGroup(GroupConfigRequestVO groupConfigRequestVO);

    Boolean updateGroupStatus(String groupName, Integer status);

    PageResult<List<GroupConfigResponseVO>> getGroupConfigForPage(GroupConfigQueryVO queryVO);

    GroupConfigResponseVO getGroupConfigByGroupName(String groupName);

    List<String> getAllGroupNameList();
}
