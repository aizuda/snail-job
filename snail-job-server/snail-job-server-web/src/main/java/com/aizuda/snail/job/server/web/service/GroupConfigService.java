package com.aizuda.snail.job.server.web.service;

import com.aizuda.snail.job.server.web.model.base.PageResult;
import com.aizuda.snail.job.server.web.model.request.GroupConfigQueryVO;
import com.aizuda.snail.job.server.web.model.request.GroupConfigRequestVO;
import com.aizuda.snail.job.server.web.model.request.UserSessionVO;
import com.aizuda.snail.job.server.web.model.response.GroupConfigResponseVO;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2021-11-22 14:53
 */
public interface GroupConfigService {

    Boolean addGroup(UserSessionVO systemUser, GroupConfigRequestVO groupConfigRequestVO);

    Boolean updateGroup(GroupConfigRequestVO groupConfigRequestVO);

    Boolean updateGroupStatus(String groupName, Integer status);

    PageResult<List<GroupConfigResponseVO>> getGroupConfigForPage(GroupConfigQueryVO queryVO);

    GroupConfigResponseVO getGroupConfigByGroupName(String groupName);

    List<GroupConfigResponseVO> getAllGroupConfigList(final List<String> namespaceId);

    List<String> getAllGroupNameList();

    List<String> getOnlinePods(String groupName);

    List<Integer> getTablePartitionList();
}
