package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.service.GroupConfigService;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigRequestVO;
import com.aizuda.easy.retry.server.web.model.response.GroupConfigResponseVO;
import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import com.aizuda.easy.retry.server.web.annotation.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-22 14:38
 */
@RestController
@RequestMapping("/group")
public class GroupConfigController {

    @Autowired
    private GroupConfigService groupConfigService;

    @LoginRequired(role = RoleEnum.ADMIN)
    @PostMapping("")
    public Boolean addGroup(@RequestBody @Validated(PostMapping.class) GroupConfigRequestVO groupConfigRequestVO) {
        return groupConfigService.addGroup(groupConfigRequestVO);
    }

    @LoginRequired
    @PutMapping("")
    public Boolean updateGroup(@RequestBody @Validated GroupConfigRequestVO groupConfigRequestVO) {
        return groupConfigService.updateGroup(groupConfigRequestVO);
    }

    @LoginRequired
    @GetMapping("list")
    public PageResult<List<GroupConfigResponseVO>> getGroupConfigForPage(GroupConfigQueryVO queryVO) {
        return groupConfigService.getGroupConfigForPage(queryVO);
    }

    @LoginRequired
    @GetMapping("{groupName}")
    public GroupConfigResponseVO getGroupConfigByGroupName(@PathVariable("groupName") String groupName) {
        return groupConfigService.getGroupConfigByGroupName(groupName);
    }

    @LoginRequired
    @GetMapping("/all/group-name/list")
    public List<String> getAllGroupNameList() {
        return groupConfigService.getAllGroupNameList();
    }

}
