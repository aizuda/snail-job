package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.ExportGroupVO;
import com.aizuda.snailjob.server.web.model.request.GroupConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.GroupConfigRequestVO;
import com.aizuda.snailjob.server.web.model.response.GroupConfigResponseVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

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

    void importGroup(@Valid @NotEmpty(message = "导入数据不能为空") List<GroupConfigRequestVO> requestVOS);

    String exportGroup(ExportGroupVO exportGroupVO);

    boolean deleteByGroupName(String groupName);
}
