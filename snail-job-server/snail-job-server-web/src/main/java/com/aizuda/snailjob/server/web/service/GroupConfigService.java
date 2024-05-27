package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.GroupConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.GroupConfigRequestVO;
import com.aizuda.snailjob.server.web.model.response.GroupConfigResponseVO;

import java.util.List;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2021-11-22 14:53
 */
public interface GroupConfigService {

    Boolean addGroup(GroupConfigRequestVO groupConfigRequestVO);

    Boolean updateGroup(GroupConfigRequestVO groupConfigRequestVO);

    Boolean updateGroupStatus(String groupName, Integer status);

    PageResult<List<GroupConfigResponseVO>> getGroupConfigForPage(GroupConfigQueryVO queryVO);

    GroupConfigResponseVO getGroupConfigByGroupName(String groupName);

    List<GroupConfigResponseVO> getAllGroupConfigList(final List<String> namespaceId);

    List<String> getAllGroupNameList();

    List<String> getOnlinePods(String groupName);

    List<Integer> getTablePartitionList();

    void importGroup(List<GroupConfigRequestVO> requestVOS);

    String exportGroup(Set<Long> groupIds);
}
